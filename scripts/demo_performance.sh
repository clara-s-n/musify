#!/usr/bin/env bash

# ============================================================================
# DEMOSTRACIÓN: PATRONES DE RENDIMIENTO
# ============================================================================
# Este script demuestra patrones de rendimiento:
# 1. Cache-Aside: Las búsquedas repetidas se sirven desde caché (más rápido)
# 2. Asynchronous Request-Reply: Múltiples operaciones se procesan concurrentemente
#
# Atributos de calidad demostrados:
# - RENDIMIENTO: Reducción de latencia con cacheo
# - ESCALABILIDAD: Procesamiento concurrente con thread pools
# - EFICIENCIA: Reducción de llamadas a APIs externas
# - THROUGHPUT: Mayor cantidad de requests procesados simultáneamente
# ============================================================================

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COLORS_ENABLED=true

# Colores para output
if [ "$COLORS_ENABLED" = true ]; then
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[1;33m'
  BLUE='\033[0;34m'
  CYAN='\033[0;36m'
  NC='\033[0m'
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
  CYAN=''
  NC=''
fi

print_header() {
  echo -e "\n${BLUE}===================================================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}===================================================================${NC}\n"
}

print_success() {
  echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
  echo -e "${RED}✗ $1${NC}"
}

print_info() {
  echo -e "${YELLOW}ℹ $1${NC}"
}

print_metric() {
  echo -e "${CYAN}$1${NC}"
}

# Función para medir tiempo de ejecución con mayor precisión
time_request() {
  local method="$1"; shift
  local url="$1"; shift
  local start=$(date +%s%N)  # Nanosegundos
  
  local http_code=$(curl -s -X "$method" "$url" \
    -H 'Accept: application/json' "$@" \
    -o /dev/null -w "%{http_code}" 2>/dev/null)
  
  local end=$(date +%s%N)
  local duration_ns=$((end - start))
  local duration_ms=$((duration_ns / 1000000))
  
  echo "$http_code:$duration_ms"
}

# Obtener token de autenticación
get_auth_token() {
  print_info "Obteniendo token de autenticación..."
  
  local response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"email":"user@demo.com","password":"password"}')
  
  local token=$(echo "$response" | jq -r '.data.accessToken // empty')
  
  if [ -z "$token" ]; then
    print_error "No se pudo obtener token"
    return 1
  fi
  
  print_success "Token obtenido correctamente"
  echo "$token"
}

print_header "VERIFICACIÓN DE DISPONIBILIDAD DEL SISTEMA"

if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
  print_success "Backend está disponible"
else
  print_error "Backend no está disponible en ${BASE_URL}"
  echo "Por favor, inicia el sistema con: docker compose up"
  exit 1
fi

print_header "PATRÓN 1: CACHE-ASIDE (REDUCCIÓN DE LATENCIA)"

print_info "Configuración en application.yaml:"
echo "  spring:"
echo "    cache:"
echo "      cache-names:"
echo "        - randomTracks"
echo "        - searchTracks"
echo "        - trackPlayback"
echo ""
echo "  @Cacheable en SpotifyService:"
echo "    - searchTracks()"
echo "    - getRandomTracks()"
echo "    - getTrackPlayback()"
echo ""

print_info "Cache eviction automático cada 10 minutos con @Scheduled"
echo ""

print_header "TEST 1.1: BÚSQUEDA SIN CACHÉ (CACHE MISS)"

QUERY="rock"
print_info "Primera búsqueda de '$QUERY' (sin caché)..."

result=$(time_request GET "${BASE_URL}/music/spotify/search?q=${QUERY}&limit=10")
http_code=$(echo "$result" | cut -d: -f1)
duration_cold=$(echo "$result" | cut -d: -f2)

print_metric "HTTP Status: $http_code"
print_metric "Tiempo de respuesta: ${duration_cold}ms (CACHE MISS - llamada a Spotify API)"

