# Patrones de Arquitectura Implementados - TFU Unidad 4

## Resumen Ejecutivo

Este documento describe los **11 patrones de arquitectura** implementados en la aplicación Musify como parte del Trabajo Final de Unidad 4. La implementación cumple y excede los requisitos mínimos de 7 patrones (2 de disponibilidad, 2 de rendimiento, 2 de seguridad, y 1 de modificabilidad).

### Resumen de Patrones Implementados

| Categoría               | Patrón                       | Estado          |
| ----------------------- | ---------------------------- | --------------- |
| **Disponibilidad (4)**  | Retry                        | ✅ Implementado |
|                         | Circuit Breaker              | ✅ Implementado |
|                         | Rate Limiting                | ✅ Implementado |
|                         | Health Endpoint Monitoring   | ✅ Implementado |
| **Rendimiento (2)**     | Cache-Aside                  | ✅ Implementado |
|                         | Asynchronous Request-Reply   | ✅ Implementado |
| **Seguridad (3)**       | Gatekeeper                   | ✅ Implementado |
|                         | Gateway Offloading           | ✅ Implementado |
|                         | Federated Identity           | ✅ Implementado |
| **Modificabilidad (2)** | External Configuration Store | ✅ Implementado |
|                         | Blue/Green Deployment        | ✅ Implementado |

---

## 1. Patrones de Disponibilidad

### 1.1. Retry (Reintentos)

**Descripción**: Reintenta automáticamente operaciones fallidas con backoff exponencial para manejar fallos transitorios.

**Ubicación en el código**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/playback/PlaybackService.java`
- **Anotación**: `@Retry(name = "streamSource")`
- **Método**: `startPlayback(Long userId, String trackId)`

```java
@Retry(name = "streamSource")
@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
@Transactional
public Optional<PlaybackDTO> startPlayback(Long userId, String trackId) {
    // Lógica de inicio de reproducción
}
```

- **Archivo**: `backend/src/main/java/com/tfu/backend/spotify/SpotifyService.java`
- **Anotación**: `@Retry(name = "spotifyApi")`
- **Métodos**: `getRandomTracks()`, `searchTracks()`, `getTrackPlayback()`

**Configuración**:

- **Archivo**: `backend/src/main/resources/application.yaml`
- **Ruta**: `resilience4j.retry.instances.streamSource`

```yaml
resilience4j:
  retry:
    instances:
      streamSource:
        maxAttempts: 3
        waitDuration: 200ms
        enableExponentialBackoff: true
      spotifyApi:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
```

**Demostración**:

- **Script**: `scripts/demo_retries.sh`
- Muestra cómo el sistema reintenta automáticamente cuando el `flaky-service` falla.

---

### 1.2. Circuit Breaker

**Descripción**: Previene cascadas de fallos abriendo el circuito cuando el porcentaje de fallos excede un umbral, proporcionando respuestas de fallback.

**Ubicación en el código**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/playback/PlaybackService.java`
- **Anotación**: `@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")`
- **Método de fallback**: `fallbackUrl(Long userId, String trackId, Throwable t)`

```java
@CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
public Optional<PlaybackDTO> startPlayback(Long userId, String trackId) {
    // Intenta obtener URL del flaky-service
}

private Optional<PlaybackDTO> fallbackUrl(Long userId, String trackId, Throwable t) {
    // Proporciona URL alternativa cuando el servicio falla
    return Optional.of(new PlaybackDTO(trackId, "https://fallback.example/stream/" + trackId));
}
```

- **Archivo**: `backend/src/main/java/com/tfu/backend/spotify/SpotifyService.java`
- **Métodos con Circuit Breaker**: `getRandomTracks()`, `searchTracks()`, `getTrackPlayback()`

**Configuración**:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      streamSource:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
      spotifyApi:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

**Demostración**:

- **Script**: `scripts/demo_retries.sh`
- Muestra cómo después de varios fallos, el circuito se abre y usa el fallback.

