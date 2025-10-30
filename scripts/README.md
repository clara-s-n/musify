# Scripts de Demostración - Musify

Este directorio contiene scripts para demostrar los patrones arquitectónicos implementados en Musify para la asignatura TFU Unidad 4.

## 📋 Scripts Disponibles

### Script Maestro

**`run_all_demos.sh`** - Script interactivo que ejecuta todas las demos

- Menú interactivo para seleccionar demos individuales o ejecutar todas
- Verificación de requisitos previos
- Pausas configurables entre demos
- Resumen final de patrones implementados

```bash
./run_all_demos.sh
```

### Demos Individuales

#### Disponibilidad

**`demo_retries.sh`** - Retry + Circuit Breaker + Fallback

- Demuestra reintentos automáticos (3 intentos, 200ms)
- Circuit Breaker con estados CLOSED/OPEN/HALF_OPEN
- Degradación elegante con fallback URLs
- Métricas de tasa de éxito

```bash
./demo_retries.sh
```

**`demo_circuit_breaker.sh`** - Circuit Breaker en detalle

- Estados detallados: CLOSED → OPEN → HALF_OPEN → CLOSED
- Configuración: 50% failure threshold, 10s wait
- Fail-fast cuando CB está abierto
- Recuperación automática

```bash
./demo_circuit_breaker.sh
```

**`demo_replication.sh`** - Blue/Green Deployment + Replicación

- Alta disponibilidad con 2 réplicas
- Simulación de fallo de réplica
- Zero-downtime deployment
- Balanceo de carga con NGINX

```bash
./demo_replication.sh
```

**`demo_health.sh`** - Health Endpoint Monitoring

- Spring Boot Actuator endpoints
- Monitoreo de componentes (DB, disco, etc.)
- OpenAPI/Swagger documentation
- Integración con sistemas de monitoreo

```bash
./demo_health.sh
```

#### Rendimiento

**`demo_performance.sh`** - Cache-Aside + Async Request-Reply

- Cache-Aside: Comparación cache miss vs cache hit
- Async: Procesamiento concurrente vs secuencial
- Métricas de latencia y throughput
- Mejora combinada (caché + async)

```bash
./demo_performance.sh
```

#### Seguridad

**`demo_security.sh`** - Patrones de Seguridad

- Validación de entrada con Bean Validation
- Rate limiting (5 intentos/minuto)
- JWT authentication
- Gatekeeper (NGINX)
- Gateway offloading

```bash
./demo_security.sh
```

## 🚀 Uso Rápido

### Opción 1: Script Maestro Interactivo

```bash
cd /home/clara/musify/scripts
./run_all_demos.sh
```

Selecciona del menú:

- `1-6`: Demos individuales
- `A`: Todas las demos (con pausas)
- `Q`: Todas las demos (modo rápido)
- `0`: Salir

### Opción 2: Ejecutar Demo Individual

```bash
cd /home/clara/musify/scripts
./demo_performance.sh  # O cualquier otro demo
```

### Opción 3: Ejecutar Todas las Demos Automáticamente

```bash
cd /home/clara/musify/scripts
# Modo con pausas (5s entre demos)
./run_all_demos.sh <<< "A"

# Modo rápido (sin pausas)
./run_all_demos.sh <<< "Q"
```

## 📊 Patrones Demostrados

### Disponibilidad (4 patrones)

1. ✅ **Retry** - `demo_retries.sh`
   - 3 intentos, 200ms entre intentos
   - Exponential backoff
2. ✅ **Circuit Breaker** - `demo_circuit_breaker.sh`
   - Threshold: 50% fallos
   - Wait: 10s en estado OPEN
3. ✅ **Rate Limiting** - `demo_security.sh`
   - 5 intentos/minuto en login
   - Protección contra fuerza bruta
4. ✅ **Health Monitoring** - `demo_health.sh`
   - Spring Boot Actuator
   - Monitoreo de componentes

### Rendimiento (2 patrones)

5. ✅ **Cache-Aside** - `demo_performance.sh`
   - Cacheo de búsquedas Spotify
   - Eviction cada 10 minutos
6. ✅ **Async Request-Reply** - `demo_performance.sh`
   - Thread pool: 5 core, 10 max
   - Procesamiento concurrente

### Seguridad (3 patrones)

7. ✅ **Gatekeeper** - `demo_security.sh`
   - NGINX como gateway único
   - TLS termination
8. ✅ **Gateway Offloading** - `demo_security.sh`
   - TLS, retries, health checks en NGINX
9. ✅ **Federated Identity** - `demo_security.sh`
   - JWT authentication
   - OAuth2 con Spotify API

### Modificabilidad (2 patrones)

10. ✅ **External Configuration Store** - Documentado en README
    - `.env` + `docker-compose.yaml`
    - `application.yaml` con placeholders
11. ✅ **Blue/Green Deployment** - `demo_replication.sh`
    - 2 réplicas backend
    - Zero-downtime updates

