#!/usr/bin/env bash
set -e
echo "1) Health en alta disponibilidad:"
for i in {1..6}; do curl -s http://localhost:8080/actuator/health; echo; done

echo "2) Apago app-1:"
docker stop app-1
sleep 2

echo "3) Health sigue OK vía app-2:"
for i in {1..6}; do curl -s http://localhost:8080/actuator/health; echo; done

echo "4) Enciendo app-1:"
docker start app-1
