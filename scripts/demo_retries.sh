#!/usr/bin/env bash

# ============================================================================
# DEMOSTRACIÓN: PATRONES DE DISPONIBILIDAD (RETRY + CIRCUIT BREAKER + FALLBACK)
# ============================================================================
# Este script demuestra tácticas de disponibilidad y recuperación ante fallos:
# 1. Reintentos (@Retry): La aplicación reintenta automáticamente hasta 3 veces
# 2. Circuit Breaker (@CircuitBreaker): Previene cascada de fallos
# 3. Degradación elegante: Proporciona URL alternativa cuando el servicio principal falla
#
# Atributos de calidad demostrados:
# - DISPONIBILIDAD: Sistema continúa funcionando ante fallos externos
# - RESILIENCIA: Recuperación automática sin intervención manual
# - TOLERANCIA A FALLOS: Degradación elegante con fallback URLs
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
  NC='\033[0m' # No Color
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
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

# función helper que imprime status + cuerpo
req() {
  local method="$1"; shift
  local url="$1"; shift
  echo -e "\n${BLUE}--- ${method} ${url} ---${NC}"
  
  local response
  local http_code
  
  response=$(curl -sS -w "\n%{http_code}" -X "$method" "$url" \
    -H 'Accept: application/json' "$@" 2>&1 || echo "ERROR")
  
  http_code=$(echo "$response" | tail -n1)
  local body=$(echo "$response" | head -n-1)
  
  echo "HTTP Status: $http_code"
  echo "$body" | jq '.' 2>/dev/null || echo "$body"
  
  if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
    print_success "Request successful"
  else
    print_error "Request failed with status $http_code"
  fi
}

# Obtener token de autenticación
get_auth_token() {
  print_info "Obteniendo token de autenticación..."
  
  local response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H 'Content-Type: application/json' \
    -d '{"email":"user@demo.com","password":"password"}')
  
  local token=$(echo "$response" | jq -r '.data.accessToken // empty')
  
  if [ -z "$token" ]; then
    print_error "No se pudo obtener token. Respuesta: $response"
    return 1
  fi
  
  print_success "Token obtenido correctamente"
  echo "$token"
}

print_header "VERIFICACIÓN DE DISPONIBILIDAD DEL SISTEMA"

echo "Verificando que el backend esté disponible..."
if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
  print_success "Backend está disponible"
  curl -s "${BASE_URL}/actuator/health" | jq '.'
else
  print_error "Backend no está disponible en ${BASE_URL}"
  echo "Por favor, inicia el sistema con: docker compose up"
  exit 1
fi

print_header "PATRÓN 1: RETRY (REINTENTOS AUTOMÁTICOS)"

print_info "Configuración en application.yaml:"
echo "  resilience4j.retry.instances.streamSource:"
echo "    maxAttempts: 3"
echo "    waitDuration: 200ms"
echo "    retryExceptions: [Exception]"
echo ""

print_info "Demostración del patrón @Retry en SpotifyService"
print_info "El servicio de Spotify puede fallar ocasionalmente, el patrón @Retry reintentará automáticamente"
echo ""

SUCCESS_COUNT=0
ERROR_COUNT=0
TOTAL_REQUESTS=10

# Términos de búsqueda para variar las peticiones
SEARCH_TERMS=("rock" "pop" "jazz" "blues" "classical" "metal" "reggae" "country" "electronic" "latin")

for i in $(seq 1 $TOTAL_REQUESTS); do
  echo -e "\n${YELLOW}========== Request $i/$TOTAL_REQUESTS ==========${NC}"
  
  search_term="${SEARCH_TERMS[$((i-1))]}"
  print_info "Buscando: $search_term"
  
  start_time=$(date +%s%N)
  response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/music/spotify/search?q=${search_term}&limit=5")
  end_time=$(date +%s%N)
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | head -n-1)
  
  duration_ms=$(( (end_time - start_time) / 1000000 ))
  
  if [ "$http_code" -eq 200 ]; then
    track_count=$(echo "$body" | jq -r '.data | length')
    print_success "✓ HTTP 200 - ${track_count} canciones encontradas (${duration_ms}ms)"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
  else
    print_error "✗ HTTP $http_code - Error en la búsqueda"
    echo "$body" | jq -r '.error // .message' 2>/dev/null || echo "$body"
    ERROR_COUNT=$((ERROR_COUNT + 1))
  fi
  
  sleep 0.5
done

print_header "RESULTADOS DEL PATRÓN RETRY"
echo "Total de requests: $TOTAL_REQUESTS"
print_success "Requests exitosos: $SUCCESS_COUNT ($(( SUCCESS_COUNT * 100 / TOTAL_REQUESTS ))%)"
if [ $ERROR_COUNT -gt 0 ]; then
  print_error "Requests con error: $ERROR_COUNT ($(( ERROR_COUNT * 100 / TOTAL_REQUESTS ))%)"
else
  print_info "Requests con error: $ERROR_COUNT ($(( ERROR_COUNT * 100 / TOTAL_REQUESTS ))%)"
