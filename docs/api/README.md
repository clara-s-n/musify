# 📚 API de Musify

> **Documentación completa y herramientas para la API REST y SOAP de Musify**

## 📁 Archivos

- **`API_GUIDE.md`** - 📖 Guía completa con todos los endpoints REST y SOAP
- **`Musify_API.postman_collection.json`** - 🔧 Colección de Postman con todos los endpoints
- **`Musify_API.postman_environment.json`** - ⚙️ Variables de entorno para Postman

## 🚀 Inicio Rápido

### 1. Leer la Documentación
Abre `API_GUIDE.md` - contiene todo lo que necesitas saber sobre la API.

### 2. Usar Postman
1. Importa `Musify_API.postman_collection.json`
2. Importa `Musify_API.postman_environment.json`
3. Selecciona el environment "Musify JSON API Environment"
4. Ejecuta el login para obtener el token automáticamente

### 3. Alternativamente usar Swagger
Visita: `http://localhost:8080/swagger-ui.html`

## 🎯 Endpoints Principales

- **Autenticación**: `/auth/*`
- **Música Spotify**: `/music/spotify/*`
- **Control Reproductor**: `/api/player/*`
- **Búsqueda**: `/api/search`
- **SOAP**: `/soap/music/*`

---

📖 **Documentación completa**: [API_GUIDE.md](API_GUIDE.md)