#!/usr/bin/env bash
set -e

# Este script demuestra la funcionalidad del catálogo de tracks
# Muestra cómo se puede buscar en el catálogo usando el endpoint /tracks

echo "Buscando tracks por término de búsqueda:"
echo "1. Búsqueda de 'pop':"
curl -s "http://localhost:8080/tracks?q=pop" | jq .
echo

echo "2. Búsqueda de 'urbano':"
curl -s "http://localhost:8080/tracks?q=urbano" | jq .
echo

echo "3. Búsqueda de 'taylor':"
curl -s "http://localhost:8080/tracks?q=taylor" | jq .
echo