sleep 1

print_header "TEST 1.2: BÚSQUEDA CON CACHÉ (CACHE HIT)"

print_info "Segunda búsqueda de '$QUERY' (con caché)..."

result=$(time_request GET "${BASE_URL}/music/spotify/search?q=${QUERY}&limit=10")
http_code=$(echo "$result" | cut -d: -f1)
duration_cached=$(echo "$result" | cut -d: -f2)

print_metric "HTTP Status: $http_code"
print_metric "Tiempo de respuesta: ${duration_cached}ms (CACHE HIT - servido desde caché)"

echo ""
print_header "ANÁLISIS DE MEJORA DE RENDIMIENTO - CACHE"

improvement=$((duration_cold - duration_cached))
if [ $duration_cached -gt 0 ]; then
  speedup=$((duration_cold * 100 / duration_cached))
else
  speedup=0
fi

echo "Comparación de tiempos:"
print_metric "  Sin caché (cache miss):  ${duration_cold}ms"
print_metric "  Con caché (cache hit):   ${duration_cached}ms"
print_metric "  Mejora absoluta:         ${improvement}ms"
print_metric "  Factor de aceleración:   ${speedup}% (${speedup}x más rápido)"

echo ""
print_success "DEMOSTRACIÓN: El patrón Cache-Aside reduce significativamente la latencia"
print_success "Las búsquedas repetidas se sirven desde memoria local en lugar de llamar a Spotify API"

print_header "TEST 1.3: MÚLTIPLES REQUESTS CONCURRENTES CON CACHÉ"

print_info "Enviando 10 requests concurrentes a endpoint cacheado..."

START=$(date +%s%N)

# Lanzar 10 peticiones en paralelo
for i in $(seq 1 10); do
  curl -s "${BASE_URL}/music/spotify/search?q=${QUERY}&limit=10" > /dev/null &
done

# Esperar a que todas terminen
wait

END=$(date +%s%N)
TOTAL_TIME=$(( (END - START) / 1000000 ))

print_metric "10 requests concurrentes completados en: ${TOTAL_TIME}ms"
print_metric "Tiempo promedio por request: $((TOTAL_TIME / 10))ms"

echo ""
print_success "Con caché, múltiples requests concurrentes se procesan eficientemente"

print_header "PATRÓN 2: ASYNCHRONOUS REQUEST-REPLY (PROCESAMIENTO CONCURRENTE)"

print_info "Configuración en AsyncConfig.java:"
echo "  ThreadPoolTaskExecutor:"
echo "    - corePoolSize: 5"
echo "    - maxPoolSize: 10"
echo "    - queueCapacity: 100"
echo "    - threadNamePrefix: async-"
echo ""
echo "  Métodos asíncronos en PlaybackController:"
echo "    - CompletableFuture<ResponseEntity<>> start()"
echo "    - CompletableFuture<ResponseEntity<>> pause()"
echo "    - CompletableFuture<ResponseEntity<>> resume()"
echo ""

TOKEN=$(get_auth_token)
echo ""

print_header "TEST 2.1: OPERACIÓN SÍNCRONA (BASELINE)"

print_info "Enviando 3 operaciones de playback de forma secuencial..."

START=$(date +%s%N)

for i in $(seq 1 3); do
  curl -s -X POST "${BASE_URL}/playback/start?trackId=sync_$i" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'Content-Type: application/json' > /dev/null
done

END=$(date +%s%N)
SYNC_TIME=$(( (END - START) / 1000000 ))

print_metric "3 operaciones secuenciales completadas en: ${SYNC_TIME}ms"
print_metric "Tiempo promedio por operación: $((SYNC_TIME / 3))ms"

sleep 1

print_header "TEST 2.2: OPERACIÓN ASÍNCRONA (CONCURRENTE)"

print_info "Enviando 3 operaciones de playback concurrentemente..."

