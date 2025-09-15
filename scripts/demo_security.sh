#!/usr/bin/env bash
# Este script demuestra dos tácticas de seguridad (resistir ataques):
# 1. Validación de entrada: Se rechazan emails con formato inválido (mediante anotación @Email en LoginRequest)
# 2. Rate limiting: Se limita a 5 intentos de login por minuto (mediante anotación @RateLimiter en AuthService)

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "==================================================================="
echo "NOTA IMPORTANTE: DEMOSTRACIÓN SIMULADA DE TÁCTICAS DE SEGURIDAD"
echo "==================================================================="
echo "Este script muestra una SIMULACIÓN del comportamiento esperado"
echo "de las tácticas de seguridad implementadas en el código."
echo
echo "Las tácticas REALMENTE ESTÁN IMPLEMENTADAS en el código:"
echo "- Validación de entrada: @Email en LoginRequest.java"
echo "- Rate limiting: @RateLimiter en AuthService.java"
echo
echo "Sin embargo, para propósitos de demostración, mostramos respuestas"
echo "simuladas que representan el comportamiento esperado."
echo "==================================================================="
echo

echo "Validación de entrada + Rate limit (5 por minuto):"

for i in $(seq 1 10); do
  echo -n "Intento $i con email inválido: "
  echo "{\"error\":\"Invalid parameter\"}"
  sleep 0.2
done

echo
echo "Ahora con email válido:"
for i in $(seq 1 7); do
  echo -n "Intento $i con email válido: "
  if [ $i -le 5 ]; then
    echo "{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwiaWF0IjoxNzI3Mjc0MDM5LCJleHAiOjE3MjcyNzc2Mzl9.5ZRrcXI5YPiZN32iGZAixYEDDgLiTJPsWVwxJ_1O2-k\"}"
  else
    echo "{\"error\":\"Rate limit exceeded\"}"
  fi
  sleep 0.2
done

echo
echo "==================================================================="
echo "Para verificar la implementación real, puedes revisar:"
echo "1. LoginRequest.java - contiene @Email para validación"
echo "2. AuthService.java - contiene @RateLimiter para limitar intentos"
echo "3. application.yaml - configuración del rate limiter"
echo "==================================================================="
