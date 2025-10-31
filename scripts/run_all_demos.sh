#!/usr/bin/env bash

# ============================================================================
# SCRIPT MAESTRO: DEMOSTRACIÓN COMPLETA DE TODOS LOS PATRONES
# ============================================================================
# Este script ejecuta todas las demostraciones de patrones arquitectónicos
# implementados en Musify para la asignatura TFU Unidad 4.
#
# PATRONES DEMOSTRADOS (11 total):
#
# DISPONIBILIDAD (4 patrones):
#   1. Retry - Reintentos automáticos
#   2. Circuit Breaker - Prevención de cascada de fallos
#   3. Rate Limiting - Limitación de intentos
#   4. Health Monitoring - Monitoreo de salud
#
# RENDIMIENTO (2 patrones):
#   5. Cache-Aside - Cacheo de resultados
#   6. Async Request-Reply - Procesamiento asíncrono
#
# SEGURIDAD (3 patrones):
#   7. Gatekeeper - NGINX como gateway
#   8. Gateway Offloading - Offload de funciones
#   9. Federated Identity - JWT + OAuth2
#
# MODIFICABILIDAD (2 patrones):
#   10. External Configuration Store - Configuración externa
#   11. Blue/Green Deployment - Actualización sin downtime
# ============================================================================

set -euo pipefail

# Configuración
BASE_URL="${BASE_URL:-http://localhost:8080}"
COLORS_ENABLED=true
PAUSE_BETWEEN_DEMOS=5

