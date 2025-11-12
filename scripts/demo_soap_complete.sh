#!/bin/bash

###############################################################################
# Script de Prueba Completo - Endpoints SOAP/XML
# Musify API - TFU Unidad 4
#
# Este script demuestra el uso de los endpoints SOAP/XML para búsqueda
# de música y obtención de canciones aleatorias.
###############################################################################

set -e  # Exit on error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="http://localhost:8080"
SOAP_ENDPOINT="${BASE_URL}/soap/music"

# Función para imprimir headers
print_header() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
}

# Función para imprimir mensajes
print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Función para formatear XML
format_xml() {
    if command -v xmllint &> /dev/null; then
        xmllint --format - 2>/dev/null || cat
    else
        cat
    fi
}

###############################################################################
# INICIO DEL SCRIPT
###############################################################################

print_header "🎵 DEMO COMPLETO - ENDPOINTS SOAP/XML"

print_info "Backend URL: ${BASE_URL}"
print_info "SOAP Endpoint: ${SOAP_ENDPOINT}"
echo ""

###############################################################################
# 1. VERIFICAR SALUD DEL BACKEND
###############################################################################

print_header "1. Verificando salud del backend"

if curl -s -f "${BASE_URL}/actuator/health" > /dev/null; then
    print_success "Backend está activo y respondiendo"
    HEALTH=$(curl -s "${BASE_URL}/actuator/health" | head -3)
    echo "$HEALTH"
else
    print_error "Backend no está accesible en ${BASE_URL}"
    exit 1
fi

echo ""
sleep 1

###############################################################################
# 2. BÚSQUEDA DE MÚSICA POR ARTISTA (SOAP)
###############################################################################

print_header "2. Búsqueda SOAP - Artista: 'Billie Eilish'"

print_info "Enviando request SOAP/XML..."

SEARCH_REQUEST='<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>Billie Eilish</query>
    <limit>5</limit>
</searchMusicRequest>'

echo ""
echo -e "${YELLOW}Request XML:${NC}"
echo "$SEARCH_REQUEST" | format_xml
echo ""

