#!/usr/bin/env bash
set -e
echo "Probando /playback/start con fallas aleatorias + retries + CB + fallback:"
for i in {1..10}; do
  curl -s "http://localhost:8080/playback/start?trackId=T$i"; echo
done
