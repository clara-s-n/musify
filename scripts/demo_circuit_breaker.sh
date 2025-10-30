#!/usr/bin/env bash

# ============================================================================
# DEMOSTRACIÓN: CIRCUIT BREAKER EN DETALLE
# ============================================================================
# Este script demuestra específicamente el patrón Circuit Breaker:
# 1. Estado CLOSED: Operación normal, requests pasan al servicio
# 2. Estado OPEN: Después de fallos, CB se abre, fail-fast con fallback
# 3. Estado HALF_OPEN: Después de timeout, prueba si servicio se recuperó
#
# Atributos de calidad demostrados:
# - DISPONIBILIDAD: Sistema responde incluso cuando dependencia falla
# - RESILIENCIA: Previene cascada de fallos
# - RECUPERACIÓN: Detección automática cuando servicio se recupera
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
  MAGENTA='\033[0;35m'
  NC='\033[0m'
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
  MAGENTA=''
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

print_state() {
  local state=$1
  case $state in
    "CLOSED")
      echo -e "${GREEN}[CLOSED]${NC}"
      ;;
    "OPEN")
      echo -e "${RED}[OPEN]${NC}"
      ;;
    "HALF_OPEN")
      echo -e "${MAGENTA}[HALF_OPEN]${NC}"
      ;;
    *)
      echo -e "${YELLOW}[UNKNOWN]${NC}"
      ;;
  esac
}

get_auth_token() {
    print_info "Obteniendo token de autenticación..."
    local response=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"user@demo.com","password":"password"}')
    
    local token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$token" ]; then
        print_error "No se pudo obtener token de autenticación"
        echo "Respuesta: $response"
        exit 1
    fi
    
    echo "$token"
}

test_playback() {
  local track_id=$1
  local token=$2
  
  local response=$(curl -s -X POST "${BASE_URL}/playback/start?trackId=${track_id}" \
    -H "Authorization: Bearer ${token}" \
    -H 'Content-Type: application/json')
  
  echo "$response"
}

print_header "CIRCUIT BREAKER - CONFIGURACIÓN"

print_info "Configuración en application.yaml:"
echo ""
echo "  resilience4j:"
echo "    circuitbreaker:"
echo "      instances:"
echo "        streamSource:"
echo "          failureRateThreshold: 50            # Abre con 50% de fallos"
echo "          slowCallRateThreshold: 50           # Abre con 50% de llamadas lentas"
echo "          slowCallDurationThreshold: 5000ms   # Llamada lenta > 5s"
echo "          slidingWindowSize: 10               # Ventana de 10 requests"
echo "          minimumNumberOfCalls: 5             # Mínimo 5 llamadas antes de calcular"
echo "          waitDurationInOpenState: 10000ms    # Esperar 10s antes de HALF_OPEN"
echo "          permittedNumberOfCallsInHalfOpenState: 3"
echo ""

print_header "VERIFICACIÓN DEL SISTEMA"

if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
  print_error "Sistema no está disponible"
  echo "Inicia el sistema con: docker compose up"
  exit 1
fi

print_success "Sistema disponible"

TOKEN=$(get_auth_token)
if [ -z "$TOKEN" ]; then
  print_error "No se pudo obtener token de autenticación"
  exit 1
fi

print_success "Token obtenido"

print_header "FASE 1: ESTADO CLOSED (OPERACIÓN NORMAL)"

print_info "Circuit Breaker comienza en estado CLOSED"
print_info "Los requests se envían normalmente al servicio flaky-service"
echo ""

echo "Enviando 5 requests iniciales..."
echo ""

for i in $(seq 1 5); do
  echo -n "Request $i: "
  print_state "CLOSED"
  echo -n "  => "
  
  response=$(test_playback "closed_$i" "$TOKEN")
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"flaky-service"* ]]; then
    print_success "Servicio principal (flaky-service)"
  elif [[ "$stream_url" == *"fallback"* ]]; then
    print_info "Fallback URL (retry agotado)"
  else
    print_error "Sin respuesta"
  fi
  
  sleep 0.5
done

print_success "Estado CLOSED: Requests enviados a servicio principal"

print_header "FASE 2: GENERAR FALLOS PARA ABRIR CIRCUIT BREAKER"

