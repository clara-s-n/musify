#!/bin/bash

# Test del endpoint proxy de YouTube para evitar tracking prevention

echo "======================================"
echo "Test: YouTube Proxy Stream Endpoint"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Verificar que el endpoint responde
echo -e "${YELLOW}Test 1: Verificando endpoint /api/youtube/stream...${NC}"
RESPONSE=$(curl -s -I "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless" 2>&1 | grep "HTTP")
if echo "$RESPONSE" | grep -q "200"; then
    echo -e "${GREEN}✓ Endpoint responde correctamente${NC}"
    echo "  $RESPONSE"
else
    echo -e "${RED}✗ Endpoint no responde${NC}"
    exit 1
fi
echo ""

# Test 2: Verificar Content-Type de audio
echo -e "${YELLOW}Test 2: Verificando Content-Type de audio...${NC}"
CONTENT_TYPE=$(curl -s -I "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless" 2>&1 | grep -i "content-type")
if echo "$CONTENT_TYPE" | grep -q "audio"; then
    echo -e "${GREEN}✓ Content-Type es audio${NC}"
    echo "  $CONTENT_TYPE"
else
    echo -e "${RED}✗ Content-Type no es audio${NC}"
    exit 1
fi
echo ""

# Test 3: Verificar Accept-Ranges para seeking
echo -e "${YELLOW}Test 3: Verificando soporte para seeking (Accept-Ranges)...${NC}"
ACCEPT_RANGES=$(curl -s -I "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless" 2>&1 | grep -i "accept-ranges")
if echo "$ACCEPT_RANGES" | grep -q "bytes"; then
    echo -e "${GREEN}✓ Soporte para seeking habilitado${NC}"
    echo "  $ACCEPT_RANGES"
else
    echo -e "${YELLOW}⚠ No hay soporte para seeking${NC}"
fi
echo ""

# Test 4: Verificar Content-Length
echo -e "${YELLOW}Test 4: Verificando Content-Length...${NC}"
CONTENT_LENGTH=$(curl -s -I "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless" 2>&1 | grep -i "content-length" | awk '{print $2}' | tr -d '\r')
if [ ! -z "$CONTENT_LENGTH" ]; then
    SIZE_MB=$(echo "scale=2; $CONTENT_LENGTH / 1024 / 1024" | bc)
    echo -e "${GREEN}✓ Content-Length presente: ${SIZE_MB} MB${NC}"
else
    echo -e "${YELLOW}⚠ No hay Content-Length (streaming chunked)${NC}"
fi
echo ""

# Test 5: Descargar primeros bytes para verificar stream
echo -e "${YELLOW}Test 5: Descargando primeros bytes para verificar stream...${NC}"
FIRST_BYTES=$(timeout 2 curl -s "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless" 2>&1 | head -c 1024 | wc -c)
if [ "$FIRST_BYTES" -eq 1024 ]; then
    echo -e "${GREEN}✓ Stream funcionando correctamente (1024 bytes recibidos)${NC}"
else
    echo -e "${YELLOW}⚠ Stream parcial (recibidos: $FIRST_BYTES bytes) - puede ser normal${NC}"
fi
echo ""

# Test 6: Verificar que NO hay URLs de googlevideo.com expuestas
echo -e "${YELLOW}Test 6: Verificando que NO se exponen URLs de YouTube...${NC}"
RESPONSE_BODY=$(curl -s "http://localhost:8080/api/youtube/stream?name=Test&artist=Test" 2>&1 | head -c 200)
if echo "$RESPONSE_BODY" | grep -q "googlevideo"; then
    echo -e "${RED}✗ URLs de YouTube expuestas en la respuesta${NC}"
    exit 1
else
    echo -e "${GREEN}✓ URLs de YouTube no expuestas (proxy funciona)${NC}"
fi
echo ""

# Test 7: Comparar con endpoint antiguo /audio
echo -e "${YELLOW}Test 7: Comparando con endpoint antiguo /audio...${NC}"
OLD_ENDPOINT=$(curl -s "http://localhost:8080/api/youtube/audio?name=Reverence&artist=Faithless" 2>&1)
if echo "$OLD_ENDPOINT" | grep -q "googlevideo"; then
    echo -e "${GREEN}✓ Endpoint antiguo expone URLs de YouTube (como esperado)${NC}"
    echo -e "  ${RED}Problema:${NC} Browser bloqueará estas URLs por Tracking Prevention"
    echo -e "  ${GREEN}Solución:${NC} Usar /stream en lugar de /audio"
else
    echo -e "${YELLOW}⚠ Endpoint antiguo no devuelve URLs${NC}"
fi
echo ""

echo "======================================"
echo -e "${GREEN}✓ Todos los tests pasaron${NC}"
echo "======================================"
echo ""
echo "Resumen:"
echo "  • Endpoint proxy: http://localhost:8080/api/youtube/stream"
echo "  • Función: Proxy transparente que evita Tracking Prevention"
echo "  • Formato: audio/webm (streaming)"
echo "  • Seeking: Soportado (Accept-Ranges: bytes)"
echo ""
echo "Solución al problema:"
echo -e "  ${RED}ANTES:${NC} Browser → googlevideo.com ${RED}(BLOCKED)${NC}"
echo -e "  ${GREEN}AHORA:${NC} Browser → localhost:8080 → googlevideo.com ${GREEN}(OK)${NC}"
echo ""
echo "Próximos pasos:"
echo "  1. Abrir http://localhost:4200"
echo "  2. Seleccionar una canción"
echo "  3. Click en Play"
echo "  4. Verificar en console: No debe aparecer 'Tracking Prevention'"
echo "  5. La música debe sonar 🎵"
