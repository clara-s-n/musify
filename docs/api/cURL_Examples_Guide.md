# Ejemplos cURL para Musify REST API

Esta guía proporciona ejemplos de cURL para todos los endpoints REST de Musify. Útil para testing rápido, scripts automatizados, y desarrolladores que prefieren la línea de comandos.

## 🌐 Variables de Entorno

Configura estas variables para uso fácil:

```bash
export MUSIFY_URL="http://localhost:8080"
export JWT_TOKEN=""  # Se actualizará después del login
```

## 🔐 Autenticación

### Registro de Usuario
```bash
curl -X POST "${MUSIFY_URL}/api/auth/register" \
-H "Content-Type: application/json" \
-d '{
    "username": "usuario_prueba",
    "email": "usuario@ejemplo.com", 
    "password": "password123"
}' | jq .
```

### Login y Obtener JWT Token
```bash
# Login y extraer token
RESPONSE=$(curl -s -X POST "${MUSIFY_URL}/api/auth/login" \
-H "Content-Type: application/json" \
-d '{
    "email": "test@example.com",
    "password": "password123"
}')

echo "$RESPONSE" | jq .

# Extraer token para uso posterior
export JWT_TOKEN=$(echo "$RESPONSE" | jq -r '.token')
echo "Token JWT: $JWT_TOKEN"
```

### Login Admin
```bash
curl -X POST "${MUSIFY_URL}/api/auth/login" \
-H "Content-Type: application/json" \
-d '{
    "email": "admin@demo.com",
    "password": "admin"
}' | jq .
```

## 🎵 Spotify Music API (Endpoints Públicos)

### Buscar Canciones
```bash
# Búsqueda básica
curl -s "${MUSIFY_URL}/music/spotify/search?q=jazz&limit=5" | jq .

# Búsqueda por artista específico
curl -s "${MUSIFY_URL}/music/spotify/search?q=queen&limit=3" | jq .

# Búsqueda con URL encoding para espacios
curl -s "${MUSIFY_URL}/music/spotify/search?q=rolling%20stones&limit=2" | jq .
```

### Música Aleatoria
```bash
# 5 canciones aleatorias
curl -s "${MUSIFY_URL}/music/spotify/random?limit=5" | jq .

# 1 canción aleatoria
curl -s "${MUSIFY_URL}/music/spotify/random?limit=1" | jq .
```

### Obtener Canción por ID de Spotify
```bash
# Canción específica (Bohemian Rhapsody - Queen)
curl -s "${MUSIFY_URL}/music/spotify/play/4iV5W9uYEdYUVa79Axb7Rh" | jq .

# Con verbose para ver headers
curl -v "${MUSIFY_URL}/music/spotify/play/4iV5W9uYEdYUVa79Axb7Rh"
```

## 🎼 Tracks Management (Requiere Autenticación)

### Listar Todas las Canciones
```bash
curl -s "${MUSIFY_URL}/api/tracks" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

### Obtener Canción por ID Local
```bash
curl -s "${MUSIFY_URL}/api/tracks/1" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

### Crear Nueva Canción
```bash
curl -X POST "${MUSIFY_URL}/api/tracks" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${JWT_TOKEN}" \
-d '{
    "title": "Mi Nueva Canción",
    "artist": "Artista Demo",
    "album": "Álbum Demo", 
    "duration": 180,
    "genre": "Pop"
}' | jq .
```

### Actualizar Canción
```bash
curl -X PUT "${MUSIFY_URL}/api/tracks/1" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${JWT_TOKEN}" \
-d '{
    "title": "Canción Actualizada",
    "artist": "Artista Actualizado",
    "album": "Álbum Actualizado",
    "duration": 200,
    "genre": "Rock"
}' | jq .
```

### Eliminar Canción
```bash
curl -X DELETE "${MUSIFY_URL}/api/tracks/1" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

## ▶️ Playback Control (Requiere Autenticación)

### Iniciar Reproducción
```bash
curl -X POST "${MUSIFY_URL}/api/playback/start" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${JWT_TOKEN}" \
-d '{
    "trackId": "4iV5W9uYEdYUVa79Axb7Rh"
}' | jq .
```

### Pausar Reproducción
```bash
curl -X POST "${MUSIFY_URL}/api/playback/pause" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

### Reanudar Reproducción
```bash
curl -X POST "${MUSIFY_URL}/api/playback/resume" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

### Detener Reproducción
```bash
curl -X POST "${MUSIFY_URL}/api/playback/stop" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

### Estado de Reproducción
```bash
curl -s "${MUSIFY_URL}/api/playback/status" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .
```

## 🔧 Monitoreo y Salud (Endpoints Públicos)

### Health Check Básico
```bash
curl -s "${MUSIFY_URL}/actuator/health" | jq .
```

### Health Check Detallado
```bash
curl -s "${MUSIFY_URL}/actuator/health/db" | jq .
```

### Información de la Aplicación
```bash
curl -s "${MUSIFY_URL}/actuator/info" | jq .
```

### Métricas Disponibles
```bash
curl -s "${MUSIFY_URL}/actuator/metrics" | jq .
```

### Métrica Específica (ejemplo: memoria JVM)
```bash
curl -s "${MUSIFY_URL}/actuator/metrics/jvm.memory.used" | jq .
```

