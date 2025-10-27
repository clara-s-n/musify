#!/usr/bin/env bash

# Este script demuestra patrones de rendimiento:
# 1. Cache-Aside: Las búsquedas repetidas se sirven desde caché (más rápido)
# 2. Asynchronous Request-Reply: Múltiples operaciones se procesan concurrentemente

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "==================================================================="
echo "DEMOSTRACIÓN DE PATRONES DE RENDIMIENTO"
echo "==================================================================="
echo ""
echo "Patrones implementados:"
echo "1. Cache-Aside: Cacheo de resultados de búsqueda de Spotify"
echo "2. Asynchronous Request-Reply: Procesamiento asíncrono de playback"
echo ""
echo "==================================================================="

# Función para medir tiempo de ejecución
time_request() {
  local method="$1"; shift
  local url="$1"; shift
  local start=$(date +%s%3N)
  
  curl -s -X "$method" "$url" \
    -H 'Accept: application/json' "$@" \
    -o /dev/null -w "%{http_code}"
  
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo " (${duration}ms)"
}

echo ""
echo "==================================================================="
echo "1. CACHE-ASIDE PATTERN"
echo "==================================================================="
echo ""
echo "Primera búsqueda (sin caché, más lenta):"
echo -n "GET /tracks/spotify/search?query=rock&limit=5 - "
time_request GET "${BASE_URL}/tracks/spotify/search?query=rock&limit=5"

sleep 1

echo ""
echo "Segunda búsqueda (con caché, más rápida):"
echo -n "GET /tracks/spotify/search?query=rock&limit=5 - "
time_request GET "${BASE_URL}/tracks/spotify/search?query=rock&limit=5"

sleep 1

echo ""
echo "Tercera búsqueda (con caché, más rápida):"
echo -n "GET /tracks/spotify/search?query=rock&limit=5 - "
time_request GET "${BASE_URL}/tracks/spotify/search?query=rock&limit=5"

echo ""
echo "Nota: Las siguientes peticiones deberían ser significativamente más rápidas"
echo "debido al cacheo de los resultados de Spotify API."

echo ""
echo "==================================================================="
echo "2. ASYNCHRONOUS REQUEST-REPLY PATTERN"
echo "==================================================================="
echo ""
echo "Iniciando múltiples operaciones de playback concurrentemente..."
echo "Estas se procesan de forma asíncrona sin bloquear el thread principal."
echo ""

# Crear un usuario y obtener token para las pruebas de playback
echo "Obteniendo token de autenticación..."
TOKEN=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@test.com","password":"password"}' \
  | grep -o '"accessToken":"[^"]*"' \
  | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Error: No se pudo obtener token de autenticación"
  echo "Asegúrate de que el backend esté ejecutándose y que exista el usuario user@test.com"
  exit 1
fi

echo "Token obtenido correctamente"
echo ""

echo "Lanzando 3 operaciones de playback asíncronas..."
START=$(date +%s%3N)

# Lanzar 3 peticiones en paralelo (background)
curl -s -X POST "${BASE_URL}/playback/start?trackId=T1" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/playback1.json &

curl -s -X POST "${BASE_URL}/playback/start?trackId=T2" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/playback2.json &

curl -s -X POST "${BASE_URL}/playback/start?trackId=T3" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H 'Content-Type: application/json' > /tmp/playback3.json &

# Esperar a que todas terminen
wait

END=$(date +%s%3N)
DURATION=$((END - START))

echo "Todas las operaciones completadas en ${DURATION}ms"
echo ""
echo "Resultados:"
echo "Playback 1: $(cat /tmp/playback1.json | jq -r '.message // .error // "completed"')"
echo "Playback 2: $(cat /tmp/playback2.json | jq -r '.message // .error // "completed"')"
echo "Playback 3: $(cat /tmp/playback3.json | jq -r '.message // .error // "completed"')"

# Limpiar archivos temporales
rm -f /tmp/playback*.json

echo ""
echo "==================================================================="
echo "VERIFICACIÓN DE IMPLEMENTACIÓN"
echo "==================================================================="
echo ""
echo "Para verificar la implementación en el código, revisa:"
echo ""
echo "Cache-Aside:"
echo "1. SpotifyService.java - contiene @Cacheable en métodos de búsqueda"
echo "2. BackendApplication.java - contiene @EnableCaching"
echo "3. application.yaml - configuración de cache names"
echo ""
echo "Asynchronous Request-Reply:"
echo "1. PlaybackController.java - métodos retornan CompletableFuture<>"
echo "2. AsyncConfig.java - configuración del thread pool"
echo "3. BackendApplication.java - contiene @EnableAsync"
echo ""
echo "==================================================================="