---

### 1.3. Rate Limiting

**Descripción**: Limita el número de intentos de login por minuto para prevenir ataques de fuerza bruta.

**Ubicación en el código**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/auth/AuthService.java`
- **Anotación**: `@RateLimiter(name = "loginLimiter")`
- **Método**: `login(String email, String password)`

```java
@RateLimiter(name = "loginLimiter")
public LoginResponse login(String email, String password) {
    // Lógica de autenticación
}
```

**Configuración**:

```yaml
resilience4j:
  ratelimiter:
    instances:
      loginLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 1m
        timeoutDuration: 0
```

**Demostración**:

- **Script**: `scripts/demo_security.sh`
- Muestra cómo después de 5 intentos, las siguientes peticiones son rechazadas.

---

### 1.4. Health Endpoint Monitoring

**Descripción**: Expone endpoints de salud para monitorear el estado del sistema y sus componentes.

**Ubicación en el código**:

- **Configuración**: `backend/src/main/resources/application.yaml`
- **Endpoint**: `http://localhost:8080/actuator/health`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Componentes monitoreados**:

- Estado de la base de datos
- Estado de la aplicación
- Métricas del sistema

**Demostración**:

- **Script**: `scripts/demo_health.sh`
- Muestra el JSON con el estado de salud del sistema.

---

## 2. Patrones de Rendimiento

### 2.1. Cache-Aside

**Descripción**: Cachea resultados de búsquedas de Spotify para reducir llamadas a APIs externas y mejorar tiempos de respuesta.

**Ubicación en el código**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/spotify/SpotifyService.java`
- **Anotaciones**: `@Cacheable` en métodos de búsqueda

```java
@Cacheable(value = "randomTracks", key = "#limit")
public List<SpotifyTrackDto> getRandomTracks(int limit) {
    // Primera llamada: consulta Spotify API
    // Siguientes llamadas: retorna desde caché
}

@Cacheable(value = "searchTracks", key = "#query + '_' + #limit")
public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    // Cachea por combinación de query y limit
}

@Cacheable(value = "trackPlayback", key = "#trackId")
public SpotifyPlaybackResponse getTrackPlayback(String trackId) {
    // Cachea datos de reproducción por trackId
}
```

**Evicción de caché**:

```java
@CacheEvict(value = {"randomTracks", "searchTracks", "trackPlayback"}, allEntries = true)
@Scheduled(fixedRate = 600000) // Cada 10 minutos
public void evictAllCaches() {
    System.out.println("Evicting all Spotify caches to refresh data");
}
```

**Configuración**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/BackendApplication.java`
- **Anotación**: `@EnableCaching`

```yaml
spring:
  cache:
    type: simple
    cache-names: randomTracks,searchTracks,trackPlayback
```

**Dependencia**:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

**Demostración**:

- **Script**: `scripts/demo_performance.sh`
- Compara tiempos de respuesta: primera llamada (sin caché) vs. llamadas subsecuentes (con caché).

---

### 2.2. Asynchronous Request-Reply

**Descripción**: Procesa operaciones de playback de forma asíncrona sin bloquear el thread principal, permitiendo manejar múltiples peticiones concurrentemente.

**Ubicación en el código**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/playback/PlaybackController.java`
- **Métodos asíncronos**: `start()`, `resume()`

```java
@PostMapping("/start")
@TimeLimiter(name = "streamSource")
public CompletableFuture<ResponseEntity<ApiResponse<PlaybackDTO>>> start(
    @RequestParam String trackId,
    Authentication authentication) {

    return CompletableFuture.supplyAsync(() -> {
        // Procesamiento asíncrono
        Long userId = Long.parseLong(authentication.getName());
        return playbackService.startPlayback(userId, trackId)
            .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Reproducción iniciada")))
            .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al iniciar reproducción")));
    });
}
```

**Configuración del Thread Pool**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/config/AsyncConfig.java`

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // Mínimo 5 threads
        executor.setMaxPoolSize(10);        // Máximo 10 threads
        executor.setQueueCapacity(100);     // Cola de 100 tareas
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

