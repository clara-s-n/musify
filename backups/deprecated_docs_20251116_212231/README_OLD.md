# Musify

## Descripción del Proyecto

Musify es una aplicación de demostración que implementa diversas tácticas de arquitectura para satisfacer requerimientos no funcionales relacionados con disponibilidad, rendimiento y seguridad. La aplicación simula un servicio de streaming de música, permitiendo buscar canciones, autenticarse y reproducir pistas.

## Tácticas de Arquitectura Implementadas

Este proyecto implementa la combinación: **"Replicación y re-intentos para disponibilidad y dos tácticas de la categoría resistir a ataques para seguridad"**

### 1. Tácticas para Disponibilidad

#### Replicación

- **Implementación**: Dos instancias replicadas del backend (`backend-app-1` y `backend-app-2`).
- **Componentes clave**:
  - Docker Compose para gestionar múltiples instancias
  - NGINX como balanceador de carga
- **Beneficio**: Alta disponibilidad incluso si una instancia falla.

#### Re-intentos y Circuit Breaker

- **Implementación**: Resilience4j para gestionar fallos del servicio de streaming.
- **Componentes clave**:
  - `@Retry`: Reintenta operaciones fallidas automáticamente
  - `@CircuitBreaker`: Previene cascada de fallos
  - `@TimeLimiter`: Establece tiempos de espera máximos
  - Método de fallback para degradación elegante
- **Beneficio**: Tolerancia a fallos transitorios y protección contra cascadas de fallos.

#### Health Endpoint Monitoring

- **Implementación**: Spring Boot Actuator para monitoreo de salud.
- **Componentes clave**:
  - Endpoint `/actuator/health` expuesto públicamente
  - Métricas y estado del sistema
- **Beneficio**: Visibilidad del estado del sistema y detección temprana de problemas.

### 2. Tácticas para Rendimiento

#### Cache-Aside

- **Implementación**: Spring Cache para cachear resultados de búsqueda de Spotify.
- **Componentes clave**:
  - `@Cacheable` en métodos de búsqueda de `SpotifyService`
  - `@CacheEvict` programado para refrescar caché cada 10 minutos
  - Configuración de caché en `application.yaml`
- **Beneficio**: Reducción de llamadas a APIs externas y mejora de tiempos de respuesta.

#### Asynchronous Request-Reply

- **Implementación**: Procesamiento asíncrono de operaciones de playback.
- **Componentes clave**:
  - `CompletableFuture<>` en `PlaybackController`
  - `ThreadPoolTaskExecutor` configurado en `AsyncConfig`
  - `@EnableAsync` para habilitar operaciones asíncronas
- **Beneficio**: Mejor utilización de recursos y capacidad de manejar múltiples peticiones concurrentemente.

### 3. Tácticas para Seguridad (Resistir Ataques)

#### Validación de Entrada

- **Implementación**: Validación de datos de usuario.
- **Componentes clave**:
  - Anotaciones `@Email` y `@NotBlank`
  - Validaciones de formato
- **Beneficio**: Prevención de inyecciones y ataques de entrada maliciosa.

#### Rate Limiting

- **Implementación**: Limitación de tasa en endpoint de login.
- **Componentes clave**:
  - `@RateLimiter` de Resilience4j
  - Configuración de límites por periodo de tiempo
- **Beneficio**: Protección contra ataques de fuerza bruta y denegación de servicio.

#### Gatekeeper y Gateway Offloading

- **Implementación**: NGINX como gateway que offloads funcionalidades.
- **Componentes clave**:
  - NGINX como reverse proxy y load balancer
  - Manejo de TLS/SSL en el gateway
  - Reintentos automáticos (`proxy_next_upstream`)
  - Health checks pasivos
- **Beneficio**: Separación de responsabilidades, protección del backend, y mejora de seguridad.

#### Federated Identity (parcial)

- **Implementación**: JWT-based authentication y OAuth2 con Spotify.
- **Componentes clave**:
  - `JwtTokenProvider` para generación y validación de tokens
  - Integración OAuth2 con Spotify API (client credentials flow)
- **Beneficio**: Autenticación distribuida y integración con proveedores externos.

### 4. Tácticas de Facilidad de Modificación y Despliegue

#### External Configuration Store

- **Implementación**: Externalización de configuración mediante variables de entorno.
- **Componentes clave**:
  - Variables de entorno en `docker-compose.yaml`
  - Archivo `.env` para configuración local
  - `application.yaml` con placeholders `${VAR:default}`
- **Beneficio**: Configuración sin recompilación y despliegue en múltiples ambientes.

#### Blue/Green Deployment (simulado)

- **Implementación**: Dos réplicas del backend permiten actualizaciones sin downtime.
- **Componentes clave**:
  - `backend-app-1` y `backend-app-2` en Docker Compose
  - NGINX load balancer con health checks
- **Beneficio**: Despliegues sin downtime mediante actualización secuencial de réplicas.

## Estructura del Proyecto

