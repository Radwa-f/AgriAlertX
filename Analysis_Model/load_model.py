# load_model.py
import json, logging
from pathlib import Path
import joblib

LOG = logging.getLogger("model_loader")

BASE_DIR  = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "crop_suitability_lr.pkl"
META_PATH  = BASE_DIR / "crop_model_meta.json"

_MODEL = None
_META  = {"metrics": {"test_f1_macro": 0.0}}

def load_model():
    """Load model + meta once at startup. If it fails, keep service alive in rule-only mode."""
    global _MODEL, _META
    try:
        LOG.info(f"Loading model from {MODEL_PATH}")
        _MODEL = joblib.load(MODEL_PATH)
        LOG.info("Model loaded.")
        try:
            with META_PATH.open("r") as f:
                _META = json.load(f)
        except Exception as e:
            LOG.warning(f"No/invalid meta file {META_PATH}: {e}")
            _META = {"metrics": {"test_f1_macro": 0.0}}
    except Exception as e:
        LOG.error(f"Failed to load model: {e}")
        _MODEL = None
        _META  = {"error": str(e), "metrics": {"test_f1_macro": 0.0}}

def get_model():
    return _MODEL

def get_meta():
    return _META

def is_model_ready(threshold: float = 0.70) -> bool:
    if _MODEL is None:
        return False
    score = float(_META.get("metrics", {}).get("test_f1_macro", 0.0))
    return score >= threshold
