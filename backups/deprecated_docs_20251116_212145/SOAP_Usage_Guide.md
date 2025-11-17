# Guía de Uso - Endpoints SOAP/XML de Musify

## 📋 Descripción General

Esta guía explica cómo usar y probar los endpoints SOAP/XML implementados en la aplicación Musify. Los endpoints SOAP complementan la API REST, proporcionando una interfaz alternativa basada en XML para búsqueda y descubrimiento de música.

## 🎯 Endpoints Disponibles

### 1. Búsqueda de Música (SOAP)
- **URL**: `POST /soap/music/search`
- **Content-Type**: `application/xml`
- **Descripción**: Busca canciones por término de búsqueda (artista, canción, género)

### 2. Música Aleatoria (SOAP)
- **URL**: `POST /soap/music/random`
- **Content-Type**: `application/xml`
- **Descripción**: Obtiene canciones aleatorias para descubrimiento

## 🚀 Métodos de Prueba

### Opción 1: Usar el Script de Demo (Recomendado)

El script automatizado prueba todos los endpoints con diferentes escenarios:

```bash
# Ejecutar desde la raíz del proyecto
./scripts/demo_soap_complete.sh
```

**El script demuestra:**
- ✅ Búsqueda por artista
- ✅ Búsqueda por género
- ✅ Búsqueda de canción específica
- ✅ Obtención de música aleatoria
- ✅ Validaciones (query vacío, límites)
- ✅ Manejo de errores

### Opción 2: Usar Postman

1. **Importar la Colección**:
   - Abrir Postman
   - Click en "Import"
   - Seleccionar: `docs/api/Musify_Complete_API_Collection.postman_collection.json`
   - Importar también el environment: `docs/api/Musify_REST_Environment.postman_environment.json`

2. **Seleccionar el Environment**:
   - En Postman, seleccionar "Musify Local" en el dropdown de environments
   - Verificar que `baseUrl` = `http://localhost:8080`

3. **Probar Endpoints SOAP**:
   - Navegar a la carpeta "📡 SOAP/XML Endpoints"
   - Ejecutar cualquier request
   - Ver respuestas XML formateadas

### Opción 3: Usar cURL Manual

#### Ejemplo 1: Búsqueda de Música

```bash
curl -X POST http://localhost:8080/soap/music/search \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>Billie Eilish</query>
    <limit>5</limit>
</searchMusicRequest>'
```

#### Ejemplo 2: Música Aleatoria

```bash
curl -X POST http://localhost:8080/soap/music/random \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <limit>8</limit>
</getRandomMusicRequest>'
```

#### Ejemplo 3: Con Formateo XML (usando xmllint)

```bash
curl -s -X POST http://localhost:8080/soap/music/search \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>rock</query>
    <limit>3</limit>
</searchMusicRequest>' | xmllint --format -
```

## 📝 Formato de Requests y Responses

### Request: Búsqueda de Música

```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>Billie Eilish</query>
    <limit>5</limit>
</searchMusicRequest>
```

**Parámetros:**
- `query` (obligatorio): Término de búsqueda
- `limit` (opcional): Número de resultados (1-50, default: 10)

### Response: Búsqueda Exitosa

```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchMusicResponse xmlns="http://tfu.com/backend/soap/music">
    <query>Billie Eilish</query>
    <totalResults>5</totalResults>
    <tracks>
        <track>
            <id>6Qb7YsAVEy8LvCAdGHpsCG</id>
            <name>bad guy</name>
            <artist>Billie Eilish</artist>
            <album>WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?</album>
            <duration>194080</duration>
            <explicit>false</explicit>
            <previewUrl>https://p.scdn.co/mp3-preview/...</previewUrl>
            <imageUrl>https://i.scdn.co/image/...</imageUrl>
        </track>
        <!-- más tracks... -->
    </tracks>
</searchMusicResponse>
```

### Request: Música Aleatoria

```xml
<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <limit>10</limit>
</getRandomMusicRequest>
```

**Parámetros:**
- `limit` (opcional): Número de canciones (1-50, default: 10)

### Response: Música Aleatoria Exitosa

```xml
<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicResponse xmlns="http://tfu.com/backend/soap/music">
    <totalResults>10</totalResults>
    <tracks>
        <track>
            <id>spotify_track_id</id>
            <name>Song Name</name>
            <artist>Artist Name</artist>
            <album>Album Name</album>
            <duration>180000</duration>
            <explicit>false</explicit>
            <previewUrl>https://p.scdn.co/mp3-preview/...</previewUrl>
            <imageUrl>https://i.scdn.co/image/...</imageUrl>
        </track>
        <!-- más tracks... -->
    </tracks>
</getRandomMusicResponse>
```

