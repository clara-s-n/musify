# 🏗️ Patrones de Arquitectura - Guía Completa

> **11 patrones arquitectónicos implementados en Musify para disponibilidad, rendimiento, seguridad y modificabilidad**

## 📊 Resumen Ejecutivo

Este documento describe los **11 patrones de arquitectura** implementados en la aplicación Musify como parte del Trabajo Final de Unidad 4. La implementación cumple y excede los requisitos mínimos de 7 patrones.

### Patrones Implementados por Categoría

| Categoría | Patrón | Estado | Script Demo |
|-----------|---------|---------|-------------|
| **Disponibilidad (4)** | Retry | ✅ Implementado | `demo_retries.sh` |
| | Circuit Breaker | ✅ Implementado | `demo_circuit_breaker.sh` |
| | Rate Limiting | ✅ Implementado | `demo_security.sh` |
| | Health Endpoint Monitoring | ✅ Implementado | `demo_health.sh` |
| **Rendimiento (2)** | Cache-Aside | ✅ Implementado | `demo_performance.sh` |
| | Asynchronous Request-Reply | ✅ Implementado | `demo_performance.sh` |
| **Seguridad (3)** | Gatekeeper | ✅ Implementado | `demo_security.sh` |
| | Gateway Offloading | ✅ Implementado | `demo_security.sh` |
| | Federated Identity | ✅ Implementado | `demo_security.sh` |
| **Modificabilidad (2)** | External Configuration Store | ✅ Implementado | `demo_replication.sh` |
| | Blue/Green Deployment | ✅ Implementado | `demo_replication.sh` |

## 🔧 Patrones de Disponibilidad

### 1. Retry (Reintentos)

**Propósito**: Reintenta automáticamente operaciones fallidas con backoff exponencial para manejar fallos transitorios.

**Implementación**:
- **Archivo**: `PlaybackService.java`, `SpotifyService.java`
- **Anotación**: `@Retry(name = "streamSource")`
- **Configuración**: 3 intentos, 200ms wait, backoff exponencial

```java
@Retry(name = "streamSource")
@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
@Transactional
public Optional<PlaybackDTO> startPlayback(Long userId, String trackId) {
    // Lógica de inicio de reproducción que se reintenta automáticamente
}
```

**Configuración YAML**:
```yaml
resilience4j:
  retry:
    instances:
      streamSource:
        maxAttempts: 3
        waitDuration: 200ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

**Justificación**: Los servicios externos (Spotify API, flaky-service) pueden fallar temporalmente. El patrón Retry mejora la disponibilidad al superar fallos transitorios automáticamente.

**Tácticas Relacionadas**: 
- **Fault Recovery**: Reintenta operaciones fallidas
- **Fault Prevention**: Previene propagación de fallos temporales

---

### 2. Circuit Breaker

**Propósito**: Previene el envío de requests a servicios que están fallando, permitiendo recuperación y evitando cascadas de fallos.

**Implementación**:
- **Archivo**: `PlaybackService.java`, `SpotifyService.java`
- **Anotación**: `@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")`
- **Estados**: CLOSED → OPEN → HALF_OPEN → CLOSED

```java
@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
public Optional<PlaybackDTO> startPlayback(Long userId, String trackId) {
    // Lógica protegida por Circuit Breaker
}

// Método fallback
public Optional<PlaybackDTO> fallbackUrl(Long userId, String trackId, Exception ex) {
    return Optional.of(new PlaybackDTO("http://fallback-stream.com/default.mp3"));
}
```

**Configuración YAML**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      streamSource:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
```

**Justificación**: Protege el sistema de servicios externos degradados, evitando timeouts largos y mejorando la experiencia del usuario con respuestas rápidas (fail-fast).

**Tácticas Relacionadas**:
- **Fault Detection**: Detecta servicios degradados automáticamente
- **Fault Recovery**: Se recupera automáticamente cuando el servicio mejora

---

### 3. Rate Limiting

**Propósito**: Limita la frecuencia de requests para prevenir abuso y proteger recursos del sistema.

**Implementación**:
- **Archivo**: `AuthService.java`
- **Anotación**: `@RateLimiter(name = "loginLimiter")`
- **Límite**: 5 intentos por minuto para login

```java
@RateLimiter(name = "loginLimiter")
public String login(String email, String password) throws AuthenticationException {
    // Lógica de login protegida por rate limiting
}
```

**Configuración YAML**:
```yaml
resilience4j:
  ratelimiter:
    instances:
      loginLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 60s
        timeoutDuration: 1s