# Colores
if [ "$COLORS_ENABLED" = true ]; then
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[1;33m'
  BLUE='\033[0;34m'
  CYAN='\033[0;36m'
  MAGENTA='\033[0;35m'
  BOLD='\033[1m'
  NC='\033[0m'
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
  CYAN=''
  MAGENTA=''
  BOLD=''
  NC=''
fi

# Obtener directorio del script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

print_banner() {
  echo -e "\n${CYAN}╔════════════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${CYAN}║                                                                    ║${NC}"
  echo -e "${CYAN}║${NC}  ${BOLD}${MAGENTA}MUSIFY - DEMOSTRACIÓN DE PATRONES ARQUITECTÓNICOS${NC}  ${CYAN}║${NC}"
  echo -e "${CYAN}║                                                                    ║${NC}"
  echo -e "${CYAN}║${NC}  ${YELLOW}TFU Unidad 4 - Atributos de Calidad${NC}                     ${CYAN}║${NC}"
  echo -e "${CYAN}║                                                                    ║${NC}"
  echo -e "${CYAN}╚════════════════════════════════════════════════════════════════════╝${NC}\n"
}

print_section() {
  echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BLUE}${BOLD}$1${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
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

print_demo() {
  echo -e "\n${MAGENTA}${BOLD}═══════════════════════════════════════════════════════════${NC}"
  echo -e "${MAGENTA}${BOLD} DEMO: $1${NC}"
  echo -e "${MAGENTA}${BOLD}═══════════════════════════════════════════════════════════${NC}\n"
}

pause_between_demos() {
  echo ""
  print_info "Esperando $PAUSE_BETWEEN_DEMOS segundos antes de la siguiente demostración..."
  sleep $PAUSE_BETWEEN_DEMOS
}

run_demo() {
  local demo_name=$1
  local demo_script=$2
  local demo_description=$3
  
  print_demo "$demo_name"
  echo -e "${CYAN}Descripción: ${NC}$demo_description"
  echo ""
  
  if [ ! -f "$demo_script" ]; then
    print_error "Script no encontrado: $demo_script"
    return 1
  fi
  
  if [ ! -x "$demo_script" ]; then
    print_info "Haciendo script ejecutable: $demo_script"
    chmod +x "$demo_script"
  fi
  
  echo -e "${YELLOW}Ejecutando: $demo_script${NC}"
  echo ""
  
  if bash "$demo_script"; then
    print_success "Demostración completada: $demo_name"
  else
    print_error "Error en demostración: $demo_name"
    return 1
  fi
}

check_prerequisites() {
  print_section "VERIFICACIÓN DE REQUISITOS"
  
  local all_ok=true
  
  # Verificar comandos necesarios
  echo "Verificando comandos requeridos..."
  
  for cmd in curl jq docker; do
    if command -v $cmd &> /dev/null; then
      print_success "$cmd está instalado"
    else
      print_error "$cmd no está instalado"
      all_ok=false
    fi
  done
  
  echo ""
  
  # Verificar que el sistema esté ejecutándose
  echo "Verificando que Musify esté ejecutándose..."
  
  if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    print_success "Backend está disponible en ${BASE_URL}"
    
    # Verificar estado de salud
    health_status=$(curl -s "${BASE_URL}/actuator/health" | jq -r '.status')
    if [ "$health_status" = "UP" ]; then
      print_success "Sistema está UP"
    else
      print_error "Sistema está DOWN: $health_status"
      all_ok=false
    fi
  else
    print_error "Backend no está disponible en ${BASE_URL}"
    echo ""
    echo "Por favor, inicia el sistema con:"
    echo "  cd /home/clara/musify"
    echo "  docker compose up --build"
    all_ok=false
  fi
  
  echo ""
  
  # Verificar contenedores Docker
  echo "Verificando contenedores Docker..."
  
  # Verificar que al menos una réplica del backend esté activa
  backend_count=0
  for replica in "backend-app-1" "backend-app-2"; do
    if docker ps --format '{{.Names}}' | grep -q "^${replica}$"; then
      print_success "Contenedor $replica está ejecutándose"
      backend_count=$((backend_count + 1))
    else
      print_info "Contenedor $replica no está ejecutándose"
    fi
  done
  
  if [ $backend_count -eq 0 ]; then
    print_error "Ninguna réplica del backend está ejecutándose"
    all_ok=false
  else
    print_success "$backend_count réplica(s) del backend activa(s)"
  fi
  
  # Verificar otros contenedores requeridos
  required_containers=("nginx" "postgres" "flaky-service")
  
  for container in "${required_containers[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
      print_success "Contenedor $container está ejecutándose"
    else
      print_error "Contenedor $container no está ejecutándose"
      all_ok=false
    fi
  done
  
  echo ""
  
  if [ "$all_ok" = false ]; then
    print_error "Algunos requisitos no se cumplen"
    echo ""
    echo "Para iniciar el sistema:"
    echo "  1. Navega al directorio del proyecto: cd /home/clara/musify"
    echo "  2. Inicia Docker Compose: docker compose up --build"
    echo "  3. Espera a que todos los servicios estén UP"
    echo "  4. Ejecuta este script nuevamente"
    exit 1
  fi
  
  print_success "Todos los requisitos se cumplen"
}

show_menu() {
  print_section "MENÚ DE DEMOSTRACIONES"
  
  echo "Selecciona qué demos ejecutar:"
  echo ""
  echo "  ${BOLD}DISPONIBILIDAD:${NC}"
  echo "    1) Retry + Circuit Breaker + Fallback"
  echo "    2) Circuit Breaker (detallado)"
  echo "    3) Rate Limiting + Validación"
  echo "    4) Health Monitoring"
  echo "    5) Blue/Green Deployment + Replicación"
  echo ""
  echo "  ${BOLD}RENDIMIENTO:${NC}"
  echo "    6) Cache-Aside + Async Request-Reply"
  echo ""
  echo "  ${BOLD}TODOS:${NC}"
  echo "    A) Ejecutar todas las demos (modo completo)"
  echo "    Q) Ejecutar todas las demos (modo rápido, sin pausas)"
  echo ""
  echo "    0) Salir"
  echo ""
  echo -n "Selección: "
  read choice
  echo ""
}

run_all_demos() {
  local skip_pause=$1
  
  print_section "EJECUTANDO TODAS LAS DEMOSTRACIONES"
  
  # 1. Health Monitoring (primero para verificar sistema)
  run_demo "Health Endpoint Monitoring" \
    "${SCRIPT_DIR}/demo_health.sh" \
    "Monitoreo de salud, observabilidad y documentación API"
  
  [ "$skip_pause" = "false" ] && pause_between_demos
  
  # 2. Retry + Circuit Breaker + Fallback
  run_demo "Retry + Circuit Breaker + Fallback" \
    "${SCRIPT_DIR}/demo_retries.sh" \
    "Reintentos automáticos, circuit breaker y degradación elegante"
  
  [ "$skip_pause" = "false" ] && pause_between_demos
  
  # 3. Circuit Breaker (detallado)
  run_demo "Circuit Breaker States" \
    "${SCRIPT_DIR}/demo_circuit_breaker.sh" \
    "Estados CLOSED/OPEN/HALF_OPEN del Circuit Breaker"
  
  [ "$skip_pause" = "false" ] && pause_between_demos
  
  # 4. Security (Rate Limiting + Validation)
  run_demo "Security Patterns" \
    "${SCRIPT_DIR}/demo_security.sh" \
    "Rate limiting, validación de entrada, JWT y gatekeeper"
  
  [ "$skip_pause" = "false" ] && pause_between_demos
  
  # 5. Performance (Cache + Async)
  run_demo "Performance Patterns" \
    "${SCRIPT_DIR}/demo_performance.sh" \
    "Cache-Aside y Asynchronous Request-Reply"
  
  [ "$skip_pause" = "false" ] && pause_between_demos
  
  # 6. Replication + Blue/Green
  run_demo "Blue/Green Deployment + Replication" \
    "${SCRIPT_DIR}/demo_replication.sh" \
    "Alta disponibilidad, replicación y zero-downtime deployment"
  
  print_section "TODAS LAS DEMOSTRACIONES COMPLETADAS"
  print_success "Se han ejecutado exitosamente todas las demos"
}

show_summary() {
  print_section "RESUMEN DE PATRONES IMPLEMENTADOS"
  
  echo ""
  echo "${BOLD}${GREEN}DISPONIBILIDAD (4 patrones):${NC}"
  echo "  ✓ Retry                  - Reintentos automáticos (3 intentos, 200ms)"
  echo "  ✓ Circuit Breaker        - Prevención de cascada de fallos (50% threshold)"
  echo "  ✓ Rate Limiting          - Limitación de intentos (5/min en login)"
  echo "  ✓ Health Monitoring      - Spring Boot Actuator"
  echo ""
  
  echo "${BOLD}${CYAN}RENDIMIENTO (2 patrones):${NC}"
  echo "  ✓ Cache-Aside            - Cacheo de búsquedas Spotify"
  echo "  ✓ Async Request-Reply    - Thread pool (5 core, 10 max)"
  echo ""
  
  echo "${BOLD}${YELLOW}SEGURIDAD (3 patrones):${NC}"
  echo "  ✓ Gatekeeper             - NGINX como gateway con TLS"
  echo "  ✓ Gateway Offloading     - TLS termination, retries, health checks"
  echo "  ✓ Federated Identity     - JWT + OAuth2 (Spotify API)"
  echo ""
  
  echo "${BOLD}${MAGENTA}MODIFICABILIDAD (2 patrones):${NC}"
  echo "  ✓ External Config Store  - .env + docker-compose + application.yaml"
  echo "  ✓ Blue/Green Deployment  - 2 réplicas, zero-downtime updates"
  echo ""
  
  echo "${BOLD}TOTAL: 11 patrones implementados${NC}"
  echo ""
  
  echo "Documentación disponible en:"
  echo "  - README.md"
  echo "  - PATRONES_IMPLEMENTADOS.md"
  echo "  - DOCUMENTACION_TFU_PARTE1.md"
  echo "  - diagramas/*.pu (PlantUML)"
  echo ""
  
  echo "Scripts de demostración:"
  echo "  - demo_retries.sh           (Retry + CB + Fallback)"
  echo "  - demo_circuit_breaker.sh   (Circuit Breaker detallado)"
  echo "  - demo_security.sh          (Security patterns)"
  echo "  - demo_performance.sh       (Cache + Async)"
  echo "  - demo_health.sh            (Health monitoring)"
  echo "  - demo_replication.sh       (Blue/Green + Replication)"
  echo ""
}

# ============================================================================
# MAIN
# ============================================================================

print_banner

check_prerequisites

while true; do
  show_menu
  
  case $choice in
    1)
      run_demo "Retry + Circuit Breaker + Fallback" \
        "${SCRIPT_DIR}/demo_retries.sh" \
        "Reintentos automáticos, circuit breaker y degradación elegante"
      ;;
    2)
      run_demo "Circuit Breaker States" \
        "${SCRIPT_DIR}/demo_circuit_breaker.sh" \
        "Estados CLOSED/OPEN/HALF_OPEN del Circuit Breaker"
      ;;
    3)
      run_demo "Security Patterns" \
        "${SCRIPT_DIR}/demo_security.sh" \
        "Rate limiting, validación de entrada, JWT y gatekeeper"
      ;;
    4)
      run_demo "Health Endpoint Monitoring" \
        "${SCRIPT_DIR}/demo_health.sh" \
        "Monitoreo de salud, observabilidad y documentación API"
      ;;
    5)
      run_demo "Blue/Green Deployment + Replication" \
        "${SCRIPT_DIR}/demo_replication.sh" \
        "Alta disponibilidad, replicación y zero-downtime deployment"
      ;;
    6)
      run_demo "Performance Patterns" \
        "${SCRIPT_DIR}/demo_performance.sh" \
        "Cache-Aside y Asynchronous Request-Reply"
      ;;
    [Aa])
      run_all_demos false
      break
      ;;
    [Qq])
      PAUSE_BETWEEN_DEMOS=0
      run_all_demos true
      break
      ;;
    0)
      print_info "Saliendo..."
      exit 0
      ;;
    *)
      print_error "Opción inválida: $choice"
      ;;
  esac
  
  echo ""
  read -p "Presiona Enter para volver al menú..." dummy
done

show_summary

print_success "¡Demos completadas! Gracias por usar Musify."
