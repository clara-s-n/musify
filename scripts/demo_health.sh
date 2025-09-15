#!/usr/bin/env bash
set -e
curl -s http://localhost:8080/actuator/health | jq .
curl -s http://localhost:8080/v3/api-docs | head
