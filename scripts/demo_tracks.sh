#!/usr/bin/env bash

# Este script demuestra la funcionalidad del catálogo de tracks
# Muestra cómo se puede buscar en el catálogo usando el endpoint /tracks

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Buscando tracks por término de búsqueda:"
echo "1. Búsqueda de 'pop':"
curl -s "${BASE_URL}/tracks?q=pop" | jq .
echo

echo "2. Búsqueda de 'urbano':"
curl -s "${BASE_URL}/tracks?q=urbano" | jq .
echo

echo "3. Búsqueda de 'taylor':"
curl -s "${BASE_URL}/tracks?q=taylor" | jq .
echo
