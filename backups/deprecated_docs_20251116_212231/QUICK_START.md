# 🚀 Guía de Inicio Rápido - API Musify

> **Para desarrolladores**: Todo lo que necesitas para empezar a usar la API de Musify

## ⚡ TL;DR - Empezar en 2 minutos

```bash
# 1. Iniciar la aplicación
docker compose up --build

# 2. Obtener token JWT
curl -X POST "https://localhost:8443/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}' \
  -k

# 3. Usar el token en requests (reemplaza YOUR_TOKEN)
curl -X GET "https://localhost:8443/music/spotify/random?limit=5" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -k
```

## 📚 Documentación Completa

### 🎯 Guía Principal
**[📖 API_COMPLETE_GUIDE.md](docs/api/API_COMPLETE_GUIDE.md)** - Guía completa con todos los endpoints, ejemplos y respuestas

### 🔧 Herramientas
- **Postman**: Importa `docs/api/Musify_Complete_JSON_Collection.postman_collection.json`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

## 🎵 Endpoints Más Usados

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/auth/login` | POST | Obtener token JWT |
| `/music/spotify/search` | GET | Buscar música |
| `/music/spotify/random` | GET | Música aleatoria |
| `/api/player/play` | POST | Reproducir canción |
| `/api/player/state` | GET | Estado del reproductor |

## 🔐 Autenticación Rápida

### Cuentas de Prueba
- **Usuario**: `test@example.com` / `password123`
- **Admin**: `admin@demo.com` / `admin`

### Token JWT
```bash
# Login y extraer token
response=$(curl -s -X POST "https://localhost:8443/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}' \
  -k)

token=$(echo $response | jq -r '.data.accessToken')
export MUSIFY_TOKEN=$token
```

## 🎧 Ejemplos Rápidos

### Buscar Música
```bash
curl -X GET "https://localhost:8443/music/spotify/search?q=jazz&limit=5" \
  -H "Authorization: Bearer $MUSIFY_TOKEN" \
  -k
```

### Control del Reproductor
```bash
# Reproducir
curl -X POST "https://localhost:8443/api/player/play?trackId=4uLU6hMCjMI75M1A2tKUQC" \
  -H "Authorization: Bearer $MUSIFY_TOKEN" \
  -k

# Ver estado
curl -X GET "https://localhost:8443/api/player/state" \
  -H "Authorization: Bearer $MUSIFY_TOKEN" \
  -k
```

## 🐳 Docker Setup

```bash
# Iniciar todo
docker compose up --build

# Solo backend (2 replicas)
docker compose up backend-app-1 backend-app-2

# Verificar salud
curl -k https://localhost:8443/actuator/health
```

## 🔍 Debugging

### Logs
```bash
# Backend logs
docker compose logs backend-app-1

# Ver todas las URLs disponibles
curl -k https://localhost:8443/actuator/mappings
```

### Health Checks
```bash
# Salud general
curl -k https://localhost:8443/actuator/health

# Métricas
curl -k https://localhost:8443/actuator/metrics
```

---

> 💡 **Tip**: Para una experiencia completa, importa la colección de Postman y usa el environment preconfigurado.

📚 **Documentación completa**: [docs/api/API_COMPLETE_GUIDE.md](docs/api/API_COMPLETE_GUIDE.md)