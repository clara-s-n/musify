#!/usr/bin/env bash
# Este script demuestra el monitoreo y la observabilidad del sistema:
# - Endpoints de salud: Proporciona información sobre el estado del sistema
# - Documentación API: Muestra la documentación OpenAPI automática

curl -s http://localhost:8080/actuator/health | jq .
curl -s http://localhost:8080/v3/api-docs | head
