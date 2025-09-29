# Guía de Pruebas para la API de Musify

Esta guía te ayudará a probar los endpoints de la API de Musify usando Postman.

## Configuración Inicial

1. Importa la colección de Postman (`musify_api_collection.postman_collection.json`) a tu aplicación Postman
2. Asegúrate de que el backend esté ejecutándose con Docker Compose (`docker-compose up`)
3. La API está disponible en `http://localhost:8080`

## Flujo de Prueba Recomendado

### 1. Autenticación

1. **Registro de Usuario** (opcional si vas a usar las cuentas preexistentes)
   - Endpoint: `POST /auth/register`
   - Cuerpo: `{"username": "tu_usuario", "email": "tu@email.com", "password": "tu_password"}`

2. **Iniciar Sesión**
   - Endpoint: `POST /auth/login`
   - Cuentas predefinidas:
     - Usuario normal: `user@demo.com` / `password`
     - Administrador: `admin@demo.com` / `admin`
   - El token JWT se guardará automáticamente como variable de colección

### 2. Explorar el Catálogo

1. **Obtener todas las pistas**
   - Endpoint: `GET /tracks`
   - Requiere autenticación (token JWT)
   
2. **Buscar pistas**
   - Endpoint: `GET /tracks/search?q=pop`
   - Prueba diferentes términos de búsqueda
   
3. **Filtrar por género/artista/año**
   - Endpoint: `GET /tracks/by-genre?genre=Pop`
   - Endpoint: `GET /tracks/by-artist?artist=Ed Sheeran`
   - Endpoint: `GET /tracks/by-year?year=2023`

### 3. Probar la Reproducción

1. **Iniciar reproducción**
   - Endpoint: `POST /playback/start`
   - Cuerpo: `{"trackId": 1}`
   - Anota la URL de streaming devuelta

2. **Consultar estado de reproducción**
   - Endpoint: `GET /playback/status`
   
3. **Pausar reproducción**
   - Endpoint: `POST /playback/pause`
   
4. **Reanudar reproducción**
   - Endpoint: `POST /playback/resume`
   
5. **Detener reproducción**
   - Endpoint: `POST /playback/stop`

6. **Ver historial de reproducciones**
   - Endpoint: `GET /playback/history?page=0&size=10`

### 4. Verificar Estado del Sistema

1. **Comprobar salud del sistema**
   - Endpoint: `GET /actuator/health`
   
2. **Explorar documentación**
   - OpenAPI JSON: `GET /v3/api-docs`
   - Swagger UI: `GET /swagger-ui.html` (o visita la URL en el navegador)

## Características Importantes para Probar

1. **Tolerancia a fallos**: El servicio `flaky-service` está diseñado para fallar aleatoriamente. El backend debe manejar estas fallas correctamente usando Circuit Breaker.

2. **Balanceo de carga**: Hay dos instancias del backend detrás de Nginx. Puedes verificar los logs para ver qué instancia maneja cada petición.

3. **Seguridad**: Todos los endpoints excepto autenticación requieren token JWT válido.

## Errores Comunes y Soluciones

1. **Error 401 Unauthorized**: Asegúrate de que el token JWT sea válido y esté incluido en el header Authorization.

2. **Error 403 Forbidden**: El usuario no tiene permisos suficientes para acceder al recurso.

3. **Error 500 Internal Server Error**: Puede ocurrir al interactuar con el servicio flaky, esto es esperado para probar el Circuit Breaker.

4. **Si no funciona la API**: Verifica el estado de los contenedores con `docker-compose ps` y revisa los logs con `docker-compose logs backend-app-1`.

## Documentación Adicional

Para más detalles sobre la API, visita la interfaz Swagger UI en `http://localhost:8080/swagger-ui.html` cuando el backend esté en ejecución.