print_info "Ejecutando: POST ${SOAP_ENDPOINT}/search"
RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/search" \
    -H "Content-Type: application/xml" \
    -d "$SEARCH_REQUEST")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_success "Búsqueda exitosa (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${GREEN}Response XML:${NC}"
    echo "$BODY" | format_xml | head -50
    echo ""
    
    # Contar resultados
    TRACK_COUNT=$(echo "$BODY" | grep -o "<track>" | wc -l)
    print_success "Canciones encontradas: $TRACK_COUNT"
else
    print_error "Error en búsqueda (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 2

###############################################################################
# 3. BÚSQUEDA DE MÚSICA POR GÉNERO (SOAP)
###############################################################################

print_header "3. Búsqueda SOAP - Género: 'rock'"

SEARCH_ROCK='<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>rock</query>
    <limit>3</limit>
</searchMusicRequest>'

echo -e "${YELLOW}Request XML:${NC}"
echo "$SEARCH_ROCK" | format_xml
echo ""

print_info "Ejecutando: POST ${SOAP_ENDPOINT}/search"
RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/search" \
    -H "Content-Type: application/xml" \
    -d "$SEARCH_ROCK")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_success "Búsqueda exitosa (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${GREEN}Response XML:${NC}"
    echo "$BODY" | format_xml | head -40
    echo ""
else
    print_error "Error en búsqueda (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 2

###############################################################################
# 4. MÚSICA ALEATORIA (SOAP)
###############################################################################

print_header "4. Obtener Música Aleatoria (SOAP)"

print_info "Solicitando 8 canciones aleatorias..."

RANDOM_REQUEST='<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <limit>8</limit>
</getRandomMusicRequest>'

echo ""
echo -e "${YELLOW}Request XML:${NC}"
echo "$RANDOM_REQUEST" | format_xml
echo ""

print_info "Ejecutando: POST ${SOAP_ENDPOINT}/random"
RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/random" \
    -H "Content-Type: application/xml" \
    -d "$RANDOM_REQUEST")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_success "Música aleatoria obtenida exitosamente (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${GREEN}Response XML (primeras canciones):${NC}"
    echo "$BODY" | format_xml | head -50
    echo ""
    
    # Contar resultados
    TRACK_COUNT=$(echo "$BODY" | grep -o "<track>" | wc -l)
    print_success "Canciones recibidas: $TRACK_COUNT"
else
    print_error "Error al obtener música aleatoria (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 2

###############################################################################
# 5. PRUEBA DE VALIDACIÓN - QUERY VACÍO
###############################################################################

print_header "5. Prueba de Validación - Query Vacío"

print_info "Enviando búsqueda con query vacío (debe fallar)..."

INVALID_REQUEST='<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query></query>
    <limit>5</limit>
</searchMusicRequest>'

echo ""
echo -e "${YELLOW}Request XML:${NC}"
echo "$INVALID_REQUEST" | format_xml
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/search" \
    -H "Content-Type: application/xml" \
    -d "$INVALID_REQUEST")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    print_success "Validación correcta - Request rechazado (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${YELLOW}Response XML:${NC}"
    echo "$BODY" | format_xml
else
    print_warning "Respuesta inesperada (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 2

###############################################################################
# 6. PRUEBA DE VALIDACIÓN - LÍMITE FUERA DE RANGO
###############################################################################

print_header "6. Prueba de Validación - Límite Fuera de Rango"

print_info "Enviando request con límite > 50 (debe fallar)..."

INVALID_LIMIT='<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <limit>100</limit>
</getRandomMusicRequest>'

echo ""
echo -e "${YELLOW}Request XML:${NC}"
echo "$INVALID_LIMIT" | format_xml
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/random" \
    -H "Content-Type: application/xml" \
    -d "$INVALID_LIMIT")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "400" ]; then
    print_success "Validación correcta - Request rechazado (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${YELLOW}Response XML:${NC}"
    echo "$BODY" | format_xml
else
    print_warning "Respuesta inesperada (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 1

###############################################################################
# 7. BÚSQUEDA ESPECÍFICA - CANCIÓN FAMOSA
###############################################################################

print_header "7. Búsqueda SOAP - Canción: 'Shape of You'"

SEARCH_SONG='<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>Shape of You Ed Sheeran</query>
    <limit>3</limit>
</searchMusicRequest>'

echo -e "${YELLOW}Request XML:${NC}"
echo "$SEARCH_SONG" | format_xml
echo ""

print_info "Ejecutando: POST ${SOAP_ENDPOINT}/search"
RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST "${SOAP_ENDPOINT}/search" \
    -H "Content-Type: application/xml" \
    -d "$SEARCH_SONG")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_success "Búsqueda exitosa (HTTP $HTTP_CODE)"
    echo ""
    echo -e "${GREEN}Response XML:${NC}"
    echo "$BODY" | format_xml
    echo ""
else
    print_error "Error en búsqueda (HTTP $HTTP_CODE)"
    echo "$BODY" | format_xml
fi

echo ""
sleep 1

###############################################################################
# RESUMEN FINAL
###############################################################################

print_header "📊 RESUMEN DE PRUEBAS SOAP/XML"

echo ""
echo -e "${GREEN}✓${NC} Endpoints SOAP probados exitosamente"
echo ""
echo "Endpoints disponibles:"
echo "  1. POST /soap/music/search   - Búsqueda de música"
echo "  2. POST /soap/music/random   - Música aleatoria"
echo ""
echo "Características demostradas:"
echo "  • Requests y responses en formato XML"
echo "  • Validación de parámetros (query, limit)"
echo "  • Manejo de errores con respuestas XML"
echo "  • Integración con Spotify API"
echo "  • Búsqueda por artista, género, canción"
echo "  • Obtención de música aleatoria"
echo ""
echo -e "${CYAN}Namespace XML: http://tfu.com/backend/soap/music${NC}"
echo ""

print_header "✨ Demo completado exitosamente"
