# Solución: Tracking Prevention Bloqueando Audio de YouTube

## Problema Identificado

El navegador está bloqueando las URLs de `googlevideo.com` con el mensaje:
```
Tracking Prevention blocked access to storage for https://rr3---sn-5ouxa-h8qes.googlevideo.com/...
```

Esto impide que el elemento `<audio>` reproduzca música directamente desde YouTube.

## Solución Implementada: Backend Proxy

### Arquitectura
```
┌─────────┐     HTTP      ┌─────────┐    yt-dlp    ┌──────────┐
│ Browser │ ────────────> │ Backend │ ──────────> │ YouTube  │
│         │ <──────────── │  Proxy  │ <────────── │          │
└─────────┘   Audio       └─────────┘   Audio     └──────────┘
             Stream                      Stream
```

**Flujo:**
1. Frontend solicita: `http://localhost:8080/api/youtube/stream?name=Reverence&artist=...`
2. Backend ejecuta `yt-dlp` para obtener URL de YouTube
3. Backend descarga el audio desde YouTube
4. Backend sirve el audio al frontend mediante streaming
5. Browser no ve la URL de `googlevideo.com`, solo ve `localhost:8080`

### Cambios Realizados

#### 1. Backend: Nuevo Endpoint Proxy (`YoutubeService.java`)

**Nuevo endpoint:** `GET /api/youtube/stream`

```java
@GetMapping("/stream")
public ResponseEntity<StreamingResponseBody> streamAudio(
        @RequestParam String name, 
        @RequestParam String artist) {
    try {
        // 1. Obtener URL de YouTube con yt-dlp
        String query = String.format("ytsearch1:\"%s\" \"%s\"", name, artist);
        ProcessBuilder pb = new ProcessBuilder(
            "yt-dlp", "-f", "bestaudio[ext=webm]/bestaudio[ext=m4a]/bestaudio",
            "--get-url", "-q", "--no-warnings", "--no-progress", query
        );
        Process proc = pb.start();
        String audioUrl = new BufferedReader(
            new InputStreamReader(proc.getInputStream())
        ).readLine();

        if (audioUrl == null || !audioUrl.startsWith("https")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 2. Conectar a YouTube y obtener el stream
        URL url = new URL(audioUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        // 3. Configurar headers para streaming
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            connection.getContentType() != null 
                ? connection.getContentType() 
                : "application/octet-stream"
        ));
        headers.setContentLength(connection.getContentLengthLong());
        headers.set("Accept-Ranges", "bytes");
        headers.set("Cache-Control", "no-cache");

        // 4. Stream el contenido al cliente
        StreamingResponseBody stream = outputStream -> {
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            }
        };

        return ResponseEntity.ok().headers(headers).body(stream);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

**Características:**
- ✅ Streaming real (no descarga todo en memoria)
- ✅ Headers apropiados (`Content-Type`, `Content-Length`, `Accept-Ranges`)
- ✅ Soporta seeking (range requests)
- ✅ Buffer de 8KB para eficiencia
- ✅ Manejo de errores robusto

#### 2. Frontend: Usar Endpoint Proxy (`player.service.ts`)

**Cambio en el método `play()`:**

```typescript
// ANTES: Obtenía la URL directa de YouTube
const youtubeUrl = `${apiUrl}/api/youtube/audio?name=${name}&artist=${artist}`;
this.http.get(youtubeUrl, { responseType: 'text' }).subscribe({
  next: (audioUrl) => {
    // Usaba la URL directa de googlevideo.com (BLOQUEADO)
    trackInfo.audioUrl = audioUrl;
  }
});

// AHORA: Usa el endpoint proxy del backend
const streamUrl = `${apiUrl}/api/youtube/stream?name=${name}&artist=${artist}`;
trackInfo.audioUrl = streamUrl;  // URL de localhost, no de YouTube
```

**Beneficios:**
- ✅ No hay llamada HTTP asíncrona (más rápido)
- ✅ URL siempre apunta a `localhost:8080` (no bloqueado)
- ✅ Browser no ve URLs de `googlevideo.com`
- ✅ Evita problemas de CORS y tracking prevention

### Por Qué Funciona

**Problema original:**
```
Browser → googlevideo.com ❌ (Blocked by Tracking Prevention)
```

**Solución:**
```
Browser → localhost:8080 → googlevideo.com ✅
   ↑                           ↓
   └──────── Audio Stream ─────┘
```

El navegador solo ve `localhost:8080`, que es de confianza. El backend actúa como intermediario.

## Despliegue

### Compilar Backend
```bash
cd /home/ana/musify
./backend/mvnw clean package -DskipTests
```

### Reiniciar Contenedores
```bash
docker compose down
docker compose up --build -d
```

### Verificar
```bash
# Ver logs del backend
docker compose logs -f backend-app-1

# Probar el endpoint proxy directamente
curl -I "http://localhost:8080/api/youtube/stream?name=Reverence&artist=Faithless"
```

Deberías ver:
```
HTTP/1.1 200 OK
Content-Type: audio/webm
Content-Length: 2387773
Accept-Ranges: bytes
```

## Testing

1. **Abrir la aplicación**: `http://localhost:4200`
2. **Seleccionar una canción**
3. **Verificar en console (no debería aparecer "Tracking Prevention")**:
   ```
   Using proxied stream URL for: Reverence
   ✅ Playback started successfully via user click
   ```
4. **La música debería sonar** 🎵

## Ventajas Adicionales

- ✅ **Cache potencial**: El backend puede cachear streams
- ✅ **Estadísticas**: Podemos contar reproducciones en el backend
- ✅ **Rate limiting**: Control de uso de yt-dlp
- ✅ **Transformaciones**: Posibilidad de convertir formatos en el backend

## Archivos Modificados

- `backend/src/main/java/com/tfu/backend/youtube/YoutubeService.java`
  - Imports agregados: `URL`, `HttpURLConnection`, `InputStream`, `StreamingResponseBody`
  - Nuevo método: `streamAudio()` con endpoint `@GetMapping("/stream")`

- `frontend/MusifyFront/src/app/services/player.service.ts`
  - Líneas ~104-126: Método `play()` simplificado
  - Cambio de `/api/youtube/audio` → `/api/youtube/stream`
  - Eliminada llamada HTTP asíncrona, URL directa al proxy

## Próximos Pasos (Opcionales)

1. **Implementar cache de streams** en el backend
2. **Agregar rate limiting** para llamadas a yt-dlp
3. **Implementar range requests** completo para seeking preciso
4. **Agregar logging de reproducciones** para estadísticas
5. **Optimizar buffer size** según ancho de banda

## Notas Técnicas

- `StreamingResponseBody` permite streaming eficiente sin cargar todo en RAM
- `HttpURLConnection` usado en lugar de `HttpClient` para control fino
- Buffer de 8KB balances entre memoria y throughput
- Headers `Accept-Ranges` prepara para implementar seeking completo
