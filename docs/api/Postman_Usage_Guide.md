# Guía de Uso - Colección Postman para Musify REST API

## 📥 Archivos para Importar

Esta guía incluye dos archivos para usar con Postman:

1. **`Musify_REST_Complete_Collection.postman_collection.json`** - Colección completa con todos los endpoints REST
2. **`Musify_REST_Environment.postman_environment.json`** - Variables de entorno preconfiguradas

## 🚀 Configuración Inicial

### 1. Importar en Postman

1. Abre Postman
2. Haz clic en **"Import"** (botón superior izquierdo)
3. Arrastra y suelta ambos archivos JSON o usa **"Upload Files"**
4. Confirma la importación de ambos archivos

### 2. Seleccionar Entorno

1. En la esquina superior derecha de Postman, haz clic en el dropdown de entornos
2. Selecciona **"Musify REST API Environment"**

### 3. Verificar Variables

Las siguientes variables están preconfiguradas:
- `baseUrl`: `http://localhost:8080` (a través de NGINX)
- `baseUrlDirect`: `https://localhost:8443` (directo al backend)
- `demo_user_email`: `test@example.com`
- `demo_user_password`: `password123`
- `admin_email`: `admin@demo.com`
- `admin_password`: `admin`

## 📋 Estructura de la Colección

### 🔐 Autenticación
- **Registro de Usuario**: Crear nuevas cuentas
- **Login Usuario Demo**: Login automático con token JWT
- **Login Admin**: Login con permisos de administrador

### 🎵 Spotify Music API
- **Buscar Canciones**: Búsqueda por términos (artista, canción, álbum)
- **Música Aleatoria**: Canciones aleatorias de Spotify
- **Obtener Canción por ID**: Información detallada de canción específica

### 🎼 Tracks Management
- **CRUD Completo**: Crear, leer, actualizar, eliminar canciones
- **Requiere autenticación JWT** para operaciones de escritura

### ▶️ Playback Control
- **Control de Reproducción**: Start, pause, resume, stop
- **Estado de Reproducción**: Verificar estado actual
- **Procesamiento Asíncrono**: Todas las operaciones son no-bloqueantes

### 🔧 Monitoreo y Salud
- **Health Check**: Verificar estado de la aplicación
- **Métricas**: Información de performance y uso
- **Spring Boot Actuator**: Endpoints de monitoreo

### 📚 Documentación API
- **OpenAPI/Swagger**: Especificación de la API
- **Swagger UI**: Interfaz web para explorar endpoints

## 🎯 Flujo de Uso Recomendado

### 1. Verificar Conectividad
```
GET {{baseUrl}}/actuator/health
```
✅ Debería devolver `200 OK` con `"status": "UP"`

### 2. Autenticación
```
POST {{baseUrl}}/api/auth/login
```
- Usa **"Login Usuario Demo"** o **"Login Admin"**
- El token JWT se guarda automáticamente en `{{auth_token}}`

### 3. Probar Endpoints Públicos
```
GET {{baseUrl}}/music/spotify/search?q={{sample_search_query}}&limit={{default_limit}}
GET {{baseUrl}}/music/spotify/random?limit=5
```

### 4. Probar Endpoints Autenticados
```
GET {{baseUrl}}/api/tracks
POST {{baseUrl}}/api/playback/start
```

## 🔧 Funcionalidades Avanzadas

### Variables Automáticas
- **`{{auth_token}}`**: Se actualiza automáticamente al hacer login
- **`{{baseUrl}}`**: URL configurable para diferentes entornos
- **Samples preconfigurados**: IDs de Spotify, queries de búsqueda

### Scripts Automáticos
- **Pre-request**: Añade automáticamente el token JWT a headers Authorization
- **Post-response**: Log automático de errores comunes (401, 403, 429)
- **Token Management**: Extracción y almacenamiento automático de JWT

### Testing Automático
Cada request incluye validaciones básicas:
```javascript
// Ejemplo de test automático
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has required fields", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
});
```

## 🛠️ Casos de Uso Específicos

### Testing de Resilencia
Para probar patrones de resilencia (Circuit Breaker, Retry):

1. Usa `{{baseUrlDirect}}` para acceso directo al backend
2. Detén el servicio `flaky-service`: `docker stop flaky-service`
3. Ejecuta requests de playback para ver fallbacks en acción

### Rate Limiting
Para probar rate limiting en login:
1. Ejecuta **"Login Usuario Demo"** 6+ veces rápidamente
2. Observa respuesta `429 Too Many Requests`

### Load Balancing
Para probar balanceador NGINX:
1. Usa `{{baseUrl}}` (puerto 8080)
2. Detén un backend: `docker stop backend-app-1`
3. Requests continúan funcionando via `backend-app-2`

## 📊 Ejemplos de Responses

### Login Success
```json
{
    "success": true,
    "message": "Login exitoso",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "email": "test@example.com",
        "username": "usuario_demo",
        "roles": ["USER"]
    }
}
```

### Search Music
```json
{
    "success": true,
    "message": "Búsqueda realizada correctamente",
    "data": [
        {
            "id": "4iV5W9uYEdYUVa79Axb7Rh",
            "name": "Bohemian Rhapsody",
            "artists": "Queen",
            "album": "A Night at the Opera",
            "imageUrl": "https://i.scdn.co/image/...",
            "previewUrl": "https://p.scdn.co/mp3-preview/..."
        }
    ],
    "timestamp": "2025-11-11T18:30:00.000Z"
}
```

### Health Check
```json
{
    "status": "UP",
    "components": {
        "db": {"status": "UP"},
        "diskSpace": {"status": "UP"},
        "ping": {"status": "UP"}
    }
}
```

## 🔍 Troubleshooting

### Problemas Comunes

**401 Unauthorized**
- Token JWT expirado o inválido
- Ejecuta login nuevamente

**403 Forbidden**
- Endpoint requiere permisos específicos
- Usa login de admin si es necesario

**404 Not Found**
- Verifica que la aplicación esté ejecutándose
- Confirma la URL base en variables de entorno

**429 Too Many Requests**
- Rate limiting activo
- Espera unos minutos o cambia de usuario

**Connection Refused**
- Aplicación no ejecutándose
- Ejecuta: `docker compose up -d`

### Verificación de Estado
```bash
# Verificar contenedores
docker compose ps

# Verificar logs
docker compose logs backend-app-1

# Verificar conectividad
curl http://localhost:8080/actuator/health
```

## 🎨 Personalización

### Cambiar URLs Base
1. Ve a **Environments** en Postman
2. Edita `baseUrl` para diferentes entornos:
   - Desarrollo: `http://localhost:8080`
   - Producción: `https://api.musify.com`
   - Testing: `http://test.musify.com:8080`

### Añadir Nuevos Tests
```javascript
pm.test("Custom validation", function () {
    const response = pm.response.json();
    pm.expect(response.data).to.be.an('array');
    pm.expect(response.data.length).to.be.greaterThan(0);
});
```

### Variables Personalizadas
Añade nuevas variables en el entorno para tus casos de uso específicos.

## 📞 Soporte

Para más información consulta:
- **Documentación REST**: `docs/api/Musify_API_Testing_Guide.md`
- **Documentación SOAP**: `docs/api/SOAP_XML_API_Guide.md`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

¡Disfruta explorando la API de Musify! 🎵