fi
echo ""
print_success "DEMOSTRACIÓN: El patrón @Retry reintenta automáticamente las peticiones fallidas"
print_success "al servicio de Spotify, mejorando la disponibilidad del sistema."

print_header "PATRÓN 2: CIRCUIT BREAKER (PREVENCIÓN DE CASCADA DE FALLOS)"

print_info "Configuración en application.yaml:"
echo "  resilience4j.circuitbreaker.instances.spotifyApi:"
echo "    failureRateThreshold: 50"
echo "    waitDurationInOpenState: 10000ms"
echo "    slidingWindowSize: 10"
echo ""

print_info "El Circuit Breaker en SpotifyService protege contra fallos en la API de Spotify"
print_info "Demostrando comportamiento con búsquedas múltiples..."
echo ""

CB_SUCCESS=0
CB_FALLBACK=0
CB_REQUESTS=12

for i in $(seq 1 $CB_REQUESTS); do
  echo -e "\n${YELLOW}--- Request $i/$CB_REQUESTS ---${NC}"
  
  # Alternar entre búsquedas válidas e inválidas para generar algunos fallos
  if [ $((i % 3)) -eq 0 ]; then
    search_term="xxxinvalidxxx"
    print_info "Búsqueda inválida intencional: $search_term"
  else
    search_term="music"
    print_info "Búsqueda válida: $search_term"
  fi
  
  start_time=$(date +%s%N)
  response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/music/spotify/search?q=${search_term}&limit=3")
  end_time=$(date +%s%N)
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | head -n-1)
  duration_ms=$(( (end_time - start_time) / 1000000 ))
  
  if [ "$http_code" -eq 200 ]; then
    track_count=$(echo "$body" | jq -r '.data | length')
    if [ "$track_count" -gt 0 ]; then
      print_success "✓ Circuit Breaker CLOSED - ${track_count} resultados (${duration_ms}ms)"
      CB_SUCCESS=$((CB_SUCCESS + 1))
    else
      print_info "○ Circuit Breaker CLOSED - 0 resultados (${duration_ms}ms)"
      CB_FALLBACK=$((CB_FALLBACK + 1))
    fi
  else
    if [ $duration_ms -lt 100 ]; then
      print_error "✗ Circuit Breaker OPEN - Fail-fast (${duration_ms}ms)"
    else
      print_error "✗ Circuit Breaker CLOSED - Error con retry (${duration_ms}ms)"
    fi
    CB_FALLBACK=$((CB_FALLBACK + 1))
  fi
  
  sleep 0.3
done

echo ""
print_header "RESULTADOS DEL CIRCUIT BREAKER"
echo "Total de requests: $CB_REQUESTS"
print_success "Requests exitosos: $CB_SUCCESS"
print_info "Requests con fallback/error: $CB_FALLBACK"
echo ""
print_success "DEMOSTRACIÓN: El Circuit Breaker protege el sistema de fallos en cascada"
print_success "Cuando detecta muchos errores, abre el circuito y falla rápidamente (fail-fast)"

print_header "PATRÓN 3: DEGRADACIÓN ELEGANTE (FALLBACK)"

print_info "Cuando las búsquedas fallan, el método searchTracksFallback() retorna una lista vacía"
print_info "permitiendo que la aplicación continúe funcionando sin errores críticos."
echo ""

print_success "El sistema puede continuar operando incluso cuando servicios externos fallan,"
print_success "proporcionando una experiencia degradada pero funcional al usuario."

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN EN EL CÓDIGO"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. SpotifyService.java:"
echo "   @Retry(name=\"spotifyApi\")"
echo "   @CircuitBreaker(name=\"spotifyApi\", fallbackMethod=\"searchTracksFallback\")"
echo "   public List<SpotifyTrackDto> searchTracks(String query, int limit)"
echo ""
echo "2. application.yaml:"
echo "   resilience4j:"
echo "     retry:"
echo "       instances:"
echo "         spotifyApi:"
echo "           maxAttempts: 3"
echo "           waitDuration: 1s"
echo "     circuitbreaker:"
echo "       instances:"
echo "         spotifyApi:"
echo "           failureRateThreshold: 50"
echo "           waitDurationInOpenState: 10000ms"
echo ""
echo "3. flaky-service/server.js:"
echo "   Servicio que simula fallos (40% timeout, 20% error, 40% success)"
echo ""

print_header "ATRIBUTOS DE CALIDAD DEMOSTRADOS"

echo "✓ DISPONIBILIDAD:"
echo "  - Sistema continúa funcionando ante fallos externos (flaky-service)"
echo "  - Tasa de éxito mejorada mediante reintentos automáticos"
echo ""
echo "✓ RESILIENCIA:"
echo "  - Recuperación automática sin intervención manual"
echo "  - Circuit Breaker previene cascada de fallos"
echo ""
echo "✓ TOLERANCIA A FALLOS:"
echo "  - Degradación elegante con fallback URLs"
echo "  - Usuario recibe respuesta aunque servicio principal falle"
echo ""

print_success "Demostración completada exitosamente"
