# 🎯 Guía Completa de Scripts de Demostración

> **Scripts para demostrar patrones arquitectónicos implementados en Musify**

## 🚀 Inicio Rápido (30 segundos)

```bash
# 1. Iniciar el sistema
docker compose up --build

# 2. Verificar que esté funcionando
curl http://localhost:8080/actuator/health

# 3. Ejecutar todas las demos
cd scripts && ./run_all_demos.sh
```

## 📋 Scripts Disponibles

### 🎛️ Script Maestro
- **`run_all_demos.sh`** - Menú interactivo con todas las opciones

### 🔧 Scripts Individuales por Patrón

| Script | Patrón Demostrado | Atributo de Calidad |
|--------|-------------------|-------------------|
| `demo_retries.sh` | Retry + Circuit Breaker + Fallback | Disponibilidad |
| `demo_circuit_breaker.sh` | Circuit Breaker detallado | Resiliencia |
| `demo_security.sh` | Rate Limiting + JWT + Validation | Seguridad |
| `demo_performance.sh` | Cache-Aside + Async Processing | Rendimiento |
| `demo_health.sh` | Health Monitoring | Observabilidad |
| `demo_replication.sh` | Blue/Green + Replicación | Modificabilidad |
| `demo_soap_complete.sh` | SOAP/XML + REST/JSON | Interoperabilidad |

## 🎯 Patrones Arquitectónicos Implementados

### 📊 Disponibilidad (4 patrones)

#### 1. **Retry Pattern**
- **Script**: `demo_retries.sh`
- **Configuración**: 3 intentos, 200ms wait, backoff exponencial
- **Implementación**: `@Retry(name="streamSource")` en PlaybackService
- **Demo**: Falla flaky-service → reintentos automáticos → éxito

#### 2. **Circuit Breaker**  
- **Script**: `demo_circuit_breaker.sh`
- **Configuración**: 50% failure threshold, 10s wait time
- **Estados**: CLOSED → OPEN → HALF_OPEN → CLOSED
- **Demo**: Saturar servicio → CB abre → fail-fast → recuperación

#### 3. **Rate Limiting**
- **Script**: `demo_security.sh`
- **Configuración**: 5 intentos/minuto en login
- **Implementación**: `@RateLimiter(name="loginLimiter")`
- **Demo**: Múltiples logins → rate limit → protección activada

#### 4. **Health Endpoint Monitoring**
- **Script**: `demo_health.sh`
- **Endpoint**: `/actuator/health`
- **Monitoreo**: Database, disco, componentes del sistema
- **Demo**: Health checks automáticos → status UP/DOWN

### ⚡ Rendimiento (2 patrones)

#### 1. **Cache-Aside**
- **Script**: `demo_performance.sh`
- **Implementación**: `@Cacheable` en SpotifyService
- **Cache names**: `randomTracks`, `searchTracks`, `trackPlayback`
- **Demo**: Cache miss (lento) vs Cache hit (rápido)

#### 2. **Async Request-Reply**
- **Script**: `demo_performance.sh`
- **Implementación**: `CompletableFuture` + `@Async`
- **Thread Pool**: 5 core, 10 max threads
- **Demo**: Secuencial vs Paralelo → speedup significativo

### 🔒 Seguridad (3 patrones)

#### 1. **Gatekeeper**
- **Script**: `demo_security.sh`
- **Implementación**: NGINX como reverse proxy
- **TLS**: Terminación SSL en gateway
- **Demo**: Requests → NGINX → Backend (SSL offloading)

#### 2. **Gateway Offloading**
- **Script**: `demo_security.sh`
- **NGINX**: Load balancing, retries, health checks
- **Config**: `max_fails=3 fail_timeout=10s`
- **Demo**: Gateway maneja reintentos y balanceo

#### 3. **Federated Identity (JWT)**
- **Script**: `demo_security.sh`
- **JWT**: Tokens con claims de usuario
- **OAuth2**: Integración con Spotify API
- **Demo**: Login → JWT token → requests autenticados

### 🔄 Modificabilidad (2 patrones)

#### 1. **External Configuration Store**
- **Script**: `demo_replication.sh`
- **Config**: Variables de entorno + `application.yaml`
- **Ejemplos**: `JWT_SECRET`, `SPOTIFY_CLIENT_ID`, `DB_HOST`
- **Demo**: Cambios de config sin rebuild

#### 2. **Blue/Green Deployment (simulado)**
- **Script**: `demo_replication.sh`
- **Setup**: 2 réplicas backend detrás de NGINX
- **Demo**: Update una réplica → switch traffic → zero downtime

## 🎮 Uso del Menú Interactivo

Al ejecutar `./run_all_demos.sh` verás:

