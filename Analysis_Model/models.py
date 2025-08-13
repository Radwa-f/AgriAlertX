# models.py
from pydantic import BaseModel
from typing import List, Dict, Optional, Literal

Severity = Literal["LOW", "MEDIUM", "HIGH"]
AlertType = Literal["ERROR", "WARNING", "INFO"]

class Recommendation(BaseModel):
    message: str

class Alert(BaseModel):
    type: AlertType
    title: str
    message: str
    severity: Severity

class CropAnalysis(BaseModel):
    overallSeverity: Severity
    alerts: List[Alert]
    recommendations: List[Recommendation]
    insights: List[str]

class Daily(BaseModel):
    temperature_2m_max: List[float]
    temperature_2m_min: List[float]
    precipitation_sum: List[float]
    # Optional daily humidity summaries if you add them in Spring later
    relative_humidity_2m_mean: Optional[List[float]] = None
    relative_humidity_2m_min: Optional[List[float]] = None
    relative_humidity_2m_max: Optional[List[float]] = None

class Hourly(BaseModel):
    precipitation: List[float]
    time: List[str]
    # Optional hourly humidity from Spring (recommended)
    relative_humidity_2m: Optional[List[float]] = None

class WeatherResponse(BaseModel):
    daily: Daily
    hourly: Hourly

class AnalyzeRequest(BaseModel):
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    cropNames: List[str]
    weather: Optional[WeatherResponse] = None

class AnalyzeResponse(BaseModel):
    cropAnalyses: Dict[str, CropAnalysis]
    errors: List[str] = []