**Habilitación**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/BackendApplication.java`
- **Anotaciones**: `@EnableAsync`, `@EnableScheduling`

**Demostración**:

- **Script**: `scripts/demo_performance.sh`
- Lanza 3 operaciones concurrentes y mide el tiempo total, demostrando procesamiento paralelo.

---

## 3. Patrones de Seguridad

### 3.1. Gatekeeper

**Descripción**: NGINX actúa como gateway/reverse proxy que protege el backend, manejando TLS y balanceo de carga.

**Ubicación en el código**:

- **Archivo**: `frontend/MusifyFront/ops/nginx.conf`

```nginx
upstream app_backend {
  server backend-app-1:8443 max_fails=3 fail_timeout=10s;
  server backend-app-2:8443 max_fails=3 fail_timeout=10s;
  keepalive 32;
}

server {
  listen 80;

  location / {
    proxy_pass https://app_backend;
    proxy_ssl_verify off;
    # Headers de seguridad
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
}
```

**Orquestación**:

- **Archivo**: `docker-compose.yaml`
- El servicio `nginx` es el único punto de entrada expuesto públicamente en el puerto 8080.

**Beneficios**:

- Ocultación de la arquitectura interna
- Punto único de control de acceso
- Terminación TLS/SSL en el gateway

---

### 3.2. Gateway Offloading

**Descripción**: NGINX offloads funcionalidades como reintentos, health checks, y compresión, liberando al backend de estas responsabilidades.

**Ubicación en el código**:

- **Archivo**: `frontend/MusifyFront/ops/nginx.conf`

**Funcionalidades offloaded**:

1. **Reintentos automáticos**:

```nginx
proxy_next_upstream error timeout http_500 http_502 http_503 http_504;
proxy_next_upstream_tries 3;
```

2. **Health checks pasivos**:

```nginx
server backend-app-1:8443 max_fails=3 fail_timeout=10s;
```

3. **Connection pooling**:

```nginx
keepalive 32;
```

4. **Compresión**:

```nginx
gzip on;
gzip_types application/json application/javascript text/plain text/css;
```

5. **Timeouts**:

```nginx
proxy_connect_timeout 10s;
proxy_send_timeout    10s;
proxy_read_timeout    10s;
```

**Demostración**:

- **Script**: `scripts/demo_replication.sh`
- Muestra cómo NGINX redirige automáticamente el tráfico cuando una instancia falla.

---

### 3.3. Federated Identity

**Descripción**: Autenticación basada en JWT para la aplicación y OAuth2 para integración con Spotify API.

**Ubicación en el código**:

**JWT Authentication**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/auth/JwtTokenProvider.java`
- Genera y valida tokens JWT

```java
public class JwtTokenProvider {
    public String generateToken(Authentication authentication) {
        // Genera token JWT con secret key
    }

    public boolean validateToken(String token) {
        // Valida firma y expiración
    }
}
```

- **Archivo**: `backend/src/main/java/com/tfu/backend/auth/JwtAuthFilter.java`
- Filtro que valida JWT en cada request

**OAuth2 con Spotify**:

- **Archivo**: `backend/src/main/java/com/tfu/backend/spotify/SpotifyService.java`
- Implementa client credentials flow

```java
@PostConstruct
@Scheduled(fixedRate = 3000000) // Refresca token cada 50 minutos
public void fetchAccessToken() {
    String authHeader = "Basic " + Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes());
    // Obtiene access token de Spotify
}
```

**Configuración**:

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: ${JWT_EXPIRATION:3600}

spotify:
  client:
    id: ${SPOTIFY_CLIENT_ID}
    secret: ${SPOTIFY_CLIENT_SECRET}
```

**Demostración**:

- **Script**: `scripts/demo_security.sh`
- Muestra login y uso de JWT token en requests autenticados.

---

## 4. Patrones de Modificabilidad y Despliegue

### 4.1. External Configuration Store

**Descripción**: Externaliza toda la configuración mediante variables de entorno, permitiendo cambios sin recompilación.

**Ubicación en el código**:

**Variables de entorno**:

- **Archivo**: `docker-compose.yaml`

```yaml
backend-app-1:
  environment:
    SERVER_PORT: 8443
    JWT_SECRET: ${JWT_SECRET}
    POSTGRES_DB: ${POSTGRES_DB}
    SPOTIFY_CLIENT_ID: ${SPOTIFY_CLIENT_ID}
    SPOTIFY_CLIENT_SECRET: ${SPOTIFY_CLIENT_SECRET}
    DB_HOST: postgres
```

**Archivo .env** (no versionado):

```env
JWT_SECRET=mi_secret_super_secreto
POSTGRES_DB=musify_db
POSTGRES_USER=musify_user
POSTGRES_PASSWORD=musify_pass
SPOTIFY_CLIENT_ID=tu_client_id
SPOTIFY_CLIENT_SECRET=tu_client_secret
```

**Consumo en la aplicación**:

- **Archivo**: `backend/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${POSTGRES_DB:mydatabase}
    username: ${POSTGRES_USER:myuser}
    password: ${POSTGRES_PASSWORD:postgres}

app:
  security:
    jwt:
      secret: ${JWT_SECRET:default_secret_key_for_development_only}

spotify:
  client:
    id: ${SPOTIFY_CLIENT_ID}
    secret: ${SPOTIFY_CLIENT_SECRET}
```

**Beneficios**:

- Configuración diferente por ambiente (dev, staging, prod)
- Secrets no versionados en Git
- Cambios sin recompilación

---

### 4.2. Blue/Green Deployment (Simulado)

**Descripción**: Dos réplicas del backend permiten actualizaciones sin downtime mediante actualización secuencial.

**Ubicación en el código**:

- **Archivo**: `docker-compose.yaml`

```yaml
services:
  backend-app-1:
    build:
      context: ./backend
    image: tfu-backend:local
    container_name: backend-app-1
    # Configuración...

  backend-app-2:
    image: tfu-backend:local # Reutiliza la misma imagen
    container_name: backend-app-2
    # Misma configuración que app-1
```

**Load Balancer**:

- **Archivo**: `frontend/MusifyFront/ops/nginx.conf`

```nginx
upstream app_backend {
  server backend-app-1:8443 max_fails=3 fail_timeout=10s;
  server backend-app-2:8443 max_fails=3 fail_timeout=10s;
}
```

**Proceso de actualización sin downtime**:

1. Detener `backend-app-1`
2. NGINX redirige todo el tráfico a `backend-app-2`
3. Actualizar y reiniciar `backend-app-1`
4. Verificar que funciona correctamente
5. Repetir proceso con `backend-app-2`

**Demostración**:

- **Script**: `scripts/demo_replication.sh`

```bash
# Detiene una instancia
docker stop backend-app-1

# Sistema sigue funcionando con app-2
curl http://localhost:8080/tracks/random

# Reinicia app-1
docker start backend-app-1
```

---

## 5. Validación de Patrones

### 5.1. Scripts de Demostración

Cada patrón tiene un script que prueba su funcionamiento:

| Script                        | Patrones Demostrados                  |
| ----------------------------- | ------------------------------------- |
| `scripts/demo_retries.sh`     | Retry, Circuit Breaker, TimeLimiter   |
| `scripts/demo_replication.sh` | Blue/Green Deployment, Load Balancing |
| `scripts/demo_security.sh`    | Rate Limiting, Input Validation, JWT  |
| `scripts/demo_performance.sh` | Cache-Aside, Async Request-Reply      |
| `scripts/demo_health.sh`      | Health Endpoint Monitoring            |

### 5.2. Cómo Ejecutar las Demostraciones

```bash
# 1. Iniciar todos los servicios
docker compose up --build

# 2. Ejecutar script de demostración
./scripts/demo_performance.sh
```

---

## 6. Relación con Tácticas de Arquitectura

### Disponibilidad → Detección y Recuperación de Fallos

- **Retry**: Recuperación automática de fallos transitorios
- **Circuit Breaker**: Prevención de cascadas de fallos
- **Health Monitoring**: Detección temprana de problemas
- **Replication**: Redundancia para alta disponibilidad

### Rendimiento → Gestión de Recursos

- **Cache-Aside**: Reducción de latencia mediante cacheo
- **Async Request-Reply**: Mejor utilización de threads y recursos

### Seguridad → Resistir Ataques

- **Rate Limiting**: Prevención de ataques de fuerza bruta
- **Gatekeeper**: Control centralizado de acceso
- **Gateway Offloading**: Protección del backend
- **Federated Identity**: Autenticación segura y distribuida

### Modificabilidad → Gestión de Configuración

- **External Configuration Store**: Separación de configuración y código
- **Blue/Green Deployment**: Despliegues seguros y reversibles

---

## 7. Tecnologías Utilizadas

| Tecnología          | Propósito                | Patrones Relacionados                 |
| ------------------- | ------------------------ | ------------------------------------- |
| **Spring Boot**     | Framework backend        | Todos los patrones backend            |
| **Resilience4j**    | Biblioteca de resilience | Retry, Circuit Breaker, Rate Limiting |
| **Spring Cache**    | Cacheo                   | Cache-Aside                           |
| **Spring Async**    | Procesamiento asíncrono  | Async Request-Reply                   |
| **Spring Actuator** | Monitoreo                | Health Endpoint Monitoring            |
| **NGINX**           | Gateway/Proxy            | Gatekeeper, Gateway Offloading        |
| **Docker Compose**  | Orquestación             | Blue/Green, External Config           |
| **JWT**             | Autenticación            | Federated Identity                    |
| **PostgreSQL**      | Base de datos            | Persistencia                          |

---

## 8. Estructura de Archivos Clave

```
musify/
├── backend/
│   ├── src/main/java/com/tfu/backend/
│   │   ├── auth/                    # JWT, Rate Limiting
│   │   │   ├── AuthService.java    # @RateLimiter
│   │   │   └── JwtTokenProvider.java
│   │   ├── config/
│   │   │   └── AsyncConfig.java    # Thread Pool Config
│   │   ├── playback/
│   │   │   ├── PlaybackService.java     # @Retry, @CircuitBreaker
│   │   │   └── PlaybackController.java  # Async endpoints
│   │   └── spotify/
│   │       └── SpotifyService.java      # @Cacheable, OAuth2
│   ├── src/main/resources/
│   │   └── application.yaml        # Configuración Resilience4j, Cache
│   └── pom.xml                     # Dependencias
├── frontend/MusifyFront/ops/
│   └── nginx.conf                  # Gatekeeper, Gateway Offloading
├── scripts/
│   ├── demo_retries.sh            # Demo Retry + Circuit Breaker
│   ├── demo_replication.sh        # Demo Blue/Green
│   ├── demo_security.sh           # Demo Rate Limiting
│   ├── demo_performance.sh        # Demo Cache + Async
│   └── demo_health.sh             # Demo Health Monitoring
├── docker-compose.yaml             # Orquestación, External Config
└── README.md                       # Documentación general
```

---

## 9. Conclusión

Este proyecto implementa **11 patrones de arquitectura** que cubren ampliamente los requisitos de disponibilidad, rendimiento, seguridad y modificabilidad. Cada patrón está:

✅ **Implementado** en el código con ejemplos concretos  
✅ **Configurado** apropiadamente  
✅ **Documentado** con ubicaciones exactas  
✅ **Demostrado** mediante scripts ejecutables  
✅ **Relacionado** con tácticas de arquitectura

Los patrones trabajan en conjunto para crear una aplicación resiliente, performante, segura y fácil de mantener.
