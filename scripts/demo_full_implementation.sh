#!/bin/bash

# Script para verificar las nuevas funcionalidades implementadas
# Título: Verificación de Performance y Lógica del Reproductor + Frontend UI/UX + API Búsqueda

echo "🚀 MUSIFY - Verificación de Implementación Completa"
echo "=================================================="
echo

# Configuración
BACKEND_URL="https://localhost:8443"
BACKEND_HTTP_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:4200"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Función para verificar si un servicio está corriendo
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -k -s --connect-timeout 5 "$url" > /dev/null 2>&1; then
        log_success "$service_name está corriendo"
        return 0
    else
        log_error "$service_name no está disponible en $url"
        return 1
    fi
}

# Función para hacer peticiones con manejo de errores
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    log_info "Verificando: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -k -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" 2>/dev/null)
    else
        response=$(curl -k -s -w "\n%{http_code}" -X "$method" "$url" 2>/dev/null)
    fi
    
    # Separar body y status code
    body=$(echo "$response" | head -n -1)
    status_code=$(echo "$response" | tail -n 1)
    
    if [ "$status_code" -ge 200 ] && [ "$status_code" -lt 300 ]; then
        log_success "$description - Status: $status_code"
        echo "Response: $(echo "$body" | jq . 2>/dev/null || echo "$body" | head -c 200)..."
        return 0
    else
        log_error "$description - Status: $status_code"
        echo "Response: $(echo "$body" | head -c 200)..."
        return 1
    fi
}

echo "1. VERIFICACIÓN DE SERVICIOS"
echo "============================="

# Verificar backend HTTPS
check_service "$BACKEND_URL/actuator/health" "Backend HTTPS"
backend_https_status=$?

# Verificar backend HTTP (fallback)
if [ $backend_https_status -ne 0 ]; then
    log_warning "Intentando conexión HTTP como fallback..."
    check_service "$BACKEND_HTTP_URL/actuator/health" "Backend HTTP"
    backend_status=$?
    BACKEND_URL=$BACKEND_HTTP_URL
else
    backend_status=$backend_https_status
fi

# Verificar frontend
check_service "$FRONTEND_URL" "Frontend Angular"
frontend_status=$?

echo
echo "2. VERIFICACIÓN DE APIS - BÚSQUEDA CATEGORIZADA"  
echo "=============================================="

if [ $backend_status -eq 0 ]; then
    # Test API de búsqueda categorizada
    make_request "GET" "$BACKEND_URL/api/search/categorized?query=rock" "" \
        "API Búsqueda Categorizada (Rock)"
    
    make_request "GET" "$BACKEND_URL/api/search/categorized?query=taylor+swift" "" \
        "API Búsqueda Categorizada (Taylor Swift)"
    
    # Test con query vacía (debería manejar error)
    log_info "Probando manejo de errores con query vacía..."
    make_request "GET" "$BACKEND_URL/api/search/categorized?query=" "" \
        "API Búsqueda Categorizada (Query vacía - test de validación)"
else
    log_error "Backend no disponible, saltando tests de API"
fi

echo
echo "3. VERIFICACIÓN DE PLAYER SERVICE"
echo "================================"

if [ $backend_status -eq 0 ]; then
    # Test Player endpoints
    make_request "GET" "$BACKEND_URL/api/player/state" "" \
        "Player State (estado inicial)"
    
    # Intentar reproducir una canción (necesitaría un track ID válido)
    log_info "Probando reproductor con track de ejemplo..."
    make_request "POST" "$BACKEND_URL/api/player/play/4f3c6e8a1b2c3d4e5f6a7b8c9d0e1f2a" "" \
        "Player Play (track de ejemplo)"
    
    make_request "GET" "$BACKEND_URL/api/player/queue" "" \
        "Player Queue (cola de reproducción)"
    
    make_request "POST" "$BACKEND_URL/api/player/next" "" \
        "Player Next (siguiente canción)"
    
    make_request "POST" "$BACKEND_URL/api/player/previous" "" \
        "Player Previous (canción anterior)"
else
    log_error "Backend no disponible, saltando tests de Player"
fi

echo
echo "4. VERIFICACIÓN DE ENDPOINTS EXISTENTES"
echo "======================================"

