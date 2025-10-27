# Musify — AI coding agent notes

Concise guide for AI agents working in this repo. Focus: where to change code, how the pieces run, and important patterns to preserve.

**Project context**: Academic assignment (TFU Unidad 4) implementing architecture patterns for availability, performance, security, and modifiability. This is a demo music streaming API built to showcase quality attributes through architectural tactics.

**IMPORTANT**: The application currently only simulates playback with stream URLs but does NOT actually play music. Real audio playback functionality needs to be implemented in the frontend (Angular audio player component with HTML5 `<audio>` element or Web Audio API).

## Architecture Overview

Backend is a Spring Boot monolith (packages: `auth`, `catalog`, `playback`, `spotify`) under `backend/src/main/java/com/tfu/backend/`. Frontend is an Angular app at `frontend/MusifyFront/`. A small Node.js `flaky-service/` simulates an unreliable external stream provider. Services are composed with `docker-compose.yaml` (two backend replicas) and fronted by NGINX (`frontend/MusifyFront/ops/nginx.conf`). Database seeds live in `database/`.

## Quick Commands (where to run)

- Start everything: `docker compose up --build` (repo root)
- Backend only (dev): `./backend/mvnw spring-boot:run` (from repo root)
- Frontend dev: `cd frontend/MusifyFront && npm start`
- Run demo scripts: `./scripts/demo_retries.sh`, `./scripts/demo_replication.sh`, `./scripts/demo_security.sh`, `./scripts/demo_health.sh`

## Implemented Architecture Patterns (Assignment Requirements)

### Availability Patterns (grupo 1)

1. **Retry** — `@Retry(name="streamSource")` in `PlaybackService.startPlayback` and `SpotifyService` methods. Config: `application.yaml` → `resilience4j.retry.instances.streamSource` (3 attempts, 200ms wait, exponential backoff).
2. **Circuit Breaker** — `@CircuitBreaker(name="streamSource", fallbackMethod="fallbackUrl")` in `PlaybackService`. Config: 50% failure threshold, 10s wait. Also applied to Spotify API calls.
3. **Rate Limiting** — `@RateLimiter(name="loginLimiter")` in `AuthService.login` (5 attempts/min). Config: `resilience4j.ratelimiter.instances.loginLimiter`.
4. **Health Endpoint Monitoring** — Spring Boot Actuator at `/actuator/health`. Exposed via `management.endpoints.web.exposure.include` in `application.yaml`.

### Performance Patterns (grupo 2)

_(Not yet implemented — opportunity to add caching or async patterns if required)_

**TODO**: Implement actual music playback:

- Frontend needs an audio player component using HTML5 `<audio>` element or Web Audio API
- Backend stream URLs from `flaky-service` and Spotify API preview URLs need to be wired to the player
- Consider adding audio buffering and progressive loading for better performance
- May need CORS handling for external audio sources

### Security Patterns (grupo 3)

1. **Gatekeeper** — NGINX (`frontend/MusifyFront/ops/nginx.conf`) acts as reverse proxy/gateway, offloading TLS termination and load balancing. Backend uses TLS (musify.p12).
2. **Gateway Offloading** — NGINX handles retries (`proxy_next_upstream`), health checks (passive via `max_fails`), and connection pooling (`keepalive 32`).
3. **Federated Identity** (partial) — JWT-based auth with `JwtTokenProvider` in `auth/` package. External integration with Spotify API OAuth (client credentials flow) in `SpotifyService`.

### Modifiability/Deployment Patterns (grupo 4)

1. **External Configuration Store** — Environment variables injected via `docker-compose.yaml` and `.env` file (e.g., `JWT_SECRET`, `SPOTIFY_CLIENT_ID`, `DB_HOST`). Config read from `application.yaml` with `${VAR:default}` syntax.
2. **Blue/Green-style deployment** (simulated) — Two backend replicas (`backend-app-1`, `backend-app-2`) behind NGINX allow zero-downtime restarts (stop one, update, restart; then repeat for second).

## Patterns and Conventions (project-specific)

