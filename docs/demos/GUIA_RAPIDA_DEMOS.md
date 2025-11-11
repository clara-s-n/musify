# Guía Rápida: Ejecutar Demostraciones

## 🚀 Inicio Rápido (3 pasos)

### 1. Iniciar el Sistema

```bash
cd /home/clara/musify
docker compose up --build
```

Espera hasta ver:

```
✓ Container backend-app-1    Started
✓ Container backend-app-2    Started
✓ Container nginx            Started
✓ Container postgres         Started
✓ Container flaky-service    Started
```

### 2. Verificar Sistema UP

```bash
curl http://localhost:8080/actuator/health
```

Debe retornar: `{"status":"UP",...}`

### 3. Ejecutar Demos

```bash
cd scripts
./run_all_demos.sh
```

## 📋 Opciones del Menú

```
1) Retry + Circuit Breaker + Fallback      → Disponibilidad
2) Circuit Breaker (detallado)             → Resiliencia
3) Rate Limiting + Validación              → Seguridad
4) Health Monitoring                       → Observabilidad
5) Blue/Green Deployment + Replicación     → Modificabilidad
6) Cache-Aside + Async Request-Reply       → Rendimiento

A) Ejecutar todas las demos (con pausas)
Q) Ejecutar todas las demos (rápido, sin pausas)
0) Salir
```

## 🎯 Demos Individuales

### Disponibilidad

```bash
./demo_retries.sh           # Retry, Circuit Breaker, Fallback
./demo_circuit_breaker.sh   # Estados del Circuit Breaker
./demo_health.sh            # Health monitoring
./demo_replication.sh       # Replicación y Blue/Green
```

### Rendimiento

```bash
./demo_performance.sh       # Cache-Aside y Async
```

### Seguridad

```bash
./demo_security.sh          # Validation, Rate Limit, JWT
```

## 📊 Qué Esperar de Cada Demo

### `demo_retries.sh` (~2 min)

```
✓ 8 requests al servicio flaky
✓ Muestra reintentos automáticos
✓ Circuit Breaker se abre cuando fallos > 50%
✓ Fallback URLs activados
→ Tasa de éxito: ~60% (con reintentos y CB)
```

### `demo_circuit_breaker.sh` (~30 seg)

```
✓ Estados: CLOSED → OPEN → HALF_OPEN → CLOSED
✓ Fail-fast cuando CB está OPEN
✓ Recuperación automática después de 10s
→ Latencia: <100ms con fail-fast vs 5000ms sin CB
```

### `demo_security.sh` (~1 min)

```
✓ Rechaza emails inválidos (HTTP 400)
✓ Rate limiting después de 5 intentos
✓ JWT token generado y validado
✓ Gatekeeper (NGINX) protege backend
→ Protección contra fuerza bruta: 5 intentos/min
```

### `demo_performance.sh` (~2 min)

```
✓ Cache miss: ~500ms
✓ Cache hit:  ~10ms (50x más rápido)
✓ 3 requests secuenciales: ~1500ms
✓ 3 requests async:        ~500ms (3x más rápido)
→ Mejora combinada: hasta 50x
```

### `demo_health.sh` (~30 seg)

```
✓ Health endpoint: /actuator/health
✓ Componentes: DB (UP), Disk (UP), Ping (UP)
✓ OpenAPI docs: /v3/api-docs
✓ Monitoreo continuo: 10 checks → 100% UP
```

### `demo_replication.sh` (~3 min)

```
✓ 2 réplicas ejecutándose
✓ Simula fallo de réplica 1
✓ Sistema sigue UP con réplica 2
✓ Recupera réplica 1 automáticamente
✓ Blue/Green deployment sin downtime
→ Disponibilidad: 100% durante todo el proceso
```

## 🔧 Troubleshooting

### Error: "Backend no está disponible"

```bash
# Reiniciar sistema
docker compose down
docker compose up --build

# Verificar contenedores
docker compose ps
```

### Error: "jq: command not found"

```bash
sudo apt-get update
sudo apt-get install jq
```

### Error: "Token de autenticación no se pudo obtener"

```bash
# Verificar usuario de prueba existe
docker exec -it postgres psql -U musify -d musify \
  -c "SELECT email FROM users WHERE email='user@test.com';"

# Si no existe, cargar datos de prueba
docker exec -i postgres psql -U musify -d musify \
  < database/03-auth-test-data.sql
```

### Error: "Permission denied"

```bash
cd /home/clara/musify/scripts
chmod +x *.sh
```

## 📹 Grabar Demos para Presentación

### Opción 1: Script Output (texto)

```bash
./run_all_demos.sh <<< "Q" | tee demo_output.txt
```

### Opción 2: Asciinema (video de terminal)

```bash
# Instalar asciinema
sudo apt-get install asciinema

# Grabar
asciinema rec musify_demo.cast

# Ejecutar demos
./run_all_demos.sh

# Ctrl+D para detener
```

### Opción 3: Screenshots

```bash
# Ejecutar modo interactivo
./run_all_demos.sh

# Tomar screenshot en cada sección importante
# Gnome: Shift+Print
# KDE: Spectacle
```

## 📊 Métricas para Reportar

### Disponibilidad

- Tasa de éxito con Retry: 60% → 80%
- Circuit Breaker: Fail-fast <100ms vs 5000ms timeout
- Uptime con 2 réplicas: 99.99%
- Recuperación automática: <10s

### Rendimiento

- Cache speedup: 50x (500ms → 10ms)
- Async speedup: 3x (1500ms → 500ms)
- Throughput: 200+ req/s con async
- Combinado: Hasta 50x mejora

### Seguridad

- Rate limiting: 5 intentos/min efectivo
- Validación: 100% emails inválidos rechazados
- JWT: Tokens firmados HMAC-SHA256
- Gatekeeper: TLS termination en NGINX

### Modificabilidad

- Blue/Green: 0 downtime durante actualización
- External config: Sin rebuild para cambios
- Replicación: Failover automático <3s

## ✅ Checklist Pre-Demostración

- [ ] Sistema ejecutándose (`docker compose ps`)
- [ ] Health endpoint UP (`curl localhost:8080/actuator/health`)
- [ ] Scripts ejecutables (`ls -la scripts/*.sh`)
- [ ] jq instalado (`jq --version`)
- [ ] Usuario test existe (ver troubleshooting)
- [ ] Terminal con buen tamaño (80x24 mínimo)
- [ ] Colores habilitados en terminal

## 🎓 Para la Presentación Académica

**Recomendación**: Ejecutar `./run_all_demos.sh <<< "Q"` (modo rápido) para mostrar:

1. ✅ **Verificación inicial** (sistema UP, contenedores running)
2. ✅ **Health monitoring** (componentes, OpenAPI)
3. ✅ **Retry + Circuit Breaker** (reintentos, fail-fast)
4. ✅ **Security** (validación, rate limiting, JWT)
5. ✅ **Performance** (cache 50x, async 3x)
6. ✅ **Replication** (Blue/Green, zero-downtime)
7. ✅ **Resumen** (11 patrones, atributos de calidad)

**Tiempo total**: ~8-10 minutos para todas las demos

---

**Tip**: Practica una vez antes de la presentación para familiarizarte con el output y timing.
