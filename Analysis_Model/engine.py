# engine.py
from typing import Tuple, List, Dict, Optional
from math import isnan
from jinja2 import Template

from models import (
    WeatherResponse, Daily, Hourly,
    Alert, Recommendation, CropAnalysis,
    Severity, AlertType
)
from crops_store import load_crops, normalize_name, CropSpec

# ------------------------------------------------------------------------------
# Configuration
# ------------------------------------------------------------------------------
CROPS: Dict[str, CropSpec] = load_crops("crops.csv")
RECS: Dict = {}

# Optional ML (classification-based suggestions)
try:
    from load_model import get_model, get_meta, is_model_ready
except Exception:
    def get_model():
        return None
    def get_meta():
        return {}
    def is_model_ready():
        return False

# Load YAML recommendation templates (required for verbal recs; if missing, skip)
try:
    import yaml
    with open("recs_library.yaml", "r") as f:
        parsed = yaml.safe_load(f) or {}
        RECS = parsed if isinstance(parsed, dict) else {}
except Exception:
    RECS = {}  # no fallbacks here (per request: "only yaml")


# ------------------------------------------------------------------------------
# Helpers
# ------------------------------------------------------------------------------
def next_day_bounds(hourly: Hourly) -> Optional[Tuple[int, int]]:
    """Return (start_idx, end_idx) for the next calendar day in hourly arrays."""
    if not hourly or not hourly.time:
        return None
    first_day = hourly.time[0][:10]
    next_day = None
    for t in hourly.time:
        d = t[:10]
        if d != first_day:
            next_day = d
            break
    if not next_day:
        return None
    idxs = [i for i, t in enumerate(hourly.time) if t.startswith(next_day)]
    if not idxs:
        return None
    return (min(idxs), max(idxs))


def next_day_precip_extrema(hourly: Hourly) -> Tuple[float, float]:
    b = next_day_bounds(hourly)
    if not b:
        return (0.0, 0.0)
    i, j = b
    vals = hourly.precipitation[i:j + 1] if hourly.precipitation else []
    if not vals:
        return (0.0, 0.0)
    return (max(vals), min(vals))


def next_day_humidity_mean(daily: Daily, hourly: Hourly) -> Tuple[float, bool]:
    """
    Returns (mean_humidity, used_hourly).
    Prefers hourly for the next day; otherwise uses daily mean index 1 if present.
    """
    b = next_day_bounds(hourly)
    if getattr(hourly, "relative_humidity_2m", None) and b:
        i, j = b
        vals = [v for v in hourly.relative_humidity_2m[i:j + 1] if v is not None]
        if vals:
            return (sum(vals) / len(vals), True)
    if getattr(daily, "relative_humidity_2m_mean", None) and len(daily.relative_humidity_2m_mean) > 1:
        return (daily.relative_humidity_2m_mean[1], False)
    import math
    return (math.nan, False)


def classify_severity(dmax: float, dmin: float, drmax: float, drmin: float, dh: float) -> Severity:
    # Weighted sum of normalized positive deviations
    score = 0.0
    score += 1.5 * max(0.0, dmax) + 1.0 * max(0.0, dmin)           # temperature
    score += 1.2 * max(0.0, drmax) + 1.0 * max(0.0, drmin)         # rain
    score += 1.3 * max(0.0, dh)                                    # humidity
    if score >= 3.0:
        return "HIGH"
    if score >= 1.2:
        return "MEDIUM"
    return "LOW"


def alert_type_for(sev: Severity) -> AlertType:
    return "ERROR" if sev == "HIGH" else "WARNING" if sev == "MEDIUM" else "INFO"


def _template(bucket: str, kind: str, sev: str) -> Optional[str]:
    """
    Look up a template string in YAML by bucket/kind/severity.
    For humidity/ml buckets, severity key is 'any'.
    """
    node = RECS.get(bucket) if isinstance(RECS, dict) else None
    if not isinstance(node, dict):
        return None
    node = node.get(kind)
    if not isinstance(node, dict):
        return None
    key = "any" if bucket in ("humidity", "ml") else (sev or "").lower()
    val = node.get(key)
    return val if isinstance(val, str) and val.strip() else None


def _render(bucket: str, kind: str, sev: Severity, crop: str, ctx: Dict) -> Optional[str]:
    txt = _template(bucket, kind, sev)
    if not txt:
        return None
    try:
        return Template(txt).render(crop=crop, **ctx)
    except Exception:
        return None