- **Resilience**: Resilience4j is used heavily. Look for annotations: `@Retry`, `@CircuitBreaker(fallbackMethod=...)`, `@TimeLimiter`, `@RateLimiter`. Example: `PlaybackService.startPlayback` and its `fallbackUrl` method. Also in `SpotifyService` for external API calls.
- **External dependency simulation**: `flaky-service/server.js` intentionally fails (40% timeout, 20% error, 40% success) to demonstrate resilience patterns. Used by `StreamClient` in playback package.
- **Validation & security**: DTOs use Bean Validation (`@NotBlank`, `@Email`) in `LoginRequest` and other DTOs. Login rate-limiting applied via Resilience4j. JWT handling in `auth/` package (`JwtTokenProvider`, `JwtAuthFilter`).
- **Configuration**: `backend/src/main/resources/application.yaml` controls resilience settings, database, JWT, Spotify API credentials. TLS keystore (`musify.p12`) in resources — be careful when modifying.
- **Orchestration**: `docker-compose.yaml` creates two backend replicas behind NGINX; changes to load balancing or health checks should be mirrored in `frontend/MusifyFront/ops/nginx.conf`.
- **Testing/Demos**: `scripts/` directory contains shell scripts demonstrating each pattern (retries, replication, security, health). These scripts are used to validate pattern implementation and are mentioned in assignment deliverables.

## Integration Pointers (where code links together)

- **Playback → flaky-service**: `backend` calls `flaky-service` via `StreamClient` (see playback package). Adjust resilience settings in `application.yaml` when changing call behavior.
- **Backend → Spotify API**: `SpotifyService` calls Spotify Web API for track search. Uses client credentials flow with token refresh (`@PostConstruct` + `@Scheduled`). Protected by `@Retry` and `@CircuitBreaker`.
- **Frontend REST contract**: Frontend calls backend REST endpoints exposed by controllers under `backend/.../controller`. Use `scripts/` to find example curl commands.
- **Database**: PostgreSQL schema and test data in `database/` (e.g., `03-auth-test-data.sql`). Manual SQL migrations; no framework like Flyway/Liquibase.
- **NGINX → Backend replicas**: NGINX load-balances to `backend-app-1:8443` and `backend-app-2:8443`. Passive health checks via `max_fails=3 fail_timeout=10s`. Automatic retry on error/timeout via `proxy_next_upstream`.

## Developer Guidance for AI Edits

- **Preserve resilience annotations** and fallback signatures when refactoring playback/stream code. Fallback methods must match signature expected by Resilience4j (same return type, additional `Throwable` parameter).
- **When changing REST endpoints**, update frontend references in `frontend/MusifyFront/src/app/*` and `scripts/` demo scripts. API is documented via OpenAPI/Swagger at `/swagger-ui.html`.
- **Small, safe edits**: run `./backend/mvnw -q -DskipTests=true package` to validate compile. Prefer unit tests for behavior changes; tests under `backend/src/test/java`.
- **For integration or platform changes**, use `docker compose up --build` and demo scripts to validate behavior (replication, retries, health).
- **Pattern implementation**: When adding new patterns (e.g., caching, async), ensure demo scripts exist in `scripts/` to prove the pattern works. Update this file and the README.md to document the pattern.

## Key Files (where to look/edit)

- `backend/src/main/java/com/tfu/backend/` — core code (auth, catalog, playback, spotify packages)
- `backend/src/main/resources/application.yaml` — resilience configs, database, JWT, Spotify API
- `flaky-service/server.js` — behavior of unreliable external service (failure rates/timeouts)
- `frontend/MusifyFront/ops/nginx.conf` — NGINX load-balancer, health checks, retry logic
- `docker-compose.yaml` — service topology (2 backend replicas, flaky-service, NGINX, postgres)
- `scripts/` — demo scripts showing each pattern in action (assignment deliverables)
- `README.md` — high-level architecture and tactics documentation

## Assignment Deliverables Checklist

**Parte 1** (Documentation):

- UML diagrams showing pattern application (to be added externally)
- Pattern justifications (see README.md "Tácticas de Arquitectura" section)
- Tactics relationship (patterns map to availability, security, modifiability tactics)

**Parte 2** (Implementation):

- ✅ REST API testable via curl/Postman (see `scripts/` and `Musify_API_Testing_Guide.md`)
- ✅ Docker deployment via `docker-compose.yaml`
- ✅ Demo scripts proving pattern effectiveness (`scripts/demo_*.sh`)

If anything above is unclear or you want more detail (specific resilience config values, controller signatures, fallback method examples, UML diagram generation), tell me which area to expand and I will iterate.
