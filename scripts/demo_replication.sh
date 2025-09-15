#!/usr/bin/env bash
set -e

# Este script demuestra la táctica de disponibilidad mediante replicación:
# 1. Replicación: Se ejecutan dos instancias del backend (app-1 y app-2)
# 2. Balanceo de carga: NGINX distribuye el tráfico entre las instancias
# 3. Alta disponibilidad: El sistema sigue funcionando cuando una instancia falla

echo "1) Health en alta disponibilidad:"
for i in {1..6}; do curl -s http://localhost:8080/actuator/health; echo; done

echo "2) Apago app-1:"
docker stop app-1
sleep 2

echo "3) Health sigue OK vía app-2:"
for i in {1..6}; do curl -s http://localhost:8080/actuator/health; echo; done

echo "4) Enciendo app-1:"
docker start app-1
