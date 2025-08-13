# ml_infer.py
from typing import Optional, Dict, List
import numpy as np
from load_model import model, classes

# minimal synonyms you actually use
CROP_MAP = {
    "Rice": "rice",
    "Wheat": "wheat",
    "Maize": "maize",
    "Cotton": "cotton",
    "Coconut": "coconut",
    "Grapes": "grapes",
    "Lentil (Rabi)": "lentil",  # dataset has 'lentil'
    # add others as needed
}

def severity_from_prob(p: float) -> str:
    # Suitability probability → risk severity (configurable)
    if p >= 0.65: return "LOW"
    if p >= 0.45: return "MEDIUM"
    return "HIGH"

def crop_prob(crop_name: str, features_row: List[float]) -> Optional[float]:
    """
    features_row = [temperature, humidity, ph, rainfall]
    returns P(suitable_for_that_crop) or None if unsupported
    """
    ml_name = CROP_MAP.get(crop_name, crop_name).lower()
    if ml_name not in classes:
        return None
    proba = model.predict_proba([features_row])[0]
    idx = classes.index(ml_name)
    return float(proba[idx])