### Response: Error

```xml
<?xml version="1.0" encoding="UTF-8"?>
<errorResponse xmlns="http://tfu.com/backend/soap/music">
    <code>400</code>
    <message>El parámetro 'query' es obligatorio</message>
</errorResponse>
```

## ✅ Validaciones Implementadas

### Búsqueda de Música
- ❌ Query vacío → Error 400
- ❌ Límite < 1 o > 50 → Error 400
- ✅ Query válido → Lista de canciones

### Música Aleatoria
- ❌ Límite < 1 o > 50 → Error 400
- ✅ Sin parámetros → 10 canciones por defecto
- ✅ Límite válido → Número especificado de canciones

## 🎨 Casos de Uso

### 1. Búsqueda por Artista
```xml
<query>Ed Sheeran</query>
```

### 2. Búsqueda por Género
```xml
<query>rock</query>
```

### 3. Búsqueda por Canción
```xml
<query>Shape of You</query>
```

### 4. Búsqueda Combinada
```xml
<query>Shape of You Ed Sheeran</query>
```

## 🔧 Troubleshooting

### Error: "Connection refused"
**Causa**: Backend no está ejecutándose
**Solución**: 
```bash
docker compose up --build
```

### Error: "Bad Gateway 502"
**Causa**: Backend se está iniciando o una instancia está caída
**Solución**: 
- Esperar 30 segundos para que el backend termine de iniciar
- Verificar health: `curl http://localhost:8080/actuator/health`

### Error: "CORS policy"
**Causa**: Accediendo desde navegador sin proxy
**Solución**: Usar curl, Postman, o el script de demo

### Response no formateado
**Solución**: Usar xmllint para formatear:
```bash
curl ... | xmllint --format -
```

## 📊 Comparación REST vs SOAP

| Aspecto | REST | SOAP/XML |
|---------|------|----------|
| **URL** | `/music/spotify/search` | `/soap/music/search` |
| **Método** | GET | POST |
| **Formato** | JSON | XML |
| **Autenticación** | JWT Bearer Token | No requerida |
| **Query String** | Sí (`?q=...&limit=...`) | No (XML body) |
| **Validación** | Query params | XML parsing |

**Ventajas de SOAP:**
- Formato estructurado y validable (XML Schema)
- Mejor para sistemas legacy que requieren XML
- Namespace claro (`http://tfu.com/backend/soap/music`)
- Mensajes auto-documentados

**Ventajas de REST:**
- Más ligero (JSON vs XML)
- Autenticación integrada
- Cacheable (GET requests)
- Más simple para desarrollo web moderno

## 🎯 Namespace XML

Todos los endpoints SOAP usan el namespace:
```
http://tfu.com/backend/soap/music
```

Este namespace debe incluirse en todos los requests XML.

## 📚 Recursos Adicionales

- **Documentación API REST**: `docs/api/Musify_API_Testing_Guide.md`
- **Implementación SOAP**: `docs/SOAP_XML_IMPLEMENTATION_SUMMARY.md`
- **Scripts de Demo**: `scripts/demo_soap_complete.sh`
- **Colección Postman**: `docs/api/Musify_Complete_API_Collection.postman_collection.json`

## 🎓 Patrones Arquitectónicos Demostrados

Los endpoints SOAP complementan la demostración de patrones arquitectónicos:

1. **Modifiability**: API SOAP como interfaz alternativa sin cambiar lógica de negocio
2. **Interoperability**: Formato XML estándar para integración con sistemas diversos
3. **Security**: Validación de entrada en ambas capas (XML parsing + business logic)
4. **Performance**: Cache compartido con endpoints REST (SpotifyService)

## ✨ Ejemplo Completo

```bash
# 1. Verificar backend
curl http://localhost:8080/actuator/health

# 2. Buscar música
curl -X POST http://localhost:8080/soap/music/search \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<searchMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <query>Billie Eilish</query>
    <limit>5</limit>
</searchMusicRequest>' | xmllint --format -

# 3. Música aleatoria
curl -X POST http://localhost:8080/soap/music/random \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicRequest xmlns="http://tfu.com/backend/soap/music">
    <limit>3</limit>
</getRandomMusicRequest>' | xmllint --format -
```

---

**Autor**: Musify Team  
**Proyecto**: TFU Unidad 4 - Patrones Arquitectónicos  
**Fecha**: Noviembre 2025
