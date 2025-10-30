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

TOKEN=$(get_auth_token)

print_info "Iniciando playback con servicio flaky-service (40% timeout, 20% error, 40% success)"
print_info "El patrón @Retry reintentará hasta 3 veces automáticamente..."
echo ""

SUCCESS_COUNT=0
FALLBACK_COUNT=0
TOTAL_REQUESTS=8

for i in $(seq 1 $TOTAL_REQUESTS); do
  echo -e "\n${YELLOW}========== Intento $i/$TOTAL_REQUESTS ==========${NC}"
  
  response=$(curl -s -X POST "${BASE_URL}/playback/start?trackId=track_$i" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'Content-Type: application/json')
  
  echo "$response" | jq '.'
  
  # Verificar si fue exitoso o usó fallback
  if echo "$response" | jq -e '.streamUrl' > /dev/null 2>&1; then
    stream_url=$(echo "$response" | jq -r '.streamUrl')
    
    if [[ "$stream_url" == *"flaky-service"* ]]; then
      print_success "Éxito con flaky-service (después de reintentos si fue necesario)"
      SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    elif [[ "$stream_url" == *"fallback"* ]]; then
      print_info "Fallback URL activado (Circuit Breaker abierto o max retries alcanzado)"
      FALLBACK_COUNT=$((FALLBACK_COUNT + 1))
    fi
  fi
  
  sleep 1
done

print_header "RESULTADOS DEL PATRÓN RETRY"
echo "Total de requests: $TOTAL_REQUESTS"
echo "Éxitos con flaky-service: $SUCCESS_COUNT ($(( SUCCESS_COUNT * 100 / TOTAL_REQUESTS ))%)"
echo "Fallbacks activados: $FALLBACK_COUNT ($(( FALLBACK_COUNT * 100 / TOTAL_REQUESTS ))%)"
echo ""
print_success "DEMOSTRACIÓN: Aunque flaky-service falla 60% del tiempo,"
print_success "el patrón Retry aumenta la tasa de éxito reintentando automáticamente."

print_header "PATRÓN 2: CIRCUIT BREAKER (PREVENCIÓN DE CASCADA DE FALLOS)"

print_info "Configuración en application.yaml:"
echo "  resilience4j.circuitbreaker.instances.streamSource:"
echo "    failureRateThreshold: 50"
echo "    waitDurationInOpenState: 10000ms"
echo "    slidingWindowSize: 10"
echo ""

print_info "Generando fallos consecutivos para abrir el Circuit Breaker..."
echo ""

CB_REQUESTS=15

for i in $(seq 1 $CB_REQUESTS); do
  echo -e "\n${YELLOW}--- Request $i/$CB_REQUESTS ---${NC}"
  
  response=$(curl -s -X POST "${BASE_URL}/playback/start?trackId=cb_test_$i" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H 'Content-Type: application/json')
  
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"fallback"* ]]; then
    print_info "Circuit Breaker OPEN - Fallback URL retornado inmediatamente"
  elif [[ "$stream_url" == *"flaky-service"* ]]; then
    print_success "Circuit Breaker CLOSED - Request enviado a flaky-service"
  fi
  
  sleep 0.5
done

echo ""
print_success "DEMOSTRACIÓN: Cuando el Circuit Breaker se abre por exceso de fallos,"
print_success "las peticiones fallan rápidamente (fail-fast) retornando fallback,"
print_success "evitando sobrecargar el servicio problemático."

print_header "PATRÓN 3: DEGRADACIÓN ELEGANTE (FALLBACK)"

print_info "Cuando todos los reintentos fallan o el Circuit Breaker está abierto,"
print_info "el método fallbackUrl() proporciona una URL alternativa."
echo ""

response=$(curl -s -X POST "${BASE_URL}/playback/start?trackId=fallback_demo" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json')

echo "$response" | jq '.'
echo ""

fallback_url=$(echo "$response" | jq -r '.streamUrl // empty')
if [[ "$fallback_url" == *"fallback"* ]]; then
  print_success "Fallback URL activado correctamente"
  echo "URL alternativa: $fallback_url"
else
  print_info "Request exitoso con servicio principal"
  echo "URL principal: $fallback_url"
fi

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN EN EL CÓDIGO"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. PlaybackService.java:"
echo "   @Retry(name=\"streamSource\")"
echo "   @CircuitBreaker(name=\"streamSource\", fallbackMethod=\"fallbackUrl\")"
echo "   public PlaybackResponseDto startPlayback(...)"
echo ""
echo "2. application.yaml:"
echo "   resilience4j:"
echo "     retry:"
echo "       instances:"
echo "         streamSource:"
echo "           maxAttempts: 3"
echo "           waitDuration: 200ms"
echo "     circuitbreaker:"
echo "       instances:"
echo "         streamSource:"
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