```

**Justificación**: Previene ataques de fuerza bruta contra el sistema de autenticación y protege contra uso excesivo de recursos.

**Tácticas Relacionadas**:
- **Fault Prevention**: Previene sobrecarga del sistema
- **Resource Management**: Gestiona el uso de recursos de autenticación

---

### 4. Health Endpoint Monitoring

**Propósito**: Proporciona información sobre la salud de los componentes del sistema para monitoreo y detección temprana de problemas.

**Implementación**:
- **Framework**: Spring Boot Actuator
- **Endpoint**: `/actuator/health`
- **Componentes**: Database, Disk Space, System

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always
```

**Respuesta típica**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 10737418240,
        "free": 4294967296,
        "threshold": 10485760
      }
    }
  }
}
```

**Justificación**: Permite monitoreo proactivo y detección temprana de problemas antes de que afecten a los usuarios.

**Tácticas Relacionadas**:
- **Fault Detection**: Detecta problemas de salud del sistema
- **Monitoring**: Proporciona visibilidad del estado del sistema

## ⚡ Patrones de Rendimiento

### 1. Cache-Aside

**Propósito**: Mejora el rendimiento cachеando resultados de operaciones costosas, reduciendo latencia y carga en servicios externos.

**Implementación**:
- **Archivo**: `SpotifyService.java`
- **Anotación**: `@Cacheable`, `@CacheEvict`
- **Cache names**: `randomTracks`, `searchTracks`, `trackPlayback`

```java
@Cacheable(value = "randomTracks", key = "#limit")
public List<SpotifyTrackDto> getRandomTracks(int limit) {
    // Operación costosa que se cachea automáticamente
}

@Cacheable(value = "searchTracks", key = "#query + ':' + #limit")
public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    // Búsquedas se cachean por query y límite
}

@CacheEvict(value = {"randomTracks", "searchTracks", "trackPlayback"}, allEntries = true)
@Scheduled(fixedRate = 600000) // 10 minutos
public void evictCache() {
    // Limpieza automática del cache
}
```

**Configuración**:
```yaml
spring:
  cache:
    type: simple
    cache-names: randomTracks,searchTracks,trackPlayback