## 🔧 Requisitos Previos

### Software Necesario

- `bash` shell
- `curl` - Para hacer requests HTTP
- `jq` - Para parsear JSON
- `docker` - Para verificar contenedores

Instalar jq si no está disponible:

```bash
sudo apt-get install jq  # Ubuntu/Debian
```

### Sistema Ejecutándose

Los scripts requieren que Musify esté ejecutándose:

```bash
cd /home/clara/musify
docker compose up --build
```

Espera a que todos los servicios estén UP:

- ✅ backend-app-1
- ✅ backend-app-2
- ✅ nginx
- ✅ postgres
- ✅ flaky-service
- ✅ frontend

Verifica con:

```bash
curl http://localhost:8080/actuator/health
```

Debe retornar: `{"status":"UP",...}`

## 🎨 Características de los Scripts

### Output Coloreado

- 🟢 Verde: Éxitos
- 🔴 Rojo: Errores
- 🟡 Amarillo: Información
- 🔵 Azul: Headers/secciones
- 🟣 Magenta: Estados especiales

### Métricas Incluidas

- ⏱️ Tiempos de respuesta (ms)
- 📊 Tasas de éxito/fallo
- 🚀 Throughput (requests/segundo)
- 📈 Factores de mejora (speedup)

### Verificación Automática

- ✅ Sistema disponible
- ✅ Contenedores ejecutándose
- ✅ Base de datos UP
- ✅ Autenticación funcional

## 📝 Atributos de Calidad Demostrados

Cada script demuestra atributos de calidad específicos:

| Script                    | Atributos Demostrados                              |
| ------------------------- | -------------------------------------------------- |
| `demo_retries.sh`         | Disponibilidad, Resiliencia, Tolerancia a fallos   |
| `demo_circuit_breaker.sh` | Disponibilidad, Resiliencia, Rendimiento           |
| `demo_security.sh`        | Seguridad, Resistencia a ataques, Confidencialidad |
| `demo_performance.sh`     | Rendimiento, Escalabilidad, Eficiencia, Throughput |
| `demo_health.sh`          | Disponibilidad, Observabilidad, Mantenibilidad     |
| `demo_replication.sh`     | Disponibilidad, Escalabilidad, Modificabilidad     |

## 🔍 Ejemplos de Uso

### Ver solo la demo de rendimiento

```bash
./demo_performance.sh
```

### Ejecutar todas las demos con pausas personalizadas

```bash
PAUSE_BETWEEN_DEMOS=10 ./run_all_demos.sh <<< "A"
```

### Cambiar URL del backend

```bash
BASE_URL="http://localhost:9090" ./demo_health.sh
```

### Deshabilitar colores

```bash
COLORS_ENABLED=false ./demo_retries.sh
```

## 📚 Documentación Relacionada

- **README.md** - Documentación general del proyecto
- **PATRONES_IMPLEMENTADOS.md** - Explicación detallada de patrones (español)
- **DOCUMENTACION_TFU_PARTE1.md** - Entregable académico Parte 1
- **diagramas/\*.pu** - Diagramas UML en PlantUML
- **.github/copilot-instructions.md** - Guía para AI coding agents

## 🎯 Entregables de la Asignatura

Estos scripts demuestran:

**Parte 2 - Implementación:**

- ✅ REST API funcional testable
- ✅ Deployment con Docker
- ✅ Scripts de demostración de patrones

Los scripts prueban cómo cada patrón logra los atributos de calidad esperados mediante:

1. **Tests automatizados** con métricas cuantificables
2. **Comparaciones** antes/después de aplicar patrones
3. **Simulaciones** de condiciones adversas (fallos, carga, ataques)
4. **Verificación** de configuración en código

## 🐛 Troubleshooting

### Error: "Backend no está disponible"

```bash
# Verifica que Docker esté ejecutándose
docker compose ps

# Reinicia el sistema
docker compose down
docker compose up --build
```

### Error: "jq: command not found"

```bash
sudo apt-get install jq
```

### Error: "Permission denied"

```bash
chmod +x *.sh
```

### Error: "Token de autenticación no se pudo obtener"

```bash
# Verifica que exista el usuario de prueba
docker exec -it postgres psql -U musify -d musify -c "SELECT email FROM users WHERE email='user@test.com';"

# Si no existe, carga los datos de prueba
docker exec -i postgres psql -U musify -d musify < /path/to/database/03-auth-test-data.sql
```

## 📞 Soporte

Para problemas con los scripts:

1. Verifica que el sistema esté ejecutándose: `docker compose ps`
2. Revisa logs: `docker compose logs backend-app-1`
3. Verifica health: `curl http://localhost:8080/actuator/health`
4. Consulta documentación: `README.md`, `PATRONES_IMPLEMENTADOS.md`

---

**Última actualización**: 30 de octubre de 2025
**Autor**: Clara S. N.
**Asignatura**: TFU Unidad 4 - Atributos de Calidad
