# Musify AI Coding Agent Instructions

## Big Picture Architecture

- Musify is a demo music streaming platform focused on availability, performance, and security tactics.
- **Backend**: Spring Boot app (`backend/`) with modules for auth, catalog, config, playback. Key entry: `BackendApplication.java`.
- **Flaky Service**: Node.js service (`flaky-service/`) simulates unreliable external dependencies for retry/circuit breaker demos.
- **Frontend**: Angular app (`frontend/MusifyFront/`).
- **NGINX**: Configured as load balancer (`frontend/MusifyFront/ops/nginx.conf`).
- **Orchestration**: All services run via Docker Compose (`docker-compose.yaml`) with two replicated backend instances.
- **Database**: PostgreSQL database with seed data for music catalog and user authentication.

## Developer Workflows

- **Build & Run All Services**: `docker compose up --build` (from repo root)
- **Backend Only**: Use Maven wrapper: `./backend/mvnw spring-boot:run`
- **Demo Scripts**: Use scripts in `scripts/` to simulate scenarios:
  - Replication: `./scripts/demo_replication.sh` - Shows load balancing between instances
  - Retries/Circuit Breaker: `./scripts/demo_retries.sh` - Demonstrates resilience patterns with flaky service
  - Security: `./scripts/demo_security.sh` - Demonstrates input validation and rate limiting
  - Health: `./scripts/demo_health.sh` - Shows health endpoint and API documentation
- **Database**: SQL seed files in `database/` (see `01-init.sql`, `02-seed.sql`, `03-auth-test-data.sql`)
- **Frontend Development**: Run `npm start` in `frontend/MusifyFront/` directory (or use Docker)

## Project-Specific Patterns & Conventions

- **Resilience4j**: Used for fault tolerance patterns with specific annotations:
  - `@Retry(name = "streamSource")` - Retries failed operations with exponential backoff (see `PlaybackService.startPlayback`)
  - `@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")` - Prevents cascading failures
  - `@TimeLimiter(name = "streamSource")` - Sets max wait times for operations
  - `@RateLimiter(name = "loginLimiter")` - Limits login attempts to prevent brute force attacks
- **Validation**: User input validated with Bean Validation annotations (`@Email`, `@NotBlank`) in request DTOs
- **Fallbacks**: Methods include fallback logic (see `PlaybackService.fallbackUrl`) for graceful degradation
- **Replication**: Multiple backend instances managed via Docker Compose and NGINX load balancer
- **Service Boundaries**: Each feature area is a separate package with clear responsibility:
  - `auth/` - Authentication, user management, JWT handling
  - `catalog/` - Music catalog and track information
  - `playback/` - Stream URLs, playback history, player state

## Integration Points

- **External Service**: `flaky-service` is called from `StreamClient` in the playback service
  - Intentionally unreliable (40% timeout, 20% error, 40% success)
  - Used to demonstrate resilience patterns
- **Frontend-Backend**: Communicate via REST endpoints exposed by backend
- **NGINX**: Handles load balancing with passive health checks and automatic retry logic
  - See `proxy_next_upstream` settings in `nginx.conf` for retry behavior
- **Database**: PostgreSQL with JPA/Hibernate for data persistence

## Key Files & Directories

- `backend/src/main/java/com/tfu/backend/` — Main backend code
- `backend/src/main/resources/application.yaml` — Backend config with resilience4j settings
- `flaky-service/server.js` — Unreliable service simulation
- `frontend/MusifyFront/ops/nginx.conf` — Load balancer and retry config
- `docker-compose.yaml` — Service orchestration with replicated backends
- `scripts/` — Demo/test scripts for showcasing architectural tactics
- `database/` — SQL setup/seed files

## Common Tasks & Debugging

- **Testing REST Endpoints**: Demo scripts in `scripts/` provide examples of API calls
- **Simulating Failures**: `flaky-service` can be used to test resilience patterns
- **Viewing Logs**: Use `docker logs backend-app-1` to see application logs
- **API Documentation**: Available at `/swagger-ui.html` when running
- **Health Checks**: Available at `/actuator/health` endpoint

---

For questions or unclear conventions, review `README.md` or ask for clarification. Update this file as new patterns emerge.