## 📚 Documentación API

### OpenAPI/Swagger JSON
```bash
curl -s "${MUSIFY_URL}/v3/api-docs" | jq .
```

### Swagger UI (abre en navegador)
```bash
# Linux/WSL
xdg-open "${MUSIFY_URL}/swagger-ui.html"

# macOS  
open "${MUSIFY_URL}/swagger-ui.html"

# Windows
start "${MUSIFY_URL}/swagger-ui.html"
```

## 🧪 Scripts de Testing Automatizado

### Script Completo de Testing
```bash
#!/bin/bash

# Test completo de la API REST
echo "=== Testing Musify REST API ==="

# 1. Health Check
echo "1. Verificando salud de la aplicación..."
curl -s "${MUSIFY_URL}/actuator/health" | jq .

# 2. Login y obtener token
echo "2. Haciendo login..."
JWT_TOKEN=$(curl -s -X POST "${MUSIFY_URL}/api/auth/login" \
-H "Content-Type: application/json" \
-d '{"email": "test@example.com", "password": "password123"}' \
| jq -r '.token')

if [ "$JWT_TOKEN" != "null" ] && [ -n "$JWT_TOKEN" ]; then
    echo "✅ Login exitoso"
else
    echo "❌ Error en login"
    exit 1
fi

# 3. Buscar música
echo "3. Buscando música..."
curl -s "${MUSIFY_URL}/music/spotify/search?q=jazz&limit=2" | jq .

# 4. Música aleatoria
echo "4. Obteniendo música aleatoria..."
curl -s "${MUSIFY_URL}/music/spotify/random?limit=1" | jq .

# 5. Control de reproducción
echo "5. Iniciando reproducción..."
curl -s -X POST "${MUSIFY_URL}/api/playback/start" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${JWT_TOKEN}" \
-d '{"trackId": "4iV5W9uYEdYUVa79Axb7Rh"}' | jq .

echo "6. Verificando estado de reproducción..."
curl -s "${MUSIFY_URL}/api/playback/status" \
-H "Authorization: Bearer ${JWT_TOKEN}" | jq .

echo "=== Testing completado ==="
```

### Testing de Rate Limiting
```bash
#!/bin/bash

echo "=== Testing Rate Limiting ==="
for i in {1..7}; do
    echo "Intento $i:"
    curl -s -X POST "${MUSIFY_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email": "test@example.com", "password": "password123"}' \
    | jq '.message'
    sleep 1
done
```

### Testing de Resilencia
```bash
#!/bin/bash

echo "=== Testing Resilencia (Circuit Breaker) ==="

# Requiere JWT token válido
JWT_TOKEN="tu_token_aqui"

# Múltiples requests para activar circuit breaker
for i in {1..5}; do
    echo "Request $i:"
    curl -s -X POST "${MUSIFY_URL}/api/playback/start" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -d '{"trackId": "invalid_track_id"}' \
    | jq '.message'
    sleep 2
done
```

## 🔍 Tips para Debugging

### Ver Headers Completos
```bash
curl -v "${MUSIFY_URL}/music/spotify/search?q=test&limit=1"
```

### Solo Headers de Response
```bash
curl -I "${MUSIFY_URL}/actuator/health"
```

### Timing de Response
```bash
curl -w "@-" -s "${MUSIFY_URL}/music/spotify/search?q=test&limit=1" <<'EOF'
     time_namelookup:  %{time_namelookup}s\n
        time_connect:  %{time_connect}s\n
     time_appconnect:  %{time_appconnect}s\n
    time_pretransfer:  %{time_pretransfer}s\n
       time_redirect:  %{time_redirect}s\n
  time_starttransfer:  %{time_starttransfer}s\n
                     ----------\n
          time_total:  %{time_total}s\n
EOF
```

### Guardar Response en Archivo
```bash
curl -s "${MUSIFY_URL}/music/spotify/search?q=jazz&limit=10" > search_results.json
```

## 🔧 Troubleshooting

### Verificar Conectividad
```bash
# Ping básico
curl -s "${MUSIFY_URL}/actuator/health" && echo "✅ Conectividad OK" || echo "❌ No hay conectividad"
```

### Verificar JWT Token
```bash
# Decodificar JWT (requiere jq y base64)
echo "$JWT_TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq .
```

### Logs de Error Detallados
```bash
# Con máximo detalle de error
curl -v -X POST "${MUSIFY_URL}/api/auth/login" \
-H "Content-Type: application/json" \
-d '{"email": "invalid", "password": "invalid"}' 2>&1
```

---

## 📝 Notas

- **jq**: Estos ejemplos usan `jq` para formatear JSON. Instálalo con: `sudo apt install jq` (Ubuntu/Debian) o `brew install jq` (macOS)
- **Variables**: Ajusta `MUSIFY_URL` según tu entorno (desarrollo, testing, producción)
- **HTTPS**: Para acceso directo al backend, usa `https://localhost:8443` y añade `-k` para ignorar certificados self-signed
- **Rate Limiting**: El endpoint de login tiene límite de 5 intentos por minuto por IP

¡Todos estos comandos están listos para copiar y pegar! 🚀