# 📚 Documentación de la API Musify

Esta carpeta contiene toda la documentación necesaria para usar y probar la API de Musify, incluyendo tanto endpoints REST/JSON como SOAP/XML.

## 📁 Archivos Disponibles

### 📖 Guía Principal
- **`API_COMPLETE_GUIDE.md`** - **Guía completa y unificada** con todos los endpoints REST y SOAP, ejemplos de uso y documentación consolidada

### 🔗 Colecciones de Postman
- **`Musify_Complete_JSON_Collection.postman_collection.json`** - Colección completa para endpoints JSON/REST con ejemplos de respuesta
- **`Musify_Complete_JSON_Environment.postman_environment.json`** - Variables de entorno preconfiguradas para JSON API
- **`Musify_REST_Complete_Collection.postman_collection.json`** - Colección REST completa (legacy)
- **`Musify_REST_Environment.postman_environment.json`** - Environment REST (legacy)

### 📖 Guías Específicas
- **`Postman_Usage_Guide.md`** - Guía para usar las colecciones de Postman

## 🎯 Inicio Rápido

### ⭐ Opción 1: Usar la Guía Completa (Recomendado)
1. Lee `API_COMPLETE_GUIDE.md` - Contiene todo lo que necesitas
2. Incluye ejemplos de cURL, Postman y respuestas esperadas
3. Cubre tanto REST/JSON como SOAP/XML

### 🔧 Opción 2: Usar Postman
1. Importa `Musify_Complete_JSON_Collection.postman_collection.json`
2. Importa `Musify_Complete_JSON_Environment.postman_environment.json`
3. Selecciona el environment "Musify JSON API Environment"
4. Sigue la guía en `Postman_Usage_Guide.md`

### 🌐 Opción 3: Usar Swagger UI
Visita: `http://localhost:8080/swagger-ui.html` (con la aplicación ejecutándose)

## 🌐 Endpoints Disponibles

### REST/JSON Endpoints
- **Autenticación**: `/auth/*` (register, login, logout)
- **Música Spotify**: `/music/spotify/*` (search, random, play, truly-random)
- **Gestión de Artistas**: `/api/artists/*` (tracks, top-tracks)
- **Búsqueda Categorizada**: `/api/search` (songs, albums, artists, concerts)
- **Control del Reproductor**: `/api/player/*` (play, pause, resume, stop, next, previous, state, shuffle, repeat)
- **YouTube Audio**: `/youtube/*` (audio, stream)
- **Monitoreo**: `/actuator/*` (health, metrics, info)

### SOAP/XML Endpoints
- **Búsqueda de Música**: `POST /soap/music/search`
- **Música Aleatoria**: `POST /soap/music/random`

## 🔧 Configuración de la Aplicación

### Arrancar la Aplicación
```bash
# Desde la raíz del proyecto
docker compose up --build -d

# Verificar que esté funcionando
curl http://localhost:8080/actuator/health
```

### URLs de Acceso
- **API via NGINX**: `http://localhost:8080` (recomendado)
- **Backend directo**: `https://localhost:8443` (para testing de resilencia)
- **Frontend**: `http://localhost:4200`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

## 🔐 Autenticación

La mayoría de endpoints requieren autenticación JWT. Usuarios de prueba disponibles:

```json
{
  "usuario_demo": {
    "email": "test@example.com",
    "password": "password123",
    "roles": ["USER"]
  },
  "admin": {
    "email": "admin@demo.com", 
    "password": "admin",
    "roles": ["ADMIN", "USER"]
  }
}
```

## 📊 Características de la API

### Patrones de Arquitectura Implementados
- **Availability**: Retry, Circuit Breaker, Rate Limiting, Health Monitoring
- **Performance**: Cache-Aside, Asynchronous Request-Reply
- **Security**: Gatekeeper (NGINX), Gateway Offloading, JWT Authentication
- **Modifiability**: External Configuration Store, Blue/Green Deployment

