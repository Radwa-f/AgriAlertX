# crops_store.py
import csv
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Tuple

@dataclass(frozen=True)
class CropSpec:
    name: str
    min_temp: float
    max_temp: float
    min_rain: float
    max_rain: float
    min_hum: float
    max_hum: float
    season_days: Tuple[int, int]  # (min, max)
    notes: str

def _parse_range(s: str) -> Tuple[int, int]:
    s = s.strip()
    if "-" in s:
        a, b = s.split("-", 1)
        return int(a), int(b)
    v = int(s)
    return v, v

def normalize_name(n: str) -> str:
    return " ".join(n.strip().lower().split())

def load_crops(csv_path: str = "crops.csv") -> Dict[str, CropSpec]:
    path = Path(csv_path)
    if not path.exists():
        raise FileNotFoundError(f"Cannot find {csv_path} (cwd={Path.cwd()})")

    out: Dict[str, CropSpec] = {}
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row["name"].strip()
            spec = CropSpec(
                name=name,
                min_temp=float(row["minTemp"]),
                max_temp=float(row["maxTemp"]),
                min_rain=float(row["minRain"]),
                max_rain=float(row["maxRain"]),
                min_hum=float(row["minHumidity"]),
                max_hum=float(row["maxHumidity"]),
                season_days=_parse_range(row["growing_season_days"]),
                notes=row.get("notes", "").strip(),
            )
            out[normalize_name(name)] = spec
    return out
