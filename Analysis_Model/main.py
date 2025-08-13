# main.py
from fastapi import FastAPI, HTTPException
from load_model import load_model, get_model, get_meta, is_model_ready
from models import AnalyzeRequest, AnalyzeResponse
from engine import analyze_one

app = FastAPI(title="Crop Analysis Engine")

@app.on_event("startup")
def _startup():
    load_model()

@app.get("/ml/status")
def ml_status():
    return {
        "loaded": get_model() is not None,
        "ok": is_model_ready(),          # uses threshold 0.70
        "meta": get_meta(),              # if load failed, meta.error will explain why
    }

@app.post("/ml/reload")
def ml_reload():
    load_model()
    return ml_status()

@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(req: AnalyzeRequest):
    if not req.cropNames:
        raise HTTPException(400, "cropNames is required")
    if req.weather is None:
        raise HTTPException(400, "Include 'weather' in the body for now.")

    analyses, errors = {}, []
    for name in req.cropNames:
        try:
            analyses[name] = analyze_one(name, req.weather)
        except Exception as e:
            errors.append(f"{name}: {e}")
    return AnalyzeResponse(cropAnalyses=analyses, errors=errors)