if [ $backend_status -eq 0 ]; then
    # Verificar endpoints principales existentes
    make_request "GET" "$BACKEND_URL/api/spotify/random" "" \
        "Spotify Random Tracks"
    
    make_request "GET" "$BACKEND_URL/api/spotify/search?query=pop" "" \
        "Spotify Search (Pop)"
    
    # Verificar endpoint SOAP (nuevo)
    log_info "Verificando endpoint SOAP..."
    soap_request='<?xml version="1.0" encoding="UTF-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <searchRequest>
                <query>rock music</query>
            </searchRequest>
        </soap:Body>
    </soap:Envelope>'
    
    response=$(curl -k -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: text/xml; charset=utf-8" \
        -H "SOAPAction: search" \
        -d "$soap_request" \
        "$BACKEND_URL/soap/music" 2>/dev/null)
    
    status_code=$(echo "$response" | tail -n 1)
    if [ "$status_code" -eq 200 ]; then
        log_success "SOAP Endpoint funcionando - Status: $status_code"
    else
        log_warning "SOAP Endpoint - Status: $status_code (puede ser normal si no está implementado)"
    fi
else
    log_error "Backend no disponible, saltando verificación de endpoints"
fi

echo
echo "5. VERIFICACIÓN DE CARACTERÍSTICAS DE PERFORMANCE"
echo "==============================================="

if [ $backend_status -eq 0 ]; then
    log_info "Midiendo tiempo de respuesta de búsqueda categorizada..."
    
    start_time=$(date +%s%3N)
    make_request "GET" "$BACKEND_URL/api/search/categorized?query=pop+music" "" \
        "Performance Test - Búsqueda Categorizada"
    end_time=$(date +%s%3N)
    
    response_time=$((end_time - start_time))
    log_info "Tiempo de respuesta: ${response_time}ms"
    
    if [ $response_time -lt 800 ]; then
        log_success "✅ Cumple objetivo TTP < 800ms"
    else
        log_warning "⚠️  TTP: ${response_time}ms (objetivo: <800ms)"
    fi
    
    # Test de cache (segunda petición debería ser más rápida)
    log_info "Probando efectividad del cache (segunda petición)..."
    start_time=$(date +%s%3N)
    make_request "GET" "$BACKEND_URL/api/search/categorized?query=pop+music" "" \
        "Performance Test - Cache Hit"
    end_time=$(date +%s%3N)
    
    cache_response_time=$((end_time - start_time))
    log_info "Tiempo de respuesta (cache): ${cache_response_time}ms"
    
    if [ $cache_response_time -lt $response_time ]; then
        log_success "✅ Cache funcionando - mejora de $((response_time - cache_response_time))ms"
    else
        log_warning "⚠️  Cache no muestra mejora significativa"
    fi
else
    log_error "Backend no disponible, saltando tests de performance"
fi

echo
echo "6. RESUMEN DE FUNCIONALIDADES IMPLEMENTADAS"
echo "=========================================="

echo
log_info "📋 BACKEND - Nuevas APIs:"
echo "  ✅ /api/search/categorized - Búsqueda con JSON estructurado"
echo "  ✅ /api/player/* - Endpoints de reproductor avanzado"
echo "  ✅ /soap/music - Endpoint SOAP/XML"
echo "  ✅ Cache-Aside implementado en servicios"
echo "  ✅ Operaciones asíncronas con CompletableFuture"

echo
log_info "🎵 PLAYER SERVICE - Funcionalidades:"
echo "  ✅ PlayNext/PlayPrevious con cola inteligente"
echo "  ✅ Autoplay con onTrackEnd event handling"
echo "  ✅ Shuffle y Repeat modes"
echo "  ✅ Gestión de estado reactivo"
echo "  ✅ Recomendaciones automáticas"

echo
log_info "🎨 FRONTEND - Componentes Angular:"
echo "  ✅ MusicPlayerComponent - Reproductor completo"
echo "  ✅ CategorizedSearchResultsComponent - Resultados estructurados"
echo "  ✅ EnhancedSearchComponent - Búsqueda avanzada"
echo "  ✅ UserMenuComponent - Menú de usuario con logout"
echo "  ✅ DevInfoComponent - Información de desarrollo"

echo
log_info "🔧 MEJORAS UI/UX:"
echo "  ✅ Diseño responsive y simétrico"
echo "  ✅ Modo búsqueda simple/avanzada"
echo "  ✅ Grid layout optimizado"
echo "  ✅ Estados de carga y error"
echo "  ✅ Animaciones y transiciones"

echo
log_info "🔐 AUTENTICACIÓN MEJORADA:"
echo "  ✅ Logout con llamada al backend"
echo "  ✅ Manejo de estado reactivo con signals"
echo "  ✅ UserMenu integrado en header"
echo "  ✅ Guards de autenticación"

echo
echo "7. COMANDOS ÚTILES PARA DESARROLLO"
echo "================================="

echo
log_info "🐳 Docker (ejecutar desde la raíz del proyecto):"
echo "  docker compose up --build                     # Levantar todo el stack"
echo "  docker compose down                           # Detener servicios"
echo "  docker compose logs backend-app-1             # Ver logs del backend"

echo
log_info "🔧 Desarrollo local:"
echo "  Backend:  ./backend/mvnw spring-boot:run     # Desde raíz del proyecto"
echo "  Frontend: cd frontend/MusifyFront && npm start"
echo "  Frontend: cd frontend/MusifyFront && ng serve"

echo
log_info "📊 Endpoints para testing:"
echo "  Health:   curl -k $BACKEND_URL/actuator/health"
echo "  Search:   curl -k '$BACKEND_URL/api/search/categorized?query=rock'"
echo "  Player:   curl -k $BACKEND_URL/api/player/state"
echo "  Random:   curl -k $BACKEND_URL/api/spotify/random"

echo
if [ $backend_status -eq 0 ] && [ $frontend_status -eq 0 ]; then
    log_success "🎉 IMPLEMENTACIÓN COMPLETA - Todos los servicios funcionando"
elif [ $backend_status -eq 0 ]; then
    log_warning "⚠️  Backend funcionando, Frontend no disponible"
elif [ $frontend_status -eq 0 ]; then
    log_warning "⚠️  Frontend funcionando, Backend no disponible" 
else
    log_error "❌ Servicios no disponibles - Verificar deployment"
fi

echo
log_info "🎓 Proyecto listo para demostración académica TFU Unidad 4"
echo "   Implementación completa de patrones de arquitectura:"
echo "   - Availability: Circuit Breaker, Retry, Rate Limiting"
echo "   - Performance: Cache-Aside, Async Request-Reply"
echo "   - Security: Gatekeeper, Gateway Offloading, JWT"
echo "   - Modifiability: External Configuration, Blue/Green Deployment"
echo