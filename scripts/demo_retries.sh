#!/usr/bin/env bash
set -e

# Este script demuestra tácticas de disponibilidad y recuperación ante fallos:
# 1. Reintentos (@Retry): La aplicación reintenta automáticamente hasta 3 veces
# 2. Circuit Breaker (@CircuitBreaker): Previene cascada de fallos
# 3. Degradación elegante: Proporciona URL alternativa cuando el servicio principal falla

echo "Probando el endpoint de health (sin autenticación):"
curl -s http://localhost:8080/actuator/health
echo

echo "Probando /playback/start con fallas aleatorias + retries + CB + fallback:"
for i in {1..10}; do
  echo -n "Intento $i: "
  RESPONSE=$(curl -s -X POST "http://localhost:8080/playback/start?trackId=T$i")
  echo "$RESPONSE"
  sleep 1
done
