#!/usr/bin/env bash

# ============================================================================
# DEMOSTRACIÓN: HEALTH ENDPOINT MONITORING + OBSERVABILIDAD
# ============================================================================
# Este script demuestra el monitoreo y la observabilidad del sistema:
# 1. Endpoints de salud: Proporciona información sobre el estado del sistema
# 2. Documentación API: OpenAPI/Swagger para exploración de endpoints
# 3. Métricas del sistema: Información de salud de componentes
#
# Atributos de calidad demostrados:
# - DISPONIBILIDAD: Monitoreo continuo del estado del sistema
# - OBSERVABILIDAD: Visibilidad del estado interno de la aplicación
# - MANTENIBILIDAD: Documentación automática de la API
# - DETECTABILIDAD: Identificación proactiva de problemas
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
  NC='\033[0m'
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

print_header "PATRÓN: HEALTH ENDPOINT MONITORING"

print_info "Spring Boot Actuator proporciona endpoints de monitoreo predefinidos"
print_info "Configuración en application.yaml:"
echo ""
echo "  management:"
echo "    endpoints:"
echo "      web:"
echo "        exposure:"
echo "          include: health,info"
echo "    endpoint:"
echo "      health:"
echo "        show-details: always"
echo ""

print_header "TEST 1: HEALTH ENDPOINT BÁSICO"

print_info "Consultando /actuator/health..."
echo ""

health_response=$(curl -s "${BASE_URL}/actuator/health")

if [ $? -eq 0 ]; then
  echo "$health_response" | jq '.'
  
  status=$(echo "$health_response" | jq -r '.status')
  
  if [ "$status" = "UP" ]; then
    print_success "Sistema está UP y disponible"
  else
    print_error "Sistema está DOWN o degradado"
  fi
else
  print_error "No se pudo conectar al endpoint de health"
  echo "Asegúrate de que el sistema esté ejecutándose: docker compose up"
  exit 1
fi

print_header "TEST 2: COMPONENTES DE SALUD"

print_info "Verificando estado de componentes individuales..."
echo ""

# Extraer información de componentes
db_status=$(echo "$health_response" | jq -r '.components.db.status // "N/A"')
diskSpace_status=$(echo "$health_response" | jq -r '.components.diskSpace.status // "N/A"')
ping_status=$(echo "$health_response" | jq -r '.components.ping.status // "N/A"')

echo "Componentes detectados:"
echo ""

if [ "$db_status" != "N/A" ]; then
  if [ "$db_status" = "UP" ]; then
    print_success "  Database (PostgreSQL): $db_status"
    
    # Mostrar detalles de la base de datos
    db_database=$(echo "$health_response" | jq -r '.components.db.details.database // "N/A"')
    db_validationQuery=$(echo "$health_response" | jq -r '.components.db.details.validationQuery // "N/A"')
    
    echo "    - Database: $db_database"
    echo "    - Validation Query: $db_validationQuery"
  else
    print_error "  Database (PostgreSQL): $db_status"
  fi
else
  print_info "  Database: No disponible en respuesta"
fi

echo ""

if [ "$diskSpace_status" != "N/A" ]; then
  if [ "$diskSpace_status" = "UP" ]; then
    print_success "  Disk Space: $diskSpace_status"
    
    # Mostrar detalles de disco
    total=$(echo "$health_response" | jq -r '.components.diskSpace.details.total // 0')
    free=$(echo "$health_response" | jq -r '.components.diskSpace.details.free // 0')
    threshold=$(echo "$health_response" | jq -r '.components.diskSpace.details.threshold // 0')
    
    if [ "$total" != "0" ]; then
      total_gb=$(( total / 1024 / 1024 / 1024 ))
      free_gb=$(( free / 1024 / 1024 / 1024 ))
      threshold_mb=$(( threshold / 1024 / 1024 ))
      
      echo "    - Total: ${total_gb} GB"
      echo "    - Free: ${free_gb} GB"
      echo "    - Threshold: ${threshold_mb} MB"
    fi
  else
    print_error "  Disk Space: $diskSpace_status"
  fi
fi

echo ""

if [ "$ping_status" != "N/A" ]; then
  if [ "$ping_status" = "UP" ]; then
    print_success "  Ping: $ping_status (aplicación responde)"
  else
    print_error "  Ping: $ping_status"
  fi
fi

print_header "TEST 3: MONITOREO CONTINUO"

print_info "Realizando 10 verificaciones de salud cada segundo..."
print_info "Simula un sistema de monitoreo externo (Prometheus, Grafana, etc.)"
echo ""

UP_COUNT=0
DOWN_COUNT=0

for i in $(seq 1 10); do
  response=$(curl -s "${BASE_URL}/actuator/health")
  status=$(echo "$response" | jq -r '.status')
  
  if [ "$status" = "UP" ]; then
    echo -n -e "${GREEN}✓${NC}"
    UP_COUNT=$((UP_COUNT + 1))
  else
    echo -n -e "${RED}✗${NC}"
    DOWN_COUNT=$((DOWN_COUNT + 1))
  fi
  
  sleep 1
done

echo ""
echo ""
echo "Resultados del monitoreo:"
echo "  UP:   $UP_COUNT/10 ($(( UP_COUNT * 100 / 10 ))%)"
echo "  DOWN: $DOWN_COUNT/10 ($(( DOWN_COUNT * 100 / 10 ))%)"

if [ $UP_COUNT -eq 10 ]; then
  print_success "Sistema 100% disponible durante el período de monitoreo"
elif [ $UP_COUNT -ge 8 ]; then
  print_info "Sistema mayormente disponible (>80%)"
else
  print_error "Sistema con problemas de disponibilidad (<80%)"
fi

