#!/usr/bin/env bash

# Este script demuestra tácticas de disponibilidad y recuperación ante fallos:
# 1. Reintentos (@Retry): La aplicación reintenta automáticamente hasta 3 veces
# 2. Circuit Breaker (@CircuitBreaker): Previene cascada de fallos
# 3. Degradación elegante: Proporciona URL alternativa cuando el servicio principal falla

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

# función helper que SIEMPRE imprime status + cuerpo
req() {
  local method="$1"; shift
  local url="$1"; shift
  # imprime: --- URL --- ; luego status-line; headers; línea en blanco; body
  echo -e "\n--- ${method} ${url} ---"
  curl -sS -X "$method" "$url" \
    -H 'Accept: application/json' "$@" \
    -i  # <- imprime status line + headers + body
}

echo "Probando el endpoint de health (sin autenticación):"
curl -s ${BASE_URL}/actuator/health
echo

echo "Probando /playback/start con fallas aleatorias + retries + CB + fallback:"

for i in {1..10}; do
  echo "Intento $i:"
  req POST "${BASE_URL}/playback/start?trackId=T$i" \
    -H 'Content-Type: application/json' \
    --user user:password || true
  sleep 1
done