### Tecnologías
- **Backend**: Spring Boot 3, Spring Security, JWT
- **Resilencia**: Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **Cache**: Spring Cache con eviction automático
- **Base de Datos**: PostgreSQL
- **Load Balancer**: NGINX con 2 réplicas del backend
- **Containerización**: Docker + Docker Compose

## 🧪 Testing

### Scripts de Demo Automatizados
```bash
# Desde la raíz del proyecto
./scripts/demo_complete_rest_soap.sh    # Demo completo REST + SOAP
./scripts/demo_soap_xml.sh              # Demo específico SOAP/XML
./scripts/demo_retries.sh               # Demo de resilencia
./scripts/demo_performance.sh           # Demo de cache y performance
./scripts/demo_security.sh              # Demo de autenticación y rate limiting
```

### Casos de Uso de Testing
- **Resilencia**: Detener `flaky-service` para probar Circuit Breaker
- **Load Balancing**: Detener un backend para probar failover
- **Rate Limiting**: Múltiples logins rápidos para probar límites
- **Cache**: Requests repetidos para verificar cache hits

## 📋 Estructura de Responses

### Response REST/JSON Estándar
```json
{
  "success": true,
  "message": "Operación completada exitosamente",
  "data": [...],
  "timestamp": "2025-11-11T18:30:00.000Z"
}
```

### Response SOAP/XML Estándar
```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchMusicResponse xmlns="http://tfu.com/backend/soap/music">
    <success>true</success>
    <message>Búsqueda completada exitosamente</message>
    <totalResults>3</totalResults>
    <tracks>
        <track>
            <id>track_id</id>
            <name>Track Name</name>
            <artist>Artist Name</artist>
            <album>Album Name</album>
            <imageUrl>https://...</imageUrl>
        </track>
    </tracks>
</searchMusicResponse>
```

## 🔍 Troubleshooting

### Problemas Comunes

**Connection Refused**
```bash
# Verificar que la aplicación esté ejecutándose
docker compose ps
docker compose up -d
```

**401 Unauthorized**
```bash
# Token JWT expirado, hacer login nuevamente
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "test@example.com", "password": "password123"}'
```

**429 Too Many Requests**
```bash
# Rate limiting activo, esperar o usar diferente endpoint
echo "Esperando 60 segundos..."
sleep 60
```

**Empty Response**
```bash
# Verificar logs del backend
docker compose logs backend-app-1 --tail=20
```

### Verificación Rápida
```bash
# Health check
curl http://localhost:8080/actuator/health

# Test básico de búsqueda
curl "http://localhost:8080/music/spotify/search?q=test&limit=1"

# Test SOAP
curl -X POST http://localhost:8080/soap/music/search \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0"?><searchRequest><query>test</query><limit>1</limit></searchRequest>'
```

## 🎯 Casos de Uso por Rol

### Desarrollador Frontend
- Usa **Postman Collection** para testing rápido
- Consulta **Swagger UI** para especificaciones
- Implementa autenticación JWT con token automático

### QA/Testing
- Ejecuta **scripts de demo** automatizados
- Usa **cURL examples** para testing de regresión
- Prueba escenarios de resilencia y error handling

### DevOps/SRE
- Monitorea endpoints de **`/actuator/*`**
- Usa **cURL timing** para performance testing
- Verifica load balancing y health checks

### Arquitecto/Tech Lead
- Revisa **patrones implementados** en documentación
- Analiza **métricas de resilencia** via actuator
- Evalúa **coexistencia REST/SOAP**

---

## 📞 Soporte

Para preguntas específicas:
- **REST API**: Consulta `Musify_API_Testing_Guide.md`
- **SOAP API**: Consulta `SOAP_XML_API_Guide.md`
- **Postman**: Consulta `Postman_Usage_Guide.md`
- **cURL**: Consulta `cURL_Examples_Guide.md`
- **Implementación**: Consulta `../SOAP_XML_IMPLEMENTATION_SUMMARY.md`

¡Happy testing! 🎵🚀