```

**Justificación**: Las llamadas a Spotify API son costosas (latencia de red). El cache reduce significativamente el tiempo de respuesta para requests repetidos.

**Tácticas Relacionadas**:
- **Resource Management**: Reduce uso de recursos de red y CPU
- **Performance**: Mejora tiempo de respuesta hasta 90%

---

### 2. Asynchronous Request-Reply

**Propósito**: Mejora el throughput procesando múltiples requests de manera concurrente en lugar de secuencial.

**Implementación**:
- **Archivo**: `PlaybackController.java`, `PlayerController.java`
- **Anotación**: `@Async`, `CompletableFuture<>`
- **Thread Pool**: 5 core threads, 10 max threads

```java
@PostMapping("/start")
public CompletableFuture<ResponseEntity<ApiResponse<PlaybackDTO>>> start(
    @RequestParam Long trackId, Authentication authentication) {
  
  return CompletableFuture.supplyAsync(() -> {
    try {
      Long userId = Long.parseLong(authentication.getName());
      PlaybackDTO playbackDTO = playbackService.startPlayback(trackId, userId);
      return ResponseEntity.ok(ApiResponse.success(playbackDTO, "Reproducción iniciada"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al iniciar reproducción", e.getMessage()));
    }
  });
}
```

**Configuración**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PlayerAsync-");
        executor.initialize();
        return executor;
    }
}
```

**Justificación**: Las operaciones de reproducción pueden ser lentas. El procesamiento asíncrono permite manejar múltiples requests simultáneamente, mejorando el throughput.

**Tácticas Relacionadas**:
- **Resource Management**: Utiliza threads de manera eficiente
- **Concurrency**: Permite procesamiento paralelo de requests

## 🔒 Patrones de Seguridad

### 1. Gatekeeper

**Propósito**: Centraliza la autenticación y autorización, protegiendo servicios internos detrás de un punto de control único.

**Implementación**:
- **Componente**: NGINX como reverse proxy
- **Archivo**: `frontend/MusifyFront/ops/nginx.conf`
- **TLS**: Terminación SSL en el gateway

```nginx
upstream backend {
    server backend-app-1:8443 max_fails=3 fail_timeout=10s;
    server backend-app-2:8443 max_fails=3 fail_timeout=10s;
    keepalive 32;
}

server {
    listen 80;
    server_name localhost;
    
    # Gatekeeper: Intercepta todas las requests
    location /api/ {
        proxy_pass https://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeout configuration
        proxy_connect_timeout 5s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

**Justificación**: Centralizar el control de acceso en NGINX mejora la seguridad al tener un único punto de validación y permite offloading de SSL.

**Tácticas Relacionadas**:
- **Authenticate Users**: Centraliza autenticación
- **Authorize Users**: Control de acceso centralizado
- **Maintain Data Confidentiality**: SSL/TLS offloading

---

### 2. Gateway Offloading

**Propósito**: Descarga funcionalidades como SSL, load balancing, retries y health checks del backend hacia el gateway.

**Implementación**:
- **Componente**: NGINX
- **Funcionalidades**: SSL termination, load balancing, retries, health checks

```nginx
upstream backend {
    server backend-app-1:8443 max_fails=3 fail_timeout=10s;
    server backend-app-2:8443 max_fails=3 fail_timeout=10s;
    keepalive 32;
}

server {
    listen 80;
    
    location /api/ {
        proxy_pass https://backend;
        
        # Gateway Offloading: NGINX maneja reintentos
        proxy_next_upstream error timeout http_500 http_502 http_503;
        proxy_next_upstream_tries 3;
        proxy_next_upstream_timeout 10s;
        
        # Connection pooling
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
```

**Justificación**: Mejorar el rendimiento y simplicidad del backend al delegar funcionalidades de infraestructura al gateway.

**Tácticas Relacionadas**:
- **Load Balancing**: Distribuye carga entre réplicas
- **Health Monitoring**: Health checks pasivos con max_fails
- **Fault Recovery**: Reintentos automáticos en el gateway

---

### 3. Federated Identity

**Propósito**: Permite autenticación centralizada usando tokens JWT y integración con proveedores externos (Spotify).

**Implementación**:
- **Archivo**: `JwtTokenProvider.java`, `SpotifyService.java`
- **JWT**: Tokens con claims de usuario
- **OAuth2**: Client credentials flow con Spotify

```java
@Service
public class JwtTokenProvider {
    public String createToken(UserDetails userDetails) {
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities());
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
}
```

**Integración OAuth2 con Spotify**:
```java
@PostConstruct
@Scheduled(fixedRate = 3300000) // 55 minutos
public void refreshAccessToken() {
    // Client Credentials Flow con Spotify
    String auth = Base64.getEncoder().encodeToString(
        (clientId + ":" + clientSecret).getBytes());
    
    // Request a Spotify para obtener access token
}
```

**Justificación**: Facilita la integración con servicios externos y proporciona un mecanismo de autenticación escalable y stateless.

**Tácticas Relacionadas**:
- **Authenticate Users**: JWT para autenticación stateless
- **Maintain User Session**: Tokens con expiración controlada
- **Support Third-party Integration**: OAuth2 con Spotify

## 🔄 Patrones de Modificabilidad

### 1. External Configuration Store

**Propósito**: Permite cambiar configuración sin modificar código o reconstruir la aplicación.

**Implementación**:
- **Variables de entorno**: `docker-compose.yaml`, `.env`
- **Archivos de config**: `application.yaml`
- **Runtime config**: Resilience4j, Spotify API, Database

```yaml
# docker-compose.yaml
environment:
  - JWT_SECRET=${JWT_SECRET:-default-secret-key}
  - SPOTIFY_CLIENT_ID=${SPOTIFY_CLIENT_ID}
  - SPOTIFY_CLIENT_SECRET=${SPOTIFY_CLIENT_SECRET}
  - DB_HOST=postgres
  - DB_PORT=5432
  - DB_NAME=musify_db
```

```yaml
# application.yaml
resilience4j:
  retry:
    instances:
      streamSource:
        maxAttempts: ${RETRY_MAX_ATTEMPTS:3}
        waitDuration: ${RETRY_WAIT_DURATION:200ms}
        
spotify:
  api:
    client-id: ${SPOTIFY_CLIENT_ID}
    client-secret: ${SPOTIFY_CLIENT_SECRET}
    
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:musify_db}
```

**Justificación**: Facilita deployment en diferentes ambientes y permite ajustes de configuración sin redeploy.

**Tácticas Relacionadas**:
- **Runtime Configuration**: Cambios sin rebuild
- **Environment Isolation**: Diferentes configs por ambiente
- **Deployment Flexibility**: Fácil configuración por ambiente

---

### 2. Blue/Green Deployment (Simulado)

**Propósito**: Permite actualizaciones sin downtime manteniendo dos versiones del sistema.

**Implementación**:
- **Setup**: 2 réplicas backend (`backend-app-1`, `backend-app-2`)
- **Load Balancer**: NGINX distribuye tráfico
- **Zero Downtime**: Update una réplica while otras maneja tráfico

```yaml
# docker-compose.yaml
services:
  backend-app-1:
    build: ./backend
    container_name: backend-app-1
    ports:
      - "8443:8443"
      
  backend-app-2:
    build: ./backend
    container_name: backend-app-2
    ports:
      - "8444:8443"
```

```nginx
# NGINX load balancing
upstream backend {
    server backend-app-1:8443 max_fails=3 fail_timeout=10s;
    server backend-app-2:8443 max_fails=3 fail_timeout=10s;
}
```

**Proceso de Update**:
1. **Update backend-app-1**: `docker compose stop backend-app-1`
2. **Traffic goes to backend-app-2**: Automático via NGINX
3. **Deploy new version**: `docker compose up --build backend-app-1`
4. **Repeat for backend-app-2**: Mismo proceso
5. **Zero downtime achieved**: Siempre hay una réplica activa

**Justificación**: Permite updates sin afectar usuarios y proporciona rollback rápido si hay problemas.

**Tácticas Relacionadas**:
- **Deployment Strategy**: Zero downtime deployments
- **Fault Tolerance**: Redundancia durante updates
- **Rollback Capability**: Fácil retorno a versión anterior

## 📊 Diagramas UML

### Diagramas Disponibles

Los siguientes diagramas PlantUML están disponibles en la carpeta `docs/diagramas/`:

#### Disponibilidad
- `retry-sequence.pu` - Secuencia del patrón Retry
- `circuit-breaker-sequence.pu` - Secuencia de Circuit Breaker
- `circuit-breaker-states.pu` - Estados de Circuit Breaker
- `health-monitoring-component.pu` - Componentes de Health Monitoring

#### Rendimiento
- `cache-aside-sequence.pu` - Secuencia de Cache-Aside
- `cache-aside-class.pu` - Clases del patrón Cache
- `async-request-reply-sequence.pu` - Secuencia Async Request-Reply
- `async-components.pu` - Componentes asíncronos

#### Seguridad
- `gatekeeper-deployment.pu` - Deployment del Gatekeeper
- `gateway-offloading-component.pu` - Componentes Gateway Offloading
- `federated-identity-jwt-sequence.pu` - Secuencia JWT
- `federated-identity-oauth2-sequence.pu` - Secuencia OAuth2

#### Modificabilidad
- `external-config-component.pu` - Componentes de configuración externa
- `blue-green-deployment.pu` - Deployment Blue/Green
- `blue-green-update-sequence.pu` - Secuencia de update Blue/Green

### Generar Diagramas

```bash
# Instalar PlantUML
sudo apt install plantuml

# Generar todos los diagramas
cd docs/diagramas
for file in *.pu; do
    plantuml "$file"
done
```

## 🧪 Scripts de Demostración

Cada patrón tiene un script de demostración que demuestra su funcionamiento:

### Ejecutar Demos Individuales

```bash
# Disponibilidad
./scripts/demo_retries.sh           # Retry + Circuit Breaker + Fallback
./scripts/demo_circuit_breaker.sh   # Circuit Breaker detallado
./scripts/demo_security.sh          # Rate Limiting + JWT + Validation
./scripts/demo_health.sh            # Health Monitoring

# Rendimiento
./scripts/demo_performance.sh       # Cache-Aside + Async Request-Reply

# Modificabilidad
./scripts/demo_replication.sh       # Blue/Green + External Config
```

### Ejecutar Todas las Demos

```bash
./scripts/run_all_demos.sh
```

## 📈 Métricas y Monitoreo

### Endpoints de Actuator

```bash
# Health checks
curl http://localhost:8080/actuator/health

# Métricas generales
curl http://localhost:8080/actuator/metrics

# Métricas específicas de patrones
curl http://localhost:8080/actuator/circuitbreakers
curl http://localhost:8080/actuator/retries
curl http://localhost:8080/actuator/ratelimiters
```

### Métricas Clave por Patrón

#### Circuit Breaker
- **State**: CLOSED/OPEN/HALF_OPEN
- **Failure Rate**: Porcentaje de fallos
- **Calls**: Total, successful, failed

#### Cache
- **Hit Ratio**: Porcentaje de cache hits
- **Evictions**: Número de entradas eliminadas
- **Size**: Número de entradas en cache

#### Async Processing
- **Active Threads**: Threads actualmente en uso
- **Completed Tasks**: Tareas completadas
- **Queue Size**: Tareas en cola

## 🎯 Relación con Tácticas de Arquitectura

### Disponibilidad
- **Fault Detection**: Health Monitoring, Circuit Breaker
- **Fault Recovery**: Retry, Circuit Breaker fallback
- **Fault Prevention**: Rate Limiting, validation

### Rendimiento  
- **Resource Management**: Cache, Async processing, Thread pools
- **Resource Arbitration**: Load balancing, Connection pooling
- **Computational Efficiency**: Cache hits, parallel processing

### Seguridad
- **Authenticate Users**: JWT, OAuth2
- **Authorize Users**: Spring Security, role-based access
- **Maintain Data Confidentiality**: SSL/TLS, token encryption
- **Limit Exposure**: Rate limiting, input validation

### Modificabilidad
- **Localize Changes**: External configuration, dependency injection
- **Deployment Time**: Blue/Green deployment, zero downtime updates
- **Runtime Configuration**: Environment variables, property files

---

> **Nota**: Todos los patrones están completamente implementados y pueden ser probados usando los scripts de demostración disponibles en la carpeta `scripts/`.