```
╔══════════════════════════════════════════════════════════════╗
║                    MUSIFY DEMO SELECTOR                     ║
╚══════════════════════════════════════════════════════════════╝

1) Retry + Circuit Breaker + Fallback      → Disponibilidad
2) Circuit Breaker (detallado)             → Resiliencia  
3) Rate Limiting + Validación              → Seguridad
4) Health Monitoring                       → Observabilidad
5) Blue/Green Deployment + Replicación     → Modificabilidad
6) Cache-Aside + Async Request-Reply       → Rendimiento
7) SOAP/XML + REST/JSON                    → Interoperabilidad

A) Ejecutar todas las demos (con pausas)
Q) Ejecutar todas las demos (rápido, sin pausas)
0) Salir
```

## 📈 Resultados Esperados

### ✅ Demo Exitosa - Indicadores
- **Retry**: 10/10 requests exitosos tras reintentos
- **Circuit Breaker**: Transiciones de estado correctas
- **Rate Limiting**: HTTP 429 tras límite superado
- **Cache**: >50% mejora en latencia en cache hits
- **Health**: Status "UP" en todos los componentes
- **Replication**: Zero downtime durante failover

### ❌ Problemas Comunes

#### Docker no iniciado
```bash
# Error: Cannot connect to Docker daemon
sudo systemctl start docker
docker compose up --build
```

#### Puertos ocupados
```bash
# Error: Port 8080 already in use
sudo lsof -i :8080
sudo kill -9 <PID>
```

#### Servicios no responden
```bash
# Verificar logs
docker compose logs backend-app-1
curl http://localhost:8080/actuator/health
```

## 🔧 Configuración de Patrones

### application.yaml - Resilience4j
```yaml
resilience4j:
  retry:
    instances:
      streamSource:
        maxAttempts: 3
        waitDuration: 200ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        
  circuitbreaker:
    instances:
      streamSource:
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        slidingWindowSize: 10
        
  ratelimiter:
    instances:
      loginLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 60s
```

### NGINX - Gateway Offloading
```nginx
upstream backend {
    server backend-app-1:8443 max_fails=3 fail_timeout=10s;
    server backend-app-2:8443 max_fails=3 fail_timeout=10s;
    keepalive 32;
}

location /api/ {
    proxy_pass https://backend;
    proxy_next_upstream error timeout http_500 http_502 http_503;
    proxy_connect_timeout 5s;
    proxy_read_timeout 30s;
}
```

### Spring Cache
```java
@Cacheable(value = "randomTracks", key = "#limit")
public List<SpotifyTrackDto> getRandomTracks(int limit) {
    // Implementación que se cachea automáticamente
}

@CacheEvict(value = {"randomTracks", "searchTracks"}, allEntries = true)
@Scheduled(fixedRate = 600000) // 10 minutos
public void evictCache() {
    // Limpieza automática del cache
}
```

## 📊 Métricas y Monitoreo

### Endpoints de Actuator
- `/actuator/health` - Estado de salud general
- `/actuator/metrics` - Métricas de aplicación
- `/actuator/circuitbreakers` - Estado de circuit breakers
- `/actuator/retries` - Estadísticas de reintentos
- `/actuator/ratelimiters` - Estado de rate limiters

### Logs Importantes
```bash
# Circuit Breaker abierto
"Circuit breaker 'streamSource' is OPEN"

# Rate limit activado  
"Request rate limit exceeded for user"

# Cache hit/miss
"Cache hit for key: randomTracks:10"
"Cache miss for key: searchTracks:jazz"

# Retry ejecutándose
"Retrying request after failure, attempt 2/3"
```

## 🎯 Scripts por Caso de Uso

### Para Demos Académicas
```bash
./run_all_demos.sh  # Menú completo e interactivo
```

### Para Pruebas Rápidas
```bash
./demo_health.sh        # Verificar sistema OK
./demo_retries.sh       # Probar resiliencia básica
./demo_performance.sh   # Mostrar mejoras de rendimiento
```

### Para Validación de Patrones Específicos
```bash
./demo_circuit_breaker.sh   # Resiliencia avanzada
./demo_security.sh         # Seguridad completa
./demo_replication.sh      # Alta disponibilidad
```

## 🔄 Troubleshooting por Script

### demo_retries.sh
- **Problema**: No hay reintentos
- **Solución**: Verificar que flaky-service esté corriendo
- **Check**: `curl http://localhost:3001/stream`

### demo_circuit_breaker.sh  
- **Problema**: CB no abre
- **Solución**: Ajustar `failureRateThreshold` en config
- **Check**: `/actuator/circuitbreakers`

### demo_performance.sh
- **Problema**: No mejora de cache
- **Solución**: Limpiar cache antes de la demo
- **Check**: Logs de "Cache hit/miss"

### demo_security.sh
- **Problema**: Rate limit no funciona
- **Solución**: Verificar config de rate limiter
- **Check**: HTTP 429 responses

## 📚 Referencias y Documentación

- **Resilience4j**: https://resilience4j.readme.io/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Spring Cache**: https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache
- **NGINX**: https://nginx.org/en/docs/http/load_balancing.html

---

> **Nota**: Todos los scripts están diseñados para ser ejecutados desde la carpeta `scripts/` con el sistema completo funcionando via `docker compose up --build`.