```
musify/
├── backend/                     # Aplicación Spring Boot
│   ├── src/                     # Código fuente
│   │   └── main/
│   │       ├── java/com/tfu/backend/
│   │       │   ├── auth/        # Autenticación y seguridad
│   │       │   ├── catalog/     # Catálogo de canciones
│   │       │   ├── config/      # Configuración de seguridad
│   │       │   └── playback/    # Reproducción de pistas
│   │       └── resources/       # Configuraciones
│   ├── Dockerfile               # Construcción de imagen backend
│   └── pom.xml                  # Dependencias Maven
├── flaky-service/               # Servicio inestable (simulación)
│   ├── Dockerfile
│   ├── package.json
│   └── server.js                # Simulación de fallos aleatorios
├── ops/
│   └── nginx.conf               # Configuración de balanceo y reintentos
├── scripts/                     # Scripts de demostración
│   ├── demo_health.sh
│   ├── demo_replication.sh
│   ├── demo_retries.sh
│   └── demo_security.sh
└── docker-compose.yaml          # Orquestación de servicios
```

## Requisitos

- Docker y Docker Compose
- Bash

## Cómo Ejecutar

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/clara-s-n/musify.git
   cd musify
   ```

2. Iniciar los servicios:

   ```bash
   docker compose up --build
   ```

3. Esperar a que todos los servicios estén ejecutándose. La aplicación estará disponible en http://localhost:8080

## Demostraciones de Patrones

Este proyecto incluye scripts completos para demostrar cómo cada patrón logra los atributos de calidad correspondientes. Los scripts son ejecutables, automatizados y proporcionan métricas cuantificables.

### Script Maestro (Recomendado)

```bash
cd scripts
./run_all_demos.sh
```

El script maestro `run_all_demos.sh` proporciona:

- ✅ Menú interactivo para seleccionar demos
- ✅ Opción para ejecutar todas las demos automáticamente
- ✅ Verificación de requisitos previos
- ✅ Output coloreado con métricas detalladas
- ✅ Resumen final de todos los patrones

### Demos Individuales

#### 1. Disponibilidad: Retry + Circuit Breaker + Fallback

```bash
./scripts/demo_retries.sh
```

**Demuestra:**

- Reintentos automáticos (3 intentos, 200ms, exponential backoff)
- Circuit Breaker (CLOSED/OPEN/HALF_OPEN states)
- Degradación elegante con fallback URLs
- Métricas: Tasa de éxito antes/después de aplicar patrones

**Atributos de calidad:** Disponibilidad, Resiliencia, Tolerancia a fallos

#### 2. Disponibilidad: Circuit Breaker en Detalle

```bash
./scripts/demo_circuit_breaker.sh
```

**Demuestra:**

- Estados del Circuit Breaker (CLOSED → OPEN → HALF_OPEN)
- Fail-fast cuando CB está abierto
- Recuperación automática cuando servicio mejora
- Configuración: 50% threshold, 10s wait, sliding window de 10

**Atributos de calidad:** Disponibilidad, Resiliencia, Rendimiento

#### 3. Seguridad: Validación + Rate Limiting + JWT

```bash
./scripts/demo_security.sh
```

**Demuestra:**

- Validación de entrada (Bean Validation con @Email)
- Rate limiting (5 intentos/minuto en login)
- JWT authentication (token generation y validation)
- Gatekeeper (NGINX como punto único de entrada)
- Gateway offloading (TLS, retries, health checks)

**Atributos de calidad:** Seguridad, Resistencia a ataques, Confidencialidad

#### 4. Rendimiento: Cache-Aside + Async Request-Reply

```bash
./scripts/demo_performance.sh
```

**Demuestra:**

- Cache-Aside: Comparación cache miss vs cache hit (50x más rápido)
- Async: Procesamiento concurrente vs secuencial (3x más rápido)
- Thread pool configuration (5 core, 10 max, 100 queue)
- Métricas: Latencia, throughput, speedup factor

**Atributos de calidad:** Rendimiento, Escalabilidad, Eficiencia, Throughput

#### 5. Disponibilidad: Health Monitoring + Observabilidad

```bash
./scripts/demo_health.sh
```

**Demuestra:**

- Spring Boot Actuator health endpoints
- Monitoreo de componentes (DB, disco, memoria)
- OpenAPI/Swagger documentation
- Integración con sistemas de monitoreo externos

**Atributos de calidad:** Disponibilidad, Observabilidad, Mantenibilidad

#### 6. Modificabilidad: Blue/Green Deployment + Replicación

```bash
./scripts/demo_replication.sh
```

**Demuestra:**

- Alta disponibilidad con 2 réplicas del backend
- Failover automático cuando réplica falla
- Zero-downtime deployment (actualización sin interrumpir servicio)
- Load balancing con NGINX
- Health checks automáticos

**Atributos de calidad:** Disponibilidad, Escalabilidad, Modificabilidad, Resiliencia

### Ejecutar Todas las Demos

```bash
# Modo interactivo con menú
cd scripts
./run_all_demos.sh

# Modo automático con pausas entre demos
cd scripts
./run_all_demos.sh <<< "A"

# Modo rápido sin pausas
cd scripts
./run_all_demos.sh <<< "Q"
```

### Documentación de Scripts

Para más detalles sobre los scripts de demostración, consulta:

- [`scripts/README.md`](scripts/README.md) - Documentación completa de los scripts
- Cada script incluye comentarios detallados y help text
- Output coloreado con métricas cuantificables
- Verificación automática de requisitos previos

### Requisitos para Ejecutar Demos

- Sistema Musify ejecutándose: `docker compose up --build`
- `curl` instalado
- `jq` instalado (para parsear JSON): `sudo apt-get install jq`
- `docker` CLI accesible

## Contribución

Este proyecto fue creado como demostración para la materia "Análisis y Diseño de Aplicaciones II" y no está destinado para uso en producción.
