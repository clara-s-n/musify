# Guía de API SOAP/XML para Musify

Esta guía describe los endpoints SOAP/XML implementados en la aplicación Musify para complementar la API REST existente.

## Información General

- **Base URL:** `http://localhost:8080/soap/music`
- **Protocolo:** HTTP POST
- **Content-Type:** `application/xml`
- **Formato:** XML con namespace `http://tfu.com/backend/soap/music`

## Endpoints Disponibles

### 1. Búsqueda de Música

**Endpoint:** `POST /soap/music/search`

**Descripción:** Busca canciones en Spotify basado en una consulta de texto.

**Request XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>
```

**Response XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<searchMusicResponse xmlns="http://tfu.com/backend/soap/music">
    <success>true</success>
    <message>Búsqueda completada exitosamente para 'jazz'</message>
    <totalResults>3</totalResults>
    <tracks>
        <track>
            <id>3HYrE28e4ePCIBLGcEuHD5</id>
            <name>Fue Amor</name>
            <artist>Jazzy Mel</artist>
            <album>MC Rapper</album>
            <imageUrl>https://i.scdn.co/image/ab67616d0000b273128570430b11fc800f35eff3</imageUrl>
        </track>
        <track>
            <id>06SYfhYSo9iY3txHE6W9Dl</id>
            <name>jazz is for ordinary people</name>
            <artist>berlioz</artist>
            <album>jazz is for ordinary people</album>
            <imageUrl>https://i.scdn.co/image/ab67616d0000b273c9515263a9b137c148813fd5</imageUrl>
        </track>
        <track>
            <id>5T8EDUDqKcs6OSOwEsfqG7</id>
            <name>Don't Stop Me Now - Remastered 2011</name>
            <artist>Queen</artist>
            <album>Jazz (2011 Remaster)</album>
            <imageUrl>https://i.scdn.co/image/ab67616d0000b2737c39dd133836c2c1c87e34d6</imageUrl>
        </track>
    </tracks>
</searchMusicResponse>
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/soap/music/search \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>'
```

### 2. Música Aleatoria

**Endpoint:** `POST /soap/music/random`

**Descripción:** Obtiene canciones aleatorias de Spotify.

**Request XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <limit>2</limit>
</randomRequest>
```

**Response XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<getRandomMusicResponse xmlns="http://tfu.com/backend/soap/music">
    <success>true</success>
    <message>Obtenidas 2 canciones aleatorias exitosamente</message>
    <totalResults>2</totalResults>
    <tracks>
        <track>
            <id>1NDJk94691Vydq1IrIeGC0</id>
            <name>PAPOTA</name>
            <artist>CA7RIEL &amp; Paco Amoroso, CA7RIEL, Paco Amoroso</artist>
            <album>PAPOTA</album>
            <imageUrl>https://i.scdn.co/image/ab67616d00001e024567b2180fab2ab9dcbb87b</imageUrl>
        </track>
        <track>
            <id>3S9l1Zky8ysxa8dEI1Hy1E</id>
            <name>VERSUS</name>
            <artist>Paulo Londra</artist>
            <album>VERSUS</album>
            <imageUrl>https://i.scdn.co/image/ab67616d00001e02caa92c50de9d48d5f8e38557</imageUrl>
        </track>
    </tracks>
</getRandomMusicResponse>
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/soap/music/random \
-H "Content-Type: application/xml" \
-d '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <limit>2</limit>
</randomRequest>'
```

## Estructura de Datos

### Track Element
- **id:** Identificador único de Spotify
- **name:** Nombre de la canción
- **artist:** Nombre del/los artista(s)
- **album:** Nombre del álbum
- **imageUrl:** URL de la imagen de portada del álbum

### Response Elements
- **success:** Booleano indicando si la operación fue exitosa
- **message:** Mensaje descriptivo del resultado
- **totalResults:** Número total de resultados devueltos
- **tracks:** Lista de canciones encontradas

## Parámetros de Request

### searchRequest
- **query:** (String, requerido) Término de búsqueda
- **limit:** (Integer, opcional, default=10) Número máximo de resultados

### randomRequest
- **limit:** (Integer, opcional, default=10) Número máximo de canciones aleatorias

## Códigos de Estado HTTP

- **200 OK:** Operación exitosa
- **400 Bad Request:** XML malformado o parámetros inválidos
- **500 Internal Server Error:** Error interno del servidor

## Diferencias con la API REST

Los endpoints SOAP/XML complementan la API REST existente con las siguientes características:

1. **Formato de datos:** XML vs JSON
2. **Namespace:** Uso de namespace XML para estructurar datos
3. **Protocolo:** Similar comportamiento pero diferentes formatos de entrada/salida
4. **Acceso público:** No requieren autenticación (igual que algunos endpoints REST)

## Testing con Postman

1. Crear una nueva request POST
2. Configurar URL: `http://localhost:8080/soap/music/search` o `/soap/music/random`
3. Añadir header: `Content-Type: application/xml`
4. En el body, seleccionar "raw" y pegar el XML de ejemplo
5. Enviar la request

## Notas de Implementación

- Los endpoints SOAP están implementados usando Spring MVC con manejo manual de XML
- No se utiliza framework SOAP completo (como Spring Web Services) para simplicidad
- Los datos provienen del mismo servicio de Spotify que usa la API REST
- Aplicación de patrones de resilencia (Circuit Breaker, Retry, Cache) igual que en REST
- Load balancing a través de NGINX igual que otros endpoints

## Validación de Schema

El sistema no valida contra un XSD específico, pero espera la estructura XML mostrada en los ejemplos. Asegúrate de:

1. Incluir la declaración XML
2. Usar los nombres de elementos correctos
3. Proporcionar valores válidos para query y limit
4. Mantener la estructura jerárquica correcta