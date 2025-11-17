# 📚 Guía Completa de la API Musify

> **Guía unificada para todos los endpoints REST y SOAP de la aplicación Musify**

## 🎯 Información General

La API de Musify ofrece dos interfaces:
- **REST/JSON**: Para integración moderna y aplicaciones web
- **SOAP/XML**: Para sistemas enterprise y compatibilidad legacy

### URLs Base
- **REST API**: `https://localhost:8443` (HTTPS) o `http://localhost:8080` (HTTP)
- **SOAP API**: `http://localhost:8080/soap`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

## 🚀 Inicio Rápido

### 1. Configuración con Postman
1. Importa la colección: `Musify_Complete_JSON_Collection.postman_collection.json`
2. Importa el environment: `Musify_Complete_JSON_Environment.postman_environment.json`
3. Ejecuta el login para obtener el token JWT automáticamente

### 2. Configuración Manual
```bash
# Variables de entorno
export MUSIFY_URL="https://localhost:8443"
export ACCESS_TOKEN="" # Obtenido del login
```

## 🔐 Autenticación

### Login (REST)
```bash
curl -X POST "$MUSIFY_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  },
  "message": "Login exitoso",
  "timestamp": "2024-11-16T12:00:00Z"
}
```

### Registro (REST)
```bash
curl -X POST "$MUSIFY_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

## 🎵 Endpoints de Música

### 1. Búsqueda de Música Spotify (REST)

#### Canciones Aleatorias
```bash
curl -X GET "$MUSIFY_URL/music/spotify/random?limit=10"
```

#### Búsqueda por Término
```bash
curl -X GET "$MUSIFY_URL/music/spotify/search?q=jazz&limit=20"
```

#### Datos de Reproducción
```bash
curl -X GET "$MUSIFY_URL/music/spotify/play/4uLU6hMCjMI75M1A2tKUQC"
```

**Respuesta típica:**
```json
{
  "success": true,
  "data": [
    {
      "id": "4uLU6hMCjMI75M1A2tKUQC",
      "name": "Never Gonna Give You Up",
      "artist": "Rick Astley",
      "album": "Whenever You Need Somebody",
      "duration_ms": 213573,
      "preview_url": "https://p.scdn.co/mp3-preview/...",
      "external_urls": {
        "spotify": "https://open.spotify.com/track/4uLU6hMCjMI75M1A2tKUQC"
      }
    }
  ],
  "message": "Canciones obtenidas correctamente",
  "timestamp": "2024-11-16T12:00:00Z"
}
```

### 2. Gestión de Artistas (REST)

#### Obtener Canciones de un Artista
```bash
curl -X GET "$MUSIFY_URL/api/artists/4Z8W4fKeB5YxbusRsdQVPb/tracks?page=0&size=20"
```

#### Top Tracks de un Artista
```bash
curl -X GET "$MUSIFY_URL/api/artists/4Z8W4fKeB5YxbusRsdQVPb/top-tracks"
```

**Respuesta con paginación:**
```json
{
  "success": true,
  "data": {
    "artistName": "Radiohead",
    "artistId": "4Z8W4fKeB5YxbusRsdQVPb",
    "tracks": [
      {
        "id": "15WzzleFcV4WOsCdbnKmnZ",
        "name": "Creep",
        "album": "Pablo Honey",
        "duration_ms": 238946,
        "preview_url": "https://p.scdn.co/mp3-preview/..."
      }
    ],
    "pagination": {
      "currentPage": 0,
      "pageSize": 20,
      "totalPages": 3,
      "totalElements": 50,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "message": "Se encontraron 20 canciones del artista 'Radiohead' (página 1 de 3)",
  "timestamp": "2024-11-16T12:00:00Z"
}
```

### 3. Búsqueda Categorizada (REST)

```bash
curl -X GET "$MUSIFY_URL/api/search?q=jazz&limit=5"
```

**Respuesta categorizada:**
```json
{
  "success": true,
  "data": {
    "query": "jazz",
    "songs": [
      {
        "id": "song1",
        "name": "Take Five",
        "artist": "Dave Brubeck Quartet",
        "album": "Time Out"
      }
    ],
    "albums": [
      {
        "id": "album1",
        "name": "Kind of Blue",
        "artist": "Miles Davis",
        "year": 1959
      }
    ],
    "artists": [
      {
        "id": "artist1",
        "name": "Miles Davis",
        "genres": ["jazz", "cool jazz"]
      }
    ],
    "concerts": [
      {
        "id": "concert1",
        "name": "Jazz Festival 2024",
        "date": "2024-12-15",
        "venue": "Blue Note"
      }
    ]
  },
  "message": "Búsqueda categorizada completada para 'jazz'",
  "timestamp": "2024-11-16T12:00:00Z"
}
```

## ▶️ Control del Reproductor (REST)

### Reproducir Canción
```bash
curl -X POST "$MUSIFY_URL/api/player/play?trackId=4uLU6hMCjMI75M1A2tKUQC"
```

### Controles Básicos
```bash
# Pausar
curl -X POST "$MUSIFY_URL/api/player/pause"

# Reanudar
curl -X POST "$MUSIFY_URL/api/player/resume"

# Detener
curl -X POST "$MUSIFY_URL/api/player/stop"

# Siguiente
curl -X POST "$MUSIFY_URL/api/player/next"

# Anterior
curl -X POST "$MUSIFY_URL/api/player/previous"
```

### Estado del Reproductor
```bash
curl -X GET "$MUSIFY_URL/api/player/state"
```

**Respuesta de estado:**
```json
{
  "success": true,
  "data": {
    "currentTrack": {
      "id": "4uLU6hMCjMI75M1A2tKUQC",
      "name": "Never Gonna Give You Up",
      "artist": "Rick Astley"
    },
    "isPlaying": true,
    "isPaused": false,
    "position": 45000,
    "duration": 213573,
    "volume": 0.8,
    "shuffle": false,
    "repeat": false,
    "queue": []
  },
  "message": "Estado del reproductor obtenido",
  "timestamp": "2024-11-16T12:00:00Z"
}
```

### Controles Avanzados
```bash
# Activar/Desactivar Shuffle
curl -X POST "$MUSIFY_URL/api/player/shuffle"

# Activar/Desactivar Repeat
curl -X POST "$MUSIFY_URL/api/player/repeat"
```

## 🎧 YouTube Audio (REST)

### Información de Audio
```bash
curl -X GET "$MUSIFY_URL/youtube/audio?url=https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

### Stream de Audio
```bash
curl -X GET "$MUSIFY_URL/youtube/stream?url=https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

## 🔍 Endpoints SOAP/XML

### Configuración Base
- **Content-Type**: `application/xml`
- **Namespace**: `http://tfu.com/backend/soap/music`
- **Base URL**: `http://localhost:8080/soap/music`

### 1. Búsqueda de Música (SOAP)

**Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>
```

**cURL:**
```bash
curl -X POST "http://localhost:8080/soap/music/search" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>'
```

**Respuesta:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchResponse>
    <tracks>
        <track>
            <id>track1</id>
            <name>Take Five</name>
            <artist>Dave Brubeck</artist>
            <album>Time Out</album>
            <duration>324000</duration>
            <previewUrl>https://example.com/preview1.mp3</previewUrl>
        </track>
    </tracks>
    <totalFound>1</totalFound>
    <query>jazz</query>
</searchResponse>
```

### 2. Música Aleatoria (SOAP)

**Request:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <count>5</count>
</randomRequest>
```

**cURL:**
```bash
curl -X POST "http://localhost:8080/soap/music/random" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <count>5</count>
</randomRequest>'
```

## 📊 Monitoreo y Health Checks

### Health Check
```bash
curl -X GET "$MUSIFY_URL/actuator/health"
```

### Métricas
```bash
curl -X GET "$MUSIFY_URL/actuator/metrics"
```

### Información de la Aplicación
```bash
curl -X GET "$MUSIFY_URL/actuator/info"
```

## 🔧 Códigos de Respuesta

### REST API
- **200 OK**: Operación exitosa
- **201 Created**: Recurso creado exitosamente
- **400 Bad Request**: Parámetros inválidos
- **401 Unauthorized**: Token JWT requerido o inválido
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error del servidor

### SOAP API
- **200 OK**: Respuesta XML válida
- **400 Bad Request**: XML malformado
- **500 Internal Server Error**: Error de procesamiento

## 🛡️ Patrones de Arquitectura Implementados

### Disponibilidad
- **Circuit Breaker**: Protección contra fallos de servicios externos
- **Retry**: Reintentos automáticos con backoff exponencial
- **Rate Limiting**: Limitación de velocidad en endpoints críticos
- **Health Monitoring**: Endpoints de monitoreo de salud

### Rendimiento
- **Cache-Aside**: Cache de resultados de búsqueda de Spotify
- **Async Processing**: Operaciones no bloqueantes en el reproductor

### Seguridad
- **JWT Authentication**: Autenticación basada en tokens JWT
- **Rate Limiting**: Protección contra ataques de fuerza bruta
- **HTTPS**: Comunicaciones seguras

## 🐳 Despliegue

### Docker Compose
```bash
# Iniciar todos los servicios
docker compose up --build

# Solo backend
docker compose up backend-app-1 backend-app-2

# Solo frontend
cd frontend/MusifyFront && npm start
```

### Variables de Entorno
```bash
JWT_SECRET=your-secret-key
SPOTIFY_CLIENT_ID=your-spotify-client-id
SPOTIFY_CLIENT_SECRET=your-spotify-client-secret
DB_HOST=localhost
DB_PORT=5432
DB_NAME=musify_db
```

## 📝 Scripts de Demostración

Ejecuta los scripts para probar patrones específicos:
```bash
# Probar todos los patrones
./scripts/run_all_demos.sh

# Patrones específicos
./scripts/demo_retries.sh
./scripts/demo_circuit_breaker.sh
./scripts/demo_performance.sh
./scripts/demo_security.sh
./scripts/demo_health.sh
```

## 🤝 Soporte y Contribución

Para reportar problemas o sugerir mejoras, consulta la documentación en:
- `docs/arquitectura/` - Patrones de arquitectura implementados
- `docs/database/` - Esquema y optimizaciones de base de datos
- `docs/deployment/` - Guías de despliegue y configuración

---

> **Nota**: Esta aplicación es de propósito educacional para demostrar patrones de arquitectura de software. No está destinada para uso en producción sin las medidas de seguridad adicionales apropiadas.