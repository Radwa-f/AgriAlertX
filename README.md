# 🌱🌦️ AgriAlertX — Agricultural Risk Alerts & Recommendations

<div align="center">
  <picture>
    <source srcset="https://github.com/user-attachments/assets/911c92e3-e9f8-4aaa-85ee-3f0a548ea628" media="(prefers-color-scheme: dark)">
    <img src="https://github.com/user-attachments/assets/c2c68b06-0e01-4f55-b577-07cb76639166" width="260" alt="AgriAlertX Logo">
  </picture>
</div>

AgriAlertX delivers real-time crop risk monitoring and actionable recommendations by fusing weather forecasts, crop tolerance profiles, and a lightweight ML model. It ships as a full stack: **Android & iOS apps**, **Next.js web**, a **Spring Boot API**, a **Python FastAPI ML microservice**, and a **Flask chatbot**—all wired through REST.

---

## Contents

- [Overview](#overview)
- [Features](#features)
- [Monorepo Layout](#monorepo-layout)
- [Architecture](#architecture)
- [APIs](#apis)
- [Metrics (Model Quality)](#metrics-model-quality)
- [Quick Start (Docker)](#quick-start-docker)
- [Local Dev (Per Service)](#local-dev-per-service)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [Testing & QA](#testing--qa)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

- **Goal:** Warn farmers before weather-driven crop stress (heat/cold, drought/flood risk, humidity disease pressure) and suggest practical mitigations.
- **How:** Fetch hourly/daily forecast → normalize → rule checks vs. crop envelopes → **ML top-k crop suitability** → generate crop-specific recommendations.
- **Why hybrid:** Rules offer **explainability**; ML adds **probabilistic lift** and alternative crop suggestions when conditions fit other crops better.

---

## Features

- 🔔 Multi-platform clients (Android, iOS, Web) with native notifications
- 🌤️ Weather ingestion (Open-Meteo—no API key required)
- 🌾 Crop envelopes (min/max temp/rain/RH) + GDD driven timing
- 🤖 FastAPI ML microservice (scikit-learn pipeline) with graceful fallbacks
- 🧠 Flask chatbot for agronomic Q&A around alerts
- 🐳 One-command Docker Compose for the full stack

---

## Monorepo Layout

```
AgriAlertX/
├─ android-app/                    # Kotlin client
├─ ios-app/                        # Swift client
├─ web-client/                     # Next.js (TypeScript + Tailwind)
├─ springboot_backend/             # API gateway & orchestration (Java/Spring Boot)
├─ chatbot/                        # Flask chatbot service
├─ Analysis_Model/                 # FastAPI ML service (predict + recommendations)
├─ docker-compose.yml
└─ README.md
```

---

## Architecture

![AgriAlertX Architecture](https://github.com/user-attachments/assets/2a9d6b42-c592-4865-ac46-c5bed44c06a0)

- **Clients:** Android, iOS, and Web call the **Spring Boot API**.
- **Spring Boot API:** Fetches weather (Open-Meteo), applies safety checks, calls **ML service**, merges outputs, returns alerts + recommendations.
- **FastAPI ML:** Multinomial Logistic Regression (StandardScaler + LogisticRegression). Endpoints: `/health`, `/ml/status`, `/analyze`.
- **Flask Chatbot:** Q&A and guidance around alerts and agronomic practices.

---

## APIs

### Spring Boot 
**POST** `/api/crops/weather-analysis/auto`
```json
{
  "latitude": 34.02,
  "longitude": -6.83,
  "cropNames": ["Wheat", "Maize", "Coffee"]
}
```

**Response (excerpt)**
```json
{
  "cropAnalyses": {
    "Wheat": {
      "overallSeverity": "MEDIUM",
      "alerts": [{ "title": "High Temperature Alert", "severity": "MEDIUM", "message": "..." }],
      "recommendations": [{ "message": "..." }],
      "insights": [
        "Max temperature deviation: 12.0%, Min temperature deviation: 0.0%",
        "ML top prediction: muskmelon (53.2%).",
        "Assumed soil pH = 6.5 for ML features."
      ]
    }
  },
  "errors": []
}
```

### FastAPI ML 
- `GET /health` → `{"ok": true}`
- `GET /ml/status` → `{ loaded, ok, meta: { features, classes, metrics } }`
- `POST /analyze` → accepts weather slices + crops, returns per-crop analysis.

### Flask Chatbot 
- `POST /chat` → `{ "message": "..." }` → chatbot reply.

---

## Metrics (Model Quality)

Hold-out and CV on crop recommendation dataset (**temperature, humidity, rainfall, pH**; graceful fallback when pH missing).

| Setting            | Accuracy | Macro-F1 |
| ------------------ | :------: | :------: |
| **with pH**        |  **0.793**  | **0.787** |
| **assumed pH=6.5** |   0.732  |  0.696   |
| **5-fold CV**      | 0.782±0.010 | 0.774±0.012 |

> Reproduce:
> `python Analysis_Model/evaluate.py --data Crop_recommendation.csv --model crop_suitability_lr.pkl --assumed_ph 6.5 --out metrics.json`

---

## Quick Start (Docker)

Create a root `.env` (or inject via your orchestrator):

```env
# Database
MYSQL_ROOT_PASSWORD=secret
MYSQL_DATABASE=agrialert
MYSQL_USER=agrialert
MYSQL_PASSWORD=agripass

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/agrialert
SPRING_DATASOURCE_USERNAME=agrialert
SPRING_DATASOURCE_PASSWORD=agripass
ML_BASE_URL=http://ml:8001

# Web client
NEXT_PUBLIC_API_BASE=http://localhost:8087
```

`docker-compose.yml`:

```yaml
version: "3.9"
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports: ["3307:3306"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-u${MYSQL_USER}", "-p${MYSQL_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 10

  ml:
    build: ./Analysis_Model
    command: uvicorn main:app --host 0.0.0.0 --port 8001
    ports: ["8001:8001"]
    depends_on: [mysql]
    environment:
      PYTHONUNBUFFERED: "1"

  flask:
    build: ./chatbot
    ports: ["5000:5000"]
    depends_on: [mysql]

  spring:
    build: ./springboot_backend
    ports: ["8087:8087"]
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      ML_BASE_URL: ${ML_BASE_URL}
    depends_on:
      mysql:
        condition: service_healthy
      ml:
        condition: service_started

  web:
    build: ./web-client
    ports: ["3000:3000"]
    environment:
      NEXT_PUBLIC_API_BASE: ${NEXT_PUBLIC_API_BASE}

networks:
  default:
    driver: bridge
```

Run everything:

```bash
docker compose up --build
```

- Spring Boot: http://localhost:8087  
- ML service: http://localhost:8001/ml/status  
- Web: http://localhost:3000  
- Chatbot: http://localhost:5000  

---

## Local Dev (Per Service)

### ML Service (FastAPI)
```bash
cd Analysis_Model
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
# Optional: retrain
python train.py --data Crop_recommendation.csv --out crop_suitability_lr.pkl
# Serve
uvicorn main:app --reload --port 8001
```

### Spring Boot
```bash
cd springboot_backend
# set ML_BASE_URL=http://localhost:8001
./mvnw spring-boot:run
```

### Web (Next.js)
```bash
cd web-client
npm install
npm run dev
```

### Android / iOS
- Update base URLs to point at your Spring Boot instance.
- Build & run from Android Studio / Xcode.
- Uses **native** notifications on both platforms.

### Chatbot (Flask)
```bash
cd chatbot
pip install -r requirements.txt
python chat.py  # adjust host/port as needed
```

---

## Configuration

- **Spring Boot**: `application.yml` reads `ML_BASE_URL` → FastAPI `/analyze`.
- **Weather**: Open-Meteo (no API key). Ensure outbound network from Spring container.
- **CORS**: Allow your mobile/web origins on the Spring Boot layer.
- **Secrets**: Inject via env; never commit credentials.

---

## Environment Variables

| Variable | Service | Default / Example | Purpose |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Spring | `jdbc:mysql://mysql:3306/agrialert` | DB connection |
| `SPRING_DATASOURCE_USERNAME` | Spring | `agrialert` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | Spring | `agripass` | DB pass |
| `ML_BASE_URL` | Spring | `http://ml:8001` | FastAPI endpoint |
| `NEXT_PUBLIC_API_BASE` | Web | `http://localhost:8087` | API base for frontend |
| `MYSQL_*` | MySQL | see `.env` | DB bootstrap |

---

## Testing & QA

- **Unit / Integration**
  - Spring: `./mvnw test`
  - ML/Chatbot: `pytest`
  - Web: `npm test`
- **Contracts**: Test `/api/crops/weather-analysis/auto` and `/ml/status` in CI (schemas/payloads).
- **Static Analysis**: SonarQube (Java/TS/Python scanning).
- **Health**: `/health` (ML), Spring Actuator if enabled.

---

## Troubleshooting

- **`ml.base-url` not resolved** → Set `ML_BASE_URL` env or define `ml.base-url` in `application.yml`.
- **Pickle / scikit-learn mismatch** → Align versions or retrain:
  ```bash
  pip install scikit-learn==1.5.2
  python Analysis_Model/train.py --data Crop_recommendation.csv --out crop_suitability_lr.pkl
  ```
- **CORS errors** → Configure Spring CORS to include web/emulator origins.
- **No humidity in weather** → Engine falls back gracefully; note appears in `insights`.

---

## Demo Videos

<div align="center">

[▶ Android Demo](https://github.com/user-attachments/assets/92a0e54f-6e8f-4512-a3e2-a383761b4b60) 
[▶ iOS Demo](https://github.com/user-attachments/assets/5f3db54d-9df8-498b-92f1-78c4bf20cf24)   
[▶ Web Demo](https://github.com/user-attachments/assets/4f79c1e3-d094-4bbc-bad5-647c9bf8379f)

</div>

---

## Roadmap

- Add soil (pH/EC) from sensors or farmer input in the apps.
- Expand ML features and retrain with regional datasets.
- On-device caching & offline recommendations.
- IoT sensor integration for micro-climate accuracy.

---

## Contributing

We welcome PRs! Please:

1. Fork the repo, create a feature branch.
2. Add tests where relevant.
3. Run linters/formatters (`mvn fmt`, `flake8/black`, `eslint`).
4. Open a PR with a clear description and screenshots if UI changes.

**Contributors**
- Fattouhi Radwa — [GitHub](https://github.com/Radwa-f)  
- Douidy Sifeddine — [GitHub](https://github.com/SaifeddineDouidy)  
- Mohamed Lachgar — [ResearchGate](https://www.researchgate.net/profile/Mohamed-Lachgar)

---

## License

See `LICENSE` for details.