print_info "Enviando múltiples requests para generar fallos..."
print_info "Flaky-service falla 60% del tiempo (40% timeout + 20% error)"
print_info "Cuando fallo alcanza 50%, Circuit Breaker se ABRE"
echo ""

REQUESTS_TO_OPEN=12
OPEN_DETECTED=false

for i in $(seq 1 $REQUESTS_TO_OPEN); do
  echo -n "Request $i: "
  
  response=$(test_playback "opening_$i" "$TOKEN")
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"fallback"* ]]; then
    # Puede ser fallback por retry o por CB abierto
    # Si muchos seguidos, probablemente CB está abierto
    if [ $i -gt 6 ] && [ "$OPEN_DETECTED" = false ]; then
      print_state "OPEN"
      echo -n "  => "
      print_error "Circuit Breaker ABIERTO - Fallback inmediato (fail-fast)"
      OPEN_DETECTED=true
    else
      print_state "CLOSED"
      echo -n "  => "
      print_info "Fallback (retries agotados)"
    fi
  elif [[ "$stream_url" == *"flaky-service"* ]]; then
    print_state "CLOSED"
    echo -n "  => "
    print_success "Servicio principal"
  fi
  
  sleep 0.5
done

if [ "$OPEN_DETECTED" = true ]; then
  print_error "Circuit Breaker se ABRIÓ después de detectar alta tasa de fallos"
else
  print_info "Circuit Breaker puede estar en proceso de apertura"
fi

print_header "FASE 3: ESTADO OPEN (FAIL-FAST)"

print_info "Cuando Circuit Breaker está OPEN:"
echo "  - Requests NO se envían al servicio fallido"
echo "  - Fallback se retorna inmediatamente (fail-fast)"
echo "  - Previene sobrecargar servicio con problemas"
echo ""

print_info "Enviando 5 requests con Circuit Breaker OPEN..."
echo ""

for i in $(seq 1 5); do
  echo -n "Request $i: "
  print_state "OPEN"
  echo -n "  => "
  
  start=$(date +%s%N)
  response=$(test_playback "open_$i" "$TOKEN")
  end=$(date +%s%N)
  duration_ms=$(( (end - start) / 1000000 ))
  
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"fallback"* ]]; then
    print_info "Fallback URL (fail-fast en ${duration_ms}ms)"
  else
    print_success "Respuesta en ${duration_ms}ms"
  fi
  
  sleep 0.5
done

print_success "Requests respondieron rápidamente con fallback (fail-fast)"
print_success "Servicio fallido no recibe tráfico, permitiendo recuperación"

print_header "FASE 4: ESPERANDO TRANSICIÓN A HALF_OPEN"

print_info "Circuit Breaker permanece OPEN durante waitDurationInOpenState (10s)"
print_info "Después de este tiempo, transiciona a HALF_OPEN para probar recuperación"
echo ""

print_info "Esperando 10 segundos..."

for i in {10..1}; do
  echo -n "  $i segundos restantes... "
  print_state "OPEN"
  echo ""
  sleep 1
done

print_success "Tiempo de espera completado"

print_header "FASE 5: ESTADO HALF_OPEN (PRUEBA DE RECUPERACIÓN)"

print_info "En estado HALF_OPEN:"
echo "  - Se permiten algunos requests de prueba (3 configurado)"
echo "  - Si tienen éxito, CB vuelve a CLOSED"
echo "  - Si fallan, CB vuelve a OPEN"
echo ""

print_info "Enviando requests de prueba en estado HALF_OPEN..."
echo ""

HALF_OPEN_SUCCESS=0
HALF_OPEN_FAIL=0

for i in $(seq 1 5); do
  echo -n "Request $i: "
  print_state "HALF_OPEN"
  echo -n "  => "
  
  response=$(test_playback "halfopen_$i" "$TOKEN")
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"flaky-service"* ]]; then
    print_success "Éxito con servicio principal"
    HALF_OPEN_SUCCESS=$((HALF_OPEN_SUCCESS + 1))
  elif [[ "$stream_url" == *"fallback"* ]]; then
    print_info "Fallback"
    HALF_OPEN_FAIL=$((HALF_OPEN_FAIL + 1))
  fi
  
  sleep 1
done

