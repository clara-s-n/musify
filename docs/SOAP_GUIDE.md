# 📡 SOAP/XML API - Guía Completa

> **Endpoints SOAP/XML para Musify - Implementación, uso y configuración completa**

## 🎯 Información General

La API SOAP/XML de Musify complementa la API REST existente, proporcionando las mismas funcionalidades de búsqueda y descubrimiento de música usando protocolo SOAP con formato XML.

### Características
- **Framework**: Spring MVC con @RestController
- **Procesamiento XML**: Manejo manual de XML (sin JAXB/Spring Web Services)
- **Servicios Backend**: Reutilización del SpotifyService existente
- **Seguridad**: Configuración actualizada para permitir acceso público a `/soap/**`
- **Namespace**: `http://tfu.com/backend/soap/music`

## 🔗 Endpoints Disponibles

### 1. Búsqueda de Música
- **URL**: `POST /soap/music/search`
- **Content-Type**: `application/xml`
- **Descripción**: Busca canciones en Spotify usando XML

### 2. Música Aleatoria
- **URL**: `POST /soap/music/random`  
- **Content-Type**: `application/xml`
- **Descripción**: Obtiene canciones aleatorias usando XML

## 🚀 Cómo Usar

### Opción 1: Script Automatizado (Recomendado) ⚡

```bash
# Desde la raíz del proyecto
./scripts/demo_soap_complete.sh
```

**El script prueba:**
- ✅ Búsqueda por artista (Billie Eilish)
- ✅ Búsqueda por género (rock)
- ✅ Búsqueda de canción específica (Shape of You)
- ✅ Música aleatoria (8 canciones)
- ✅ Validación de query vacío (debe fallar)
- ✅ Validación de límite fuera de rango (debe fallar)

### Opción 2: Postman 🎨

1. Importar colección: `docs/api/Musify_API.postman_collection.json`
2. Importar environment: `docs/api/Musify_API.postman_environment.json`
3. Ir a la carpeta **"SOAP/XML Endpoints"**
4. Ejecutar cualquier request

### Opción 3: cURL Manual 💻

#### Búsqueda de Música

**Request:**
```bash
curl -X POST "http://localhost:8080/soap/music/search" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>'
```

**Response:**
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

#### Música Aleatoria

**Request:**
```bash
curl -X POST "http://localhost:8080/soap/music/random" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<randomRequest>
    <count>5</count>
</randomRequest>'
```

**Response:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<randomResponse>
    <tracks>
        <track>
            <id>5SQnZzUb1W1JGye7fVaBoP</id>
            <name>Viva La Vida</name>
            <artist>Coldplay</artist>
            <album>Viva La Vida or Death and All His Friends</album>
            <duration>242000</duration>
            <previewUrl>https://p.scdn.co/mp3-preview/...</previewUrl>
        </track>
    </tracks>
    <totalReturned>1</totalReturned>
