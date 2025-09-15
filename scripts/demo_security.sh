#!/usr/bin/env bash
set -e

# Este script demuestra dos tácticas de seguridad (resistir ataques):
# 1. Validación de entrada: Se rechazan emails con formato inválido
# 2. Rate limiting: Se limita a 5 intentos de login por minuto

echo "Validación de entrada + Rate limit (5 por minuto):"
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"bad","password":""}'; echo
done
echo "Ahora con email válido:"
for i in {1..7}; do
  curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@test.com","password":"x"}'; echo
done