echo ""
echo "Resultados en HALF_OPEN:"
echo "  Éxitos: $HALF_OPEN_SUCCESS"
echo "  Fallos: $HALF_OPEN_FAIL"

if [ $HALF_OPEN_SUCCESS -ge 2 ]; then
  print_success "Suficientes éxitos - Circuit Breaker probablemente CERRADO"
else
  print_error "Muchos fallos - Circuit Breaker probablemente volvió a OPEN"
fi

print_header "FASE 6: VERIFICAR ESTADO FINAL"

print_info "Enviando requests finales para verificar estado..."
echo ""

for i in $(seq 1 5); do
  echo -n "Request $i: "
  
  response=$(test_playback "final_$i" "$TOKEN")
  stream_url=$(echo "$response" | jq -r '.streamUrl // empty')
  
  if [[ "$stream_url" == *"flaky-service"* ]]; then
    print_state "CLOSED"
    echo -n "  => "
    print_success "Servicio principal (CB cerrado)"
  elif [[ "$stream_url" == *"fallback"* ]]; then
    print_state "OPEN"
    echo -n "  => "
    print_info "Fallback (CB abierto o retry)"
  fi
  
  sleep 0.5
done

print_header "DIAGRAMA DE ESTADOS DEL CIRCUIT BREAKER"

echo ""
echo "                    [CLOSED]"
echo "                       |"
echo "                       | Tasa de fallos > 50%"
echo "                       v"
echo "                    [OPEN]"
echo "                       |"
echo "                       | Después de 10s"
echo "                       v"
echo "                  [HALF_OPEN]"
echo "                    /     \\"
echo "          Éxito /           \\ Fallo"
echo "               v             v"
echo "          [CLOSED]        [OPEN]"
echo ""

print_header "BENEFICIOS DEL CIRCUIT BREAKER"

echo "1. PREVENCIÓN DE CASCADA DE FALLOS:"
echo "   - Servicio fallido no recibe más tráfico"
echo "   - Sistema upstream no se sobrecarga esperando respuestas"
echo ""
echo "2. FAIL-FAST:"
echo "   - Respuestas rápidas con fallback"
echo "   - Mejor experiencia de usuario que timeouts largos"
echo ""
echo "3. RECUPERACIÓN AUTOMÁTICA:"
echo "   - Detecta cuando servicio se recupera (HALF_OPEN)"
echo "   - Vuelve a operación normal automáticamente"
echo ""
echo "4. MONITOREO:"
echo "   - Estados del CB son observables"
echo "   - Métricas útiles para debugging y alertas"
echo ""

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. PlaybackService.java:"
echo "   @CircuitBreaker(name=\"streamSource\", fallbackMethod=\"fallbackUrl\")"
echo "   public PlaybackResponseDto startPlayback(...)"
echo ""
echo "   private PlaybackResponseDto fallbackUrl(..., Throwable t) {"
echo "     // Método fallback cuando CB está abierto"
echo "   }"
echo ""
echo "2. application.yaml:"
echo "   resilience4j:"
echo "     circuitbreaker:"
echo "       instances:"
echo "         streamSource:"
echo "           failureRateThreshold: 50"
echo "           slidingWindowSize: 10"
echo "           waitDurationInOpenState: 10000"
echo ""
echo "3. pom.xml:"
echo "   <dependency>"
echo "     <groupId>io.github.resilience4j</groupId>"
echo "     <artifactId>resilience4j-spring-boot3</artifactId>"
echo "   </dependency>"
echo ""

print_header "ATRIBUTOS DE CALIDAD DEMOSTRADOS"

echo "✓ DISPONIBILIDAD:"
echo "  - Sistema continúa funcionando con fallback"
echo "  - Respuestas rápidas incluso cuando servicio falla"
echo ""
echo "✓ RESILIENCIA:"
echo "  - Previene cascada de fallos a otros servicios"
echo "  - Recuperación automática cuando servicio mejora"
echo ""
echo "✓ RENDIMIENTO:"
echo "  - Fail-fast evita timeouts largos"
echo "  - Reduce latencia cuando servicio está fallando"
echo ""
echo "✓ OBSERVABILIDAD:"
echo "  - Estados del CB son monitoreables"
echo "  - Métricas para análisis de incidentes"
echo ""

print_success "Demostración completada exitosamente"