</randomResponse>
```

## 🛠️ Implementación Técnica

### Arquitectura de Diseño

#### Enfoque Técnico
- **Framework**: Spring MVC con @RestController
- **Procesamiento XML**: Manejo manual de XML (sin JAXB/Spring Web Services)
- **Servicios Backend**: Reutilización del SpotifyService existente
- **Seguridad**: Configuración actualizada para permitir acceso público a `/soap/**`

#### Decisiones de Diseño

1. **Simplicidad sobre Complejidad**:
   - Manejo manual de XML en lugar de frameworks SOAP completos
   - Evita problemas de compatibilidad javax.xml vs jakarta.xml
   - Facilita mantenimiento y debugging

2. **Reutilización de Servicios**:
   - Los endpoints SOAP utilizan el mismo SpotifyService que REST
   - Mismos patrones de resilencia (Circuit Breaker, Retry, Cache)
   - Consistencia en los datos devueltos

3. **Namespace XML**:
   - Uso de namespace propio: `http://tfu.com/backend/soap/music`
   - Estructura XML consistente y validable

### Estructura de Archivos

```
backend/src/main/java/com/tfu/backend/
├── soap/
│   └── SoapMusicController.java          # Controlador SOAP principal
├── spotify/
│   └── SpotifyService.java               # Servicio reutilizado
└── config/
    └── SecurityConfig.java               # Configuración de seguridad actualizada
```

### Código del Controlador

```java
@RestController
@RequestMapping("/soap/music")
public class SoapMusicController {
    
    private final SpotifyService spotifyService;
    
    @PostMapping(value = "/search", 
                 consumes = "application/xml", 
                 produces = "application/xml")
    public ResponseEntity<String> searchMusic(@RequestBody String xmlRequest) {
        // Procesamiento manual de XML
        // Reutilización de SpotifyService
        // Construcción de respuesta XML
    }
    
    @PostMapping(value = "/random", 
                 consumes = "application/xml", 
                 produces = "application/xml")
    public ResponseEntity<String> getRandomMusic(@RequestBody String xmlRequest) {
        // Procesamiento manual de XML
        // Reutilización de SpotifyService
        // Construcción de respuesta XML
    }
}
```

### Configuración de Seguridad

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/soap/**").permitAll()  // Acceso público a SOAP
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
```

## ✅ Validación y Testing

### Casos de Prueba Automáticos

El script `demo_soap_complete.sh` incluye los siguientes casos de prueba:

#### Casos Exitosos
1. **Búsqueda por artista**: "Billie Eilish"
2. **Búsqueda por género**: "rock"
3. **Búsqueda específica**: "Shape of You"
4. **Música aleatoria**: 8 canciones

#### Casos de Error (Validación)
1. **Query vacío**: Debe retornar error 400
2. **Límite fuera de rango**: Debe retornar error 400

### Respuestas de Error

```xml
<?xml version="1.0" encoding="UTF-8"?>
<errorResponse>
    <error>
        <code>400</code>
        <message>Query parameter is required</message>
    </error>
</errorResponse>
```

### Logs de Debugging

```bash
# Logs exitosos
"SOAP Search request for query: jazz, limit: 3"
"SOAP Random request for count: 5"

# Logs de error
"SOAP Search failed: Query parameter is empty"
"SOAP Random failed: Invalid count parameter"
```

## 🔄 Integración con Patrones Existentes

### Resilience4j (Circuit Breaker, Retry)
Los endpoints SOAP heredan automáticamente los patrones de resilencia:

```java
// En SpotifyService.java (reutilizado por SOAP)
@Retry(name = "spotifyApi")
@CircuitBreaker(name = "spotifyApi", fallbackMethod = "fallbackSearchTracks")
public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    // Implementación con reintentos y circuit breaker
}
```

### Cache-Aside
Los resultados SOAP también aprovechan el cache existente:

```java
@Cacheable(value = "searchTracks", key = "#query + ':' + #limit")
public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    // Los requests SOAP también se benefician del cache
}
```

## 📊 Métricas y Monitoreo

### Endpoints de Actuator
Los endpoints SOAP se monitorean junto con REST:

- `/actuator/metrics` - Incluye métricas de endpoints SOAP
- `/actuator/health` - Estado general del sistema
- `/actuator/circuitbreakers` - Estado de circuit breakers (compartido)

### Métricas Específicas

```bash
# Requests SOAP totales
curl http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/soap/music/search

# Latencia SOAP
curl http://localhost:8080/actuator/metrics/http.server.requests?tag=method:POST&tag=uri:/soap/music/search

# Circuit Breaker estado (compartido con REST)
curl http://localhost:8080/actuator/circuitbreakers
```

## 🚀 Demo Completo

### Ejecutar Demo Automatizada

```bash
# Iniciar sistema
docker compose up --build

# Esperar a que esté listo
curl http://localhost:8080/actuator/health

# Ejecutar demo SOAP
./scripts/demo_soap_complete.sh
```

### Salida Esperada del Demo

```
╔══════════════════════════════════════════════════════════════╗
║                    DEMO SOAP/XML - MUSIFY                   ║
╚══════════════════════════════════════════════════════════════╝

✅ Sistema verificado - Iniciando demos SOAP/XML

📡 SOAP: Búsqueda de Música por Artista
Request: <searchRequest><query>Billie Eilish</query><limit>3</limit></searchRequest>
✅ SUCCESS: 3 canciones encontradas

📡 SOAP: Búsqueda por Género
Request: <searchRequest><query>rock</query><limit>5</limit></searchRequest>
✅ SUCCESS: 5 canciones rock encontradas

📡 SOAP: Música Aleatoria
Request: <randomRequest><count>8</count></randomRequest>
✅ SUCCESS: 8 canciones aleatorias obtenidas

✅ TODOS LOS TESTS SOAP PASARON EXITOSAMENTE
```

## 🔧 Troubleshooting

### Problemas Comunes

#### Error: "No se puede conectar a /soap/music/search"
```bash
# Verificar que el backend esté ejecutándose
curl http://localhost:8080/actuator/health

# Verificar logs del backend
docker compose logs backend-app-1
```

#### Error: "XML malformado"
```bash
# Verificar que el Content-Type sea correcto
curl -H "Content-Type: application/xml" ...

# Verificar que el XML esté bien formado
<?xml version="1.0" encoding="UTF-8"?>
<searchRequest>
    <query>jazz</query>
    <limit>3</limit>
</searchRequest>
```

#### Error: "Service Unavailable"
```bash
# Verificar que Spotify API funcione
curl http://localhost:8080/music/spotify/random?limit=1

# Verificar configuración de Circuit Breaker
curl http://localhost:8080/actuator/circuitbreakers
```

## 📚 Referencias

- **Spring MVC**: https://docs.spring.io/spring-framework/docs/current/reference/html/web.html
- **XML Processing**: https://docs.oracle.com/javase/tutorial/jaxp/
- **Resilience4j**: https://resilience4j.readme.io/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

---

> **Nota**: Los endpoints SOAP complementan la API REST sin reemplazarla. Ambas interfaces pueden utilizarse simultáneamente y comparten la misma lógica de negocio y patrones de resilencia.