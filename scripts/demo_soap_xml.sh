#!/bin/bash

# Demo script para endpoints SOAP/XML de Musify
# Este script demuestra la funcionalidad SOAP complementaria a la API REST

echo "=========================================="
echo "     DEMO: Endpoints SOAP/XML Musify"
echo "=========================================="
echo

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080"

echo -e "${BLUE}Verificando que la aplicación esté ejecutándose...${NC}"
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${RED}Error: La aplicación no está ejecutándose en ${BASE_URL}${NC}"
    echo "Por favor ejecuta: docker compose up -d"
    exit 1
fi
echo -e "${GREEN}✓ Aplicación ejecutándose correctamente${NC}"
echo

echo -e "${BLUE}=== 1. DEMO: Búsqueda de Música SOAP/XML ===${NC}"
echo -e "${YELLOW}Endpoint: POST /soap/music/search${NC}"
echo -e "${YELLOW}Buscando canciones de 'jazz' (límite: 3)${NC}"
echo

echo "REQUEST XML:"
echo '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>'
echo

echo "RESPONSE:"
curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>' \
-w "\n" | xmllint --format - 2>/dev/null || curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>' \
-w "\n"

echo
echo -e "${GREEN}✓ Búsqueda SOAP completada${NC}"
echo

echo -e "${BLUE}=== 2. DEMO: Música Aleatoria SOAP/XML ===${NC}"
echo -e "${YELLOW}Endpoint: POST /soap/music/random${NC}"
echo -e "${YELLOW}Obteniendo 2 canciones aleatorias${NC}"
echo

echo "REQUEST XML:"
echo '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <limit>2</limit>
</randomRequest>'
echo

echo "RESPONSE:"
curl -X POST "${BASE_URL}/soap/music/random" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <limit>2</limit>
</randomRequest>' \
-w "\n" | xmllint --format - 2>/dev/null || curl -X POST "${BASE_URL}/soap/music/random" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <limit>2</limit>
</randomRequest>' \
-w "\n"

echo
echo -e "${GREEN}✓ Música aleatoria SOAP completada${NC}"
echo

echo -e "${BLUE}=== 3. DEMO: Comparación REST vs SOAP ===${NC}"
echo -e "${YELLOW}Comparando la misma funcionalidad en REST (JSON) vs SOAP (XML)${NC}"
echo

echo -e "${BLUE}REST JSON Request:${NC}"
echo "GET ${BASE_URL}/music/spotify/search?query=rock&limit=2"
echo
echo -e "${BLUE}REST JSON Response:${NC}"
curl -s "${BASE_URL}/music/spotify/search?query=rock&limit=2" | jq . 2>/dev/null || curl -s "${BASE_URL}/music/spotify/search?query=rock&limit=2"
echo
echo

echo -e "${BLUE}SOAP XML Request:${NC}"
echo "POST ${BASE_URL}/soap/music/search"
echo '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>rock</query>
    <limit>2</limit>
</searchRequest>'
echo

echo -e "${BLUE}SOAP XML Response:${NC}"
curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>rock</query>
    <limit>2</limit>
</searchRequest>' \
-w "\n" | xmllint --format - 2>/dev/null || curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>rock</query>
    <limit>2</limit>
</searchRequest>' \
-w "\n"

echo
echo -e "${GREEN}✓ Comparación REST vs SOAP completada${NC}"
echo

echo -e "${BLUE}=== 4. DEMO: Testing con diferentes parámetros ===${NC}"
echo -e "${YELLOW}Probando búsqueda con mayor límite (5 resultados)${NC}"
echo

curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>pop</query>
    <limit>5</limit>
</searchRequest>' \
-w "\n" | xmllint --format - 2>/dev/null || curl -X POST "${BASE_URL}/soap/music/search" \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>pop</query>
    <limit>5</limit>
</searchRequest>' \
-w "\n"

echo
echo -e "${GREEN}✓ Testing con parámetros completado${NC}"
echo

echo "=========================================="
echo -e "${GREEN}     DEMO SOAP/XML COMPLETADO${NC}"
echo "=========================================="
echo
echo "Resumen de endpoints SOAP probados:"
echo "• POST /soap/music/search - Búsqueda de música con XML"
echo "• POST /soap/music/random - Música aleatoria con XML"
echo
echo "Para más detalles, consulta: docs/api/SOAP_XML_API_Guide.md"
echo "Para comparar con REST: docs/api/Musify_API_Testing_Guide.md"
echo