# Musify AI Coding Agent Instructions

## Big Picture Architecture

- Musify is a demo music streaming platform focused on availability, performance, and security tactics.
- **Backend**: Spring Boot app (`backend/`) with modules for auth, catalog, config, playback. Key entry: `BackendApplication.java`.
- **Flaky Service**: Node.js service (`flaky-service/`) simulates unreliable external dependencies for retry/circuit breaker demos.
- **Frontend**: Angular app (`frontend/MusifyFront/`).
- **NGINX**: Configured as load balancer (`frontend/MusifyFront/ops/nginx.conf`).
- **Orchestration**: All services run via Docker Compose (`docker-compose.yaml`).

## Developer Workflows

- **Build & Run All Services**: `docker compose up --build` (from repo root)
- **Backend Only**: Use Maven wrapper: `./backend/mvnw spring-boot:run`
- **Demo Scripts**: Use scripts in `scripts/` to simulate scenarios:
  - Replication: `./scripts/demo_replication.sh`
  - Retries/Circuit Breaker: `./scripts/demo_retries.sh`
  - Security: `./scripts/demo_security.sh`
  - Health: `./scripts/demo_health.sh`
- **Database**: SQL seed files in `database/` (see `01-init.sql`, `02-seed.sql`, etc.)

## Project-Specific Patterns & Conventions

- **Resilience4j**: Used for retries, circuit breaker, rate limiting in backend. See annotations in `playback/` and `auth/` modules.
- **Validation**: User input validated with annotations (`@Email`, `@NotBlank`).
- **Rate Limiting**: Login endpoint limited via `@RateLimiter`.
- **Fallbacks**: Methods use fallback logic for degraded service (see playback service).
- **Replication**: Multiple backend instances managed by Docker Compose and NGINX.
- **Service Boundaries**: Each major feature (auth, catalog, playback) is a separate package under `backend/src/main/java/com/tfu/backend/`.

## Integration Points

- **External Service**: `flaky-service` is called from backend playback logic.
- **Frontend-Backend**: Communicate via REST endpoints exposed by backend.
- **NGINX**: Handles load balancing and retry logic for backend services.

## Key Files & Directories

- `backend/src/main/java/com/tfu/backend/` — Main backend code
- `backend/src/main/resources/application.yaml` — Backend config
- `flaky-service/server.js` — Unreliable service simulation
- `frontend/MusifyFront/ops/nginx.conf` — NGINX config
- `docker-compose.yaml` — Service orchestration
- `scripts/` — Demo/test scripts
- `database/` — SQL setup/seed files

---

For questions or unclear conventions, review `README.md` or ask for clarification. Update this file as new patterns emerge.