print_header "TEST 4: DOCUMENTACIÓN API (OPENAPI/SWAGGER)"

print_info "Spring Boot genera documentación OpenAPI automáticamente"
print_info "Consultando /v3/api-docs..."
echo ""

api_docs=$(curl -s "${BASE_URL}/v3/api-docs" 2>/dev/null || echo "{}")

if [ "$api_docs" != "{}" ]; then
  # Extraer información básica
  title=$(echo "$api_docs" | jq -r '.info.title // "N/A"')
  version=$(echo "$api_docs" | jq -r '.info.version // "N/A"')
  
  print_success "Documentación API disponible"
  echo ""
  echo "Información de la API:"
  echo "  Título:  $title"
  echo "  Versión: $version"
  echo ""
  
  # Contar endpoints
  path_count=$(echo "$api_docs" | jq '.paths | length')
  echo "  Endpoints documentados: $path_count"
  echo ""
  
  print_info "Ejemplo de endpoints disponibles:"
  echo "$api_docs" | jq -r '.paths | keys[]' | head -n 10
  
  echo ""
  print_success "Swagger UI disponible en: ${BASE_URL}/swagger-ui.html"
else
  print_error "No se pudo obtener documentación API"
fi

print_header "TEST 5: USO EN SISTEMAS DE MONITOREO"

print_info "El health endpoint puede integrarse con:"
echo ""
echo "1. Kubernetes/Docker:"
echo "   livenessProbe:"
echo "     httpGet:"
echo "       path: /actuator/health"
echo "       port: 8080"
echo "     initialDelaySeconds: 30"
echo "     periodSeconds: 10"
echo ""
echo "2. NGINX Load Balancer (nginx.conf):"
echo "   upstream backend {"
echo "     server backend-app-1:8443 max_fails=3 fail_timeout=10s;"
echo "     server backend-app-2:8443 max_fails=3 fail_timeout=10s;"
echo "   }"
echo "   # Passive health checks based on request failures"
echo ""
echo "3. Prometheus (metrics endpoint):"
echo "   scrape_configs:"
echo "     - job_name: 'musify-backend'"
echo "       metrics_path: '/actuator/prometheus'"
echo "       static_configs:"
echo "         - targets: ['localhost:8080']"
echo ""
echo "4. Grafana Dashboards:"
echo "   - Visualización de métricas en tiempo real"
echo "   - Alertas automáticas basadas en health status"
echo "   - Historial de uptime/downtime"
echo ""

print_header "TEST 6: SIMULACIÓN DE DEGRADACIÓN"

print_info "Verificando comportamiento bajo diferentes condiciones..."
echo ""

# Test 1: Sistema normal
print_info "Condición 1: Sistema normal"
normal_response=$(curl -s -w "\n%{http_code}" "${BASE_URL}/actuator/health")
http_code=$(echo "$normal_response" | tail -n1)
body=$(echo "$normal_response" | head -n-1)

if [ "$http_code" = "200" ]; then
  print_success "HTTP 200 - Sistema UP"
else
  print_error "HTTP $http_code - Sistema con problemas"
fi

echo ""

# Test 2: Verificar accesibilidad de otros endpoints
print_info "Condición 2: Verificar endpoints dependientes"

# Test login endpoint
login_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@demo.com","password":"password"}')

if [ "$login_code" = "200" ]; then
  print_success "Login endpoint accesible (HTTP $login_code)"
else
  print_info "Login endpoint: HTTP $login_code"
fi

# Test tracks endpoint
tracks_code=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/tracks/spotify/random?limit=5")

if [ "$tracks_code" = "200" ]; then
  print_success "Tracks endpoint accesible (HTTP $tracks_code)"
else
  print_info "Tracks endpoint: HTTP $tracks_code"
fi

print_header "VERIFICACIÓN DE IMPLEMENTACIÓN"

echo "Para verificar la implementación, revisa:"
echo ""
echo "1. Dependencia (pom.xml):"
echo "   <dependency>"
echo "     <groupId>org.springframework.boot</groupId>"
echo "     <artifactId>spring-boot-starter-actuator</artifactId>"
echo "   </dependency>"
echo ""
echo "2. Configuración (application.yaml):"
echo "   management:"
echo "     endpoints:"
echo "       web:"
echo "         exposure:"
echo "           include: health,info"
echo "     endpoint:"
echo "       health:"
echo "         show-details: always"
echo ""
echo "3. Acceso a endpoints:"
echo "   - Health: ${BASE_URL}/actuator/health"
echo "   - Info: ${BASE_URL}/actuator/info"
echo "   - API Docs: ${BASE_URL}/v3/api-docs"
echo "   - Swagger UI: ${BASE_URL}/swagger-ui.html"
echo ""

print_header "ATRIBUTOS DE CALIDAD DEMOSTRADOS"

echo "✓ DISPONIBILIDAD:"
echo "  - Monitoreo continuo del estado del sistema"
echo "  - Detección temprana de problemas"
echo "  - Información en tiempo real de componentes"
echo ""
echo "✓ OBSERVABILIDAD:"
echo "  - Visibilidad del estado interno (DB, disco, memoria)"
echo "  - Endpoints estandarizados para herramientas externas"
echo "  - Información estructurada en formato JSON"
echo ""
echo "✓ MANTENIBILIDAD:"
echo "  - Documentación API automática con OpenAPI"
echo "  - Contratos claros entre frontend y backend"
echo "  - Facilita desarrollo y debugging"
echo ""
echo "✓ DETECTABILIDAD:"
echo "  - Health checks para load balancers"
echo "  - Integración con sistemas de monitoreo"
echo "  - Alertas proactivas basadas en métricas"
echo ""

print_success "Demostración completada exitosamente"