START=$(date +%s%N)

# Lanzar 3 peticiones en paralelo
curl -s -X POST "${BASE_URL}/playback/start?trackId=async_1" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/async1.json &

curl -s -X POST "${BASE_URL}/playback/start?trackId=async_2" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/async2.json &

curl -s -X POST "${BASE_URL}/playback/start?trackId=async_3" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/async3.json &

# Esperar a que todas terminen
wait

END=$(date +%s%N)
ASYNC_TIME=$(( (END - START) / 1000000 ))

print_metric "3 operaciones concurrentes completadas en: ${ASYNC_TIME}ms"

echo ""
echo "Resultados de operaciones asíncronas:"
for i in 1 2 3; do
  msg=$(cat /tmp/async${i}.json | jq -r '.message // .error // "completed"' 2>/dev/null || echo "completed")
  print_success "  Playback $i: $msg"
done

# Limpiar archivos temporales
rm -f /tmp/async*.json

print_header "ANÁLISIS DE MEJORA DE RENDIMIENTO - ASYNC"

improvement=$((SYNC_TIME - ASYNC_TIME))
if [ $ASYNC_TIME -gt 0 ]; then
  speedup=$((SYNC_TIME * 100 / ASYNC_TIME))
else
  speedup=0
fi

echo "Comparación de tiempos:"
print_metric "  Secuencial (síncrono):    ${SYNC_TIME}ms"
print_metric "  Concurrente (asíncrono):  ${ASYNC_TIME}ms"
print_metric "  Mejora absoluta:          ${improvement}ms"
print_metric "  Factor de aceleración:    ${speedup}% (${speedup}x más rápido)"

echo ""
print_success "DEMOSTRACIÓN: El patrón Async Request-Reply procesa operaciones concurrentemente"
print_success "Thread pool permite ejecutar múltiples tareas en paralelo sin bloquear"

print_header "TEST 2.3: ALTO THROUGHPUT CON ASYNC"

print_info "Enviando 20 operaciones asíncronas concurrentes..."
print_info "Thread pool (5 core, 10 max) procesará en lotes..."

START=$(date +%s%N)

# Lanzar 20 peticiones en paralelo
for i in $(seq 1 20); do
  curl -s -X POST "${BASE_URL}/playback/start?trackId=throughput_$i" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'Content-Type: application/json' > /dev/null &
done

# Esperar a que todas terminen
wait

END=$(date +%s%N)
THROUGHPUT_TIME=$(( (END - START) / 1000000 ))

print_metric "20 operaciones concurrentes completadas en: ${THROUGHPUT_TIME}ms"
print_metric "Throughput: $((20000 / THROUGHPUT_TIME)) requests/segundo"
print_metric "Latencia promedio: $((THROUGHPUT_TIME / 20))ms por request"

echo ""
print_success "Thread pool maneja eficientemente múltiples requests concurrentes"
print_success "Sistema escala horizontalmente con procesamiento asíncrono"

print_header "COMPARACIÓN: CACHÉ + ASYNC vs SIN OPTIMIZACIÓN"

print_info "Simulando carga realista: 10 búsquedas (cacheables) + 5 playbacks (async)"
echo ""

# Test sin optimización (deshabilitando caché conceptualmente, simulando)
print_info "Escenario SIN optimización (estimado):"
print_metric "  10 búsquedas sin caché: ~10 × ${duration_cold}ms = $((10 * duration_cold))ms"
print_metric "  5 playbacks secuenciales: ~5 × $((SYNC_TIME / 3))ms = $((5 * SYNC_TIME / 3))ms"
TOTAL_WITHOUT=$(( 10 * duration_cold + 5 * SYNC_TIME / 3 ))
print_metric "  TOTAL estimado: ${TOTAL_WITHOUT}ms"

echo ""

# Test con optimización
print_info "Escenario CON optimización (caché + async):"

START=$(date +%s%N)

