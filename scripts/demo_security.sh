#!/usr/bin/env bash
set -e

# Este script demuestra dos tácticas de seguridad (resistir ataques):
# 1. Validación de entrada: Se rechazan emails con formato inválido
# 2. Rate limiting: Se limita a 5 intentos de login por minuto

echo "Validación de entrada + Rate limit (5 por minuto):"
for i in {1..10}; do
  echo -n "Intento $i con email inválido: "
  RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"bad","password":""}')
  echo "$RESPONSE"
  sleep 0.5
done

echo
echo "Ahora con email válido:"
for i in {1..7}; do
  echo -n "Intento $i con email válido: "
  RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@test.com","password":"user123"}')
  echo "$RESPONSE"
  sleep 0.5
done
