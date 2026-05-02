# LogWatch - Log Analysis and Alert Generation System

Application to centralize, store, query, and analyze logs produced by different services, with automatic alert generation when anomalous patterns are detected using configurable YAML rules.

## Project Structure

```
logwatch/
├── logwatch-backend/       # HTTP service (Spring Boot + PostgreSQL)
├── logwatch-desktop/       # Desktop client (JavaFX)
├── sample-data/            # Sample data for import
├── docker-compose.yml      # PostgreSQL in a container
└── README.md
```

## Prerequisites

- **Java 17** or higher (JDK)
- **Maven 3.8+**
- **PostgreSQL 14+** (or Docker to run it in a container)
- **Docker** and **Docker Compose** (optional, for the database)

## Getting Started

### 1. Database

**Option A: Docker Compose (recommended)**

```bash
docker-compose up -d
```

This starts PostgreSQL on `localhost:5432` with database `logwatch`.

**Option B: Local PostgreSQL**

Create the database manually:

```sql
CREATE DATABASE logwatch;
CREATE USER logwatch WITH PASSWORD 'logwatch';
GRANT ALL PRIVILEGES ON DATABASE logwatch TO logwatch;
```

### 2. Backend (Spring Boot)

```bash
cd logwatch-backend
mvn clean install
mvn spring-boot:run
```

The server starts at `http://localhost:8080`. Flyway will run migrations automatically.

### 3. Desktop Client (JavaFX)

```bash
cd logwatch-desktop
mvn clean javafx:run
```

## REST API

All requests require the header `X-API-Key: logwatch-dev-key` (configurable).

### Events

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/events` | Ingest a JSON event; returns 201 with `id` |
| GET | `/api/events` | Query events with filters; paginated (`from`, `to`, `eventType`, `user`, `ip`, `status`, `source`, `severity`) |
| GET | `/api/events/{id}` | Event detail; includes `rawPayload` |
| GET | `/api/events/export/csv` | Export to CSV; same filters apply |
| GET | `/api/events/export/json` | Export to JSON; same filters apply |

### Rules

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/rules` | List rules; includes state and severity |
| POST | `/api/rules` | Create rule; validates YAML |
| PUT | `/api/rules/{id}` | Update rule; full replacement |
| PATCH | `/api/rules/{id}/toggle` | Enable/disable; quick toggle |

### Alerts

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/alerts` | List alerts; filters: `from`, `to`, `ruleId`, `severity`; paginated |
| GET | `/api/alerts/{id}` | Detail with evidence; includes events that triggered the alert |

### Import

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/import/csv` | Import CSV; multipart, field `file` |
| POST | `/api/import/json` | Import JSON; multipart, field `file` |

## YAML Rule Format

```yaml
match:
  eventType: AUTH_FAILURE
  httpStatus: [401, 403]
groupBy: ip
windowSeconds: 300
threshold: 10
cooldownSeconds: 600
description: "More than 10 login failures from the same IP in 5 minutes"
```

### Rule fields:

- **match.eventType**: Event type to filter
- **match.httpStatus**: HTTP status code(s) to filter (single value or list)
- **groupBy**: Grouping field (`ip`, `user`, `source`)
- **windowSeconds**: Time window in seconds
- **threshold**: Detection threshold (minimum number of matches)
- **cooldownSeconds**: Minimum time between repeated alerts
- **description**: Human-readable alert description

## Ingest Example via cURL

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: logwatch-dev-key" \
  -d '{
    "timestamp": "2026-04-24T10:00:00Z",
    "source": "auth-service",
    "eventType": "AUTH_FAILURE",
    "severity": "WARNING",
    "userName": "admin",
    "sourceIp": "192.168.1.100",
    "httpStatus": 401,
    "message": "Failed authentication attempt"
  }'
```

## Configuration

Configuration is externalized in `logwatch-backend/src/main/resources/application.yml`:

- **Database**: `spring.datasource.*`
- **API Key**: `logwatch.api.key` and `logwatch.api.key-enabled`
- **Rule engine**: `logwatch.rule-engine.interval-ms` (evaluation interval in ms)

## Technologies

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.2, Java 17 |
| Persistence | PostgreSQL + JPA/Hibernate |
| Migrations | Flyway |
| Rule engine | SnakeYAML |
| Serialization | Jackson |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Logging | SLF4J + Logback |
| Desktop client | JavaFX 21 |
| Import/Export | OpenCSV, Jackson |

## Interactive API Documentation

With the backend running, open:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