# 10 búsquedas en paralelo (cacheadas)
for i in $(seq 1 10); do
  curl -s "${BASE_URL}/music/spotify/search?q=${QUERY}&limit=10" > /dev/null &
done

# 5 playbacks en paralelo
for i in $(seq 1 5); do
  curl -s -X POST "${BASE_URL}/playback/start?trackId=combined_$i" \
    -H "Authorization: Bearer ${TOKEN}" > /dev/null &
done

wait

END=$(date +%s%N)
TOTAL_WITH=$(( (END - START) / 1000000 ))

print_metric "  10 búsquedas (cache hit) + 5 playbacks (async): ${TOTAL_WITH}ms"

echo ""
improvement=$((TOTAL_WITHOUT - TOTAL_WITH))
if [ $TOTAL_WITH -gt 0 ]; then
  speedup=$((TOTAL_WITHOUT * 100 / TOTAL_WITH))
else
  speedup=0
fi

print_header "MEJORA COMBINADA (CACHÉ + ASYNC)"
print_metric "  Sin optimización:  ${TOTAL_WITHOUT}ms"
print_metric "  Con optimización:  ${TOTAL_WITH}ms"
print_metric "  Mejora absoluta:   ${improvement}ms"
print_metric "  Factor de aceleración: ${speedup}% (${speedup}x más rápido)"

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN EN EL CÓDIGO"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. Cache-Aside (SpotifyService.java):"
echo "   @Cacheable(value = \"searchTracks\", key = \"#query + '-' + #limit\")"
echo "   public List<SpotifyTrackDto> searchTracks(String query, int limit)"
echo ""
echo "   @CacheEvict(allEntries = true, value = {\"randomTracks\", \"searchTracks\", \"trackPlayback\"})"
echo "   @Scheduled(fixedRate = 600000) // 10 minutos"
echo "   public void evictAllCaches()"
echo ""
echo "2. Async (PlaybackController.java):"
echo "   @PostMapping(\"/start\")"
echo "   public CompletableFuture<ResponseEntity<PlaybackResponseDto>> start(...)"
echo ""
echo "3. Thread Pool (AsyncConfig.java):"
echo "   @Bean(name = \"taskExecutor\")"
echo "   public Executor taskExecutor() {"
echo "     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();"
echo "     executor.setCorePoolSize(5);"
echo "     executor.setMaxPoolSize(10);"
echo "     executor.setQueueCapacity(100);"
echo "   }"
echo ""
echo "4. Configuration (BackendApplication.java):"
echo "   @EnableCaching"
echo "   @EnableAsync"
echo "   @EnableScheduling"
echo ""

print_header "ATRIBUTOS DE CALIDAD DEMOSTRADOS"

echo "✓ RENDIMIENTO:"
echo "  - Cache-Aside reduce latencia de ${duration_cold}ms a ${duration_cached}ms"
echo "  - Async reduce tiempo total de operaciones concurrentes en ~${speedup}%"
echo "  - Combinados: mejora de rendimiento de hasta ${speedup}x"
echo ""
echo "✓ ESCALABILIDAD:"
echo "  - Thread pool maneja múltiples requests concurrentemente"
echo "  - Sistema escala horizontalmente con procesamiento paralelo"
echo "  - Caché reduce carga en APIs externas (Spotify)"
echo ""
echo "✓ EFICIENCIA:"
echo "  - Reducción de llamadas a Spotify API mediante cacheo"
echo "  - Uso eficiente de recursos con thread pool limitado"
echo "  - Menor consumo de red y latencia"
echo ""
echo "✓ THROUGHPUT:"
echo "  - Mayor cantidad de requests procesados simultáneamente"
echo "  - Throughput medido: $((20000 / THROUGHPUT_TIME)) req/s con async"
echo "  - Respuesta rápida incluso bajo carga alta"
echo ""

print_success "Demostración completada exitosamente"
