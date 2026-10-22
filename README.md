# EcoPulse — Sustainable Resource Optimization Platform

EcoPulse helps facilities monitor energy, water, and waste consumption, detect excessive usage through rule-based alerts, and track actionable sustainability recommendations.

## Main features

- Facility CRUD (persisted in MySQL)
- Resource meters linked to facilities (ENERGY / WATER / WASTE)
- Manual consumption readings (period amounts, not cumulative dial readings)
- Automatic threshold alerts and linked recommendations
- Dashboard with live API-driven charts and summaries
- Date-range sustainability reports with optional CSV export
- React + Vite frontend integrated via REST
- Docker Compose stack (MySQL + backend + frontend)
- Jenkins pipeline skeleton and Spring Boot Actuator health

## Technology stack

| Layer | Tools |
|--------|--------|
| Backend | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Validation, Actuator |
| Database | MySQL (H2 in-memory for automated tests) |
| Frontend | React, Vite, Axios, Recharts, React Router |
| DevOps | Maven, Docker, Docker Compose, Jenkinsfile |

## Architecture

Layered package `com.gen.ecopulse`:

- **controller** — REST endpoints
- **service** — business logic (readings, alerts, reports)
- **repository** — JPA data access
- **entity** — database tables
- **dto** — API request/response shapes (avoids JSON recursion)
- **exception** — `@RestControllerAdvice` error format
- **config** — CORS for the Vite dev server

### Entity relationships

- `Facility` 1 — * `ResourceMeter`
- `ResourceMeter` 1 — * `ResourceReading`
- `ResourceReading` 0..1 — `Alert` (unique per reading)
- `Alert` 1 — 1 `Recommendation`

### Alert severity rules

When a reading is saved, if `readingValue > meter.alertThreshold`:

| Condition | Severity |
|-----------|----------|
| Value ≤ 125% of threshold | `WARNING` |
| Value > 125% of threshold | `HIGH` |

At most **one alert per reading** (`reading_id` is unique on alerts). Re-processing the same reading does not create duplicates.

Recommendations are generated automatically from the meter’s resource type (energy / water / waste action text).

## API overview

Base URL: `http://localhost:8080`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/hello` | Health greeting |
| POST/GET/PUT/DELETE | `/api/facilities` | Facility CRUD |
| POST/GET/PUT/DELETE | `/api/meters` | Meter CRUD (`?facilityId=&resourceType=`) |
| POST/GET | `/api/readings` | Readings (`?facilityId=&resourceType=&start=&end=`) |
| GET | `/api/readings/meter/{id}` | Readings by meter |
| GET | `/api/alerts`, `/api/alerts/active` | Alerts |
| PUT | `/api/alerts/{id}/status` | Body: `{ "status": "OPEN" }` |
| GET | `/api/recommendations` | Recommendations |
| PUT | `/api/recommendations/{id}/status` | Body: `{ "status": "COMPLETED" }` |
| GET | `/api/dashboard/summary` | Dashboard data |
| GET | `/api/reports/summary` | Report totals (`startDate`, `endDate`, `facilityId`) |
| GET | `/api/reports/trends` | Daily trends |
| GET | `/api/reports/export` | CSV download |
| GET | `/actuator/health` | Actuator health |

### Sample: create facility

```bash
curl -X POST http://localhost:8080/api/facilities \
  -H "Content-Type: application/json" \
  -d '{"name":"Main Campus","location":"Pune","type":"College"}'
```

### Sample: record reading (triggers alert if above threshold)

```bash
curl -X POST http://localhost:8080/api/readings \
  -H "Content-Type: application/json" \
  -d '{"meterId":1,"readingValue":120,"unit":"kWh","notes":"Weekly total"}'
```

## Prerequisites

- JDK 21
- Maven (or use `./mvnw`)
- MySQL 8 with database `ecopulse`
- Node.js 20+ (frontend)

## Configure MySQL (local)

1. Create database: `CREATE DATABASE ecopulse;`
2. Copy `.env.example` to `.env` or set environment variables:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/ecopulse"
$env:DB_USERNAME="your_user"
$env:DB_PASSWORD="your_password"
```

Alternatively copy `application-local.properties.example` to `src/main/resources/application-local.properties` and set `spring.datasource.password` (file is gitignored if placed as documented in `.gitignore`).

> **Note:** Database passwords are no longer stored in `application.properties`. Set `DB_PASSWORD` before running locally.

## Run the backend

```powershell
cd ecopulse
$env:DB_PASSWORD="your_password"
.\mvnw.cmd spring-boot:run
```

Verify: [http://localhost:8080/api/hello](http://localhost:8080/api/hello)

## Run the frontend

```powershell
cd frontend
copy .env.example .env
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) — API base URL is `VITE_API_BASE_URL` in `.env`.

## Run tests

Tests use in-memory H2 (see `src/test/resources/application.properties`):

```powershell
.\mvnw.cmd clean verify
```

## Docker Compose

```powershell
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| MySQL | localhost:3307 (user `ecopulse` / password `ecopulse`) |

## Jenkins pipeline

`Jenkinsfile` stages: checkout → Maven verify → Docker image build → frontend `npm run build`. Optional registry push runs only when `DOCKER_REGISTRY` is configured.

## Demo workflow (interview)

1. Add a **facility** (Facilities page).
2. Register **meters** with thresholds (e.g. energy 50 kWh).
3. Record a **reading** above threshold → check **Alerts** and **Recommendations**.
4. Open **Dashboard** for charts and totals.
5. Generate a **report** for a date range and download CSV.

## Screenshots

_Add screenshots of Dashboard, Facilities, and Alerts pages after running the app._

## Known limitations & future work

- No authentication/RBAC (MVP scope)
- Readings are period totals, not cumulative meter deltas
- No email/SMS notifications for alerts
- AWS EC2 deployment is optional follow-up

## License

Academic / portfolio use — see repository owner.