def _ml_top_k(temp: float, hum: float, rainfall: float, ph: float, k: int = 3):
    """
    Returns list of (label, prob_float) for top-k predictions if model ready; else [].
    """
    if not is_model_ready():
        return []
    model = get_model()
    meta = get_meta() or {}
    feats = (meta.get("features") or ["temperature", "humidity", "ph", "rainfall"])
    # Build feature row in meta order
    vec = []
    for f in feats:
        if f == "temperature":
            vec.append(temp)
        elif f == "humidity":
            vec.append(hum)
        elif f == "rainfall":
            vec.append(rainfall)
        elif f == "ph":
            vec.append(ph)
        else:
            vec.append(0.0)
    try:
        import numpy as np
        X = np.array([vec], dtype=float)
        proba = model.predict_proba(X)[0]
        labels = meta.get("classes") or []
        pairs = list(zip(labels, proba))
        pairs.sort(key=lambda x: x[1], reverse=True)
        return pairs[:k]
    except Exception:
        return []


# ------------------------------------------------------------------------------
# Main analysis
# ------------------------------------------------------------------------------
def analyze_one(crop_name: str, w: WeatherResponse) -> CropAnalysis:
    # Lookup spec
    spec: CropSpec = CROPS.get(normalize_name(crop_name))
    if not spec:
        # If crop not found in CSV, still do numeric evaluation but no YAML recs will mention notes/ranges.
        spec = CropSpec(crop_name, 15, 27, 25, 75, 60, 80, (90, 150), "")

    # Tomorrow (fallback to today if necessary)
    tmax = w.daily.temperature_2m_max[1] if len(w.daily.temperature_2m_max) > 1 else w.daily.temperature_2m_max[0]
    tmin = w.daily.temperature_2m_min[1] if len(w.daily.temperature_2m_min) > 1 else w.daily.temperature_2m_min[0]
    rmax_next, rmin_next = next_day_precip_extrema(w.hourly)
    hmean, used_hourly = next_day_humidity_mean(w.daily, w.hourly)

    # Normalized deviations
    dmax = (tmax - spec.max_temp) / spec.max_temp if tmax > spec.max_temp else 0.0
    dmin = (spec.min_temp - tmin) / spec.min_temp if tmin < spec.min_temp else 0.0
    drmax = (rmax_next - spec.max_rain) / spec.max_rain if rmax_next > spec.max_rain else 0.0
    drmin = (spec.min_rain - rmin_next) / spec.min_rain if rmin_next < spec.min_rain else 0.0

    if not isnan(hmean):
        dh = (hmean - spec.max_hum) / spec.max_hum if hmean > spec.max_hum else \
             (spec.min_hum - hmean) / spec.min_hum if hmean < spec.min_hum else 0.0
        dh = max(0.0, dh)
    else:
        dh = 0.0

    sev: Severity = classify_severity(dmax, dmin, drmax, drmin, dh)
    atype: AlertType = alert_type_for(sev)

    # Alerts (still concise, algorithmic)
    alerts: List[Alert] = []
    if tmax > spec.max_temp:
        alerts.append(Alert(
            type=atype,
            title="High Temperature Alert",
            message=f"Temperature exceeds optimal range for {spec.name}. Risk of heat stress.",
            severity=sev
        ))
    if tmin < spec.min_temp:
        alerts.append(Alert(
            type=atype,
            title="Low Temperature Alert",
            message=f"Temperature below optimal range for {spec.name}. Risk of cold damage.",
            severity=sev
        ))
    if rmax_next > spec.max_rain:
        alerts.append(Alert(
            type=atype,
            title="High Rainfall Alert",
            message=f"Rainfall exceeds optimal range for {spec.name}. Risk of waterlogging.",
            severity=sev
        ))
    if rmin_next < spec.min_rain:
        alerts.append(Alert(
            type=atype,
            title="Low Rainfall Alert",
            message=f"Rainfall below optimal range for {spec.name}. Risk of drought stress.",
            severity=sev
        ))
    if not isnan(hmean):
        if hmean > spec.max_hum:
            alerts.append(Alert(
                type=atype,
                title="High Humidity Alert",
                message=f"Humidity above optimal range for {spec.name}. Risk of foliar disease.",
                severity=sev
            ))
        elif hmean < spec.min_hum:
            alerts.append(Alert(
                type=atype,
                title="Low Humidity Alert",
                message=f"Humidity below optimal range for {spec.name}. Risk of transpiration stress.",
                severity=sev
            ))

    # Recommendations (YAML ONLY)
    recs: List[Recommendation] = []
    ctx = {
        "severity": sev.lower(),
        "tmax": tmax, "tmin": tmin,
        "rmax": rmax_next, "rmin": rmin_next,
        "hmean": None if isnan(hmean) else hmean
    }

    # Temperature
    if tmax > spec.max_temp:
        txt = _render("temperature", "high", sev, spec.name, ctx)
        if txt:
            recs.append(Recommendation(message=txt))
    if tmin < spec.min_temp:
        txt = _render("temperature", "low", sev, spec.name, ctx)
        if txt:
            recs.append(Recommendation(message=txt))

    # Rain
    if rmax_next > spec.max_rain:
        txt = _render("rain", "high", sev, spec.name, ctx)
        if txt:
            recs.append(Recommendation(message=txt))
    if rmin_next < spec.min_rain:
        txt = _render("rain", "low", sev, spec.name, ctx)
        if txt:
            recs.append(Recommendation(message=txt))

    # Humidity
    if not isnan(hmean):
        if hmean > spec.max_hum:
            txt = _render("humidity", "high", sev, spec.name, ctx)
            if txt:
                recs.append(Recommendation(message=txt))
        elif hmean < spec.min_hum:
            txt = _render("humidity", "low", sev, spec.name, ctx)
            if txt:
                recs.append(Recommendation(message=txt))

    # ML suggestions (optional insight + YAML phrasing for disagreement)
    insights: List[str] = [
        f"Max temperature deviation: {abs(dmax) * 100:.1f}%, Min temperature deviation: {abs(dmin) * 100:.1f}%",
        f"Max rainfall deviation: {abs(drmax) * 100:.1f}%, Min rainfall deviation: {abs(drmin) * 100:.1f}%",
        f"Typical growing season: {spec.season_days[0]}–{spec.season_days[1]} days.",
    ]
    if not isnan(hmean):
        src = "hourly" if used_hourly else "daily"
        insights.append(f"Mean next-day humidity ({src}): {hmean:.0f}% (optimal {spec.min_hum:.0f}–{spec.max_hum:.0f}%).")
    if spec.notes:
        insights.append(f"Note: {spec.notes}")

    # Use daily precip sum for tomorrow as ML "rainfall" feature if present; else 0
    try:
        rainfall_ml = w.daily.precipitation_2m_sum[1]  # if you mapped Open-Meteo "precipitation_sum" to this field
    except Exception:
        try:
            rainfall_ml = w.daily.precipitation_sum[1]
        except Exception:
            rainfall_ml = 0.0

    # Assume pH if not provided elsewhere
    ph_assumed = 6.5
    if is_model_ready():
        topk = _ml_top_k(
            temp=(tmax + tmin) / 2.0,
            hum=(0.0 if isnan(hmean) else hmean),
            rainfall=rainfall_ml,
            ph=ph_assumed,
            k=3
        )
        if topk:
            # Basic numeric insight lines (not verbal templates)
            top1, top1p = topk[0][0], float(topk[0][1])
            insights.append(f"ML top prediction: {top1} ({top1p * 100:.1f}%).")
            if len(topk) > 1:
                nxt = ", ".join([f"{lbl} ({prob * 100:.0f}%)" for lbl, prob in topk[1:]])
                insights.append(f"Next: {nxt}")
            insights.append(f"Assumed soil pH = {ph_assumed} for ML features.")

            # If ML disagrees with chosen crop, add a YAML rec sentence (if available)
            if top1.lower() != normalize_name(spec.name):
                ctx_ml = {"alt1": top1, "alt1p": f"{top1p * 100:.0f}%"}
                txt = _render("ml", "disagree", "low", spec.name, ctx_ml)  # severity is not used (key 'any')
                if txt:
                    recs.append(Recommendation(message=txt))

    # De-dup recs
    seen = set()
    dedup_recs: List[Recommendation] = []
    for r in recs:
        if r.message not in seen:
            seen.add(r.message)
            dedup_recs.append(r)

    return CropAnalysis(
        overallSeverity=sev,
        alerts=alerts,
        recommendations=dedup_recs,
        insights=insights
    )
