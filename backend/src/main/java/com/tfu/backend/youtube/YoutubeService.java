package com.tfu.backend.youtube;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@RestController
@RequestMapping("/api/youtube")
public class YoutubeService {

    // endpoint para conseguir audio de youtube
    @GetMapping("/audio")
    public ResponseEntity<String> getAudioURL(@RequestParam String name, @RequestParam String artist) {
        try {
            // Crear query más específica para audio/música
            String query = String.format("ytsearch5:\"%s\" \"%s\" audio OR music OR album OR song", name, artist);
    
            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-f", "bestaudio[ext=webm]/bestaudio[ext=m4a]/bestaudio", // Preferir formatos web
                "--get-url",
                "-q",
                "--no-warnings", 
                "--no-progress",
                "--prefer-free-formats", // Preferir formatos libres
                "--audio-quality", "0", // Mejor calidad de audio
                query
        );
        Process proc = pb.start();
        String url;
        try (BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            url = out.readLine();
        }

        int exit = proc.waitFor();
        if (exit != 0 || url == null || !url.startsWith("https")) {
            // Si falla la búsqueda específica, intentar búsqueda más simple
            return trySimpleSearch(name, artist);
        }
        return ResponseEntity.ok(url);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Fallo yt-dlp: " + e.getMessage());
    }
    }
    
    // Método de fallback con búsqueda más simple
    private ResponseEntity<String> trySimpleSearch(String name, String artist) {
        try {
            String simpleQuery = String.format("ytsearch1:%s %s", name, artist);
            
            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-f", "bestaudio",
                "--get-url",
                "-q",
                "--no-warnings",
                "--no-progress",
                simpleQuery
            );
            Process proc = pb.start();
            String url;
            try (BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                url = out.readLine();
            }

            int exit = proc.waitFor();
            if (exit != 0 || url == null || !url.startsWith("https")) {
                return ResponseEntity.status(502).body("No se pudo encontrar audio para la canción solicitada.");
            }
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en búsqueda de fallback: " + e.getMessage());
        }
    } 

    /**
     * Proxy endpoint that streams YouTube audio through the backend
     * This avoids browser tracking prevention issues with googlevideo.com URLs
     */
    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamAudio(
            @RequestParam String name, 
            @RequestParam String artist) {
        try {
            // First get the audio URL from yt-dlp
            String query = String.format("ytsearch1:\"%s\" \"%s\"", name, artist);
            
            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-f", "bestaudio[ext=webm]/bestaudio[ext=m4a]/bestaudio",
                "--get-url",
                "-q",
                "--no-warnings",
                "--no-progress",
                query
            );
            Process proc = pb.start();
            String audioUrl;
            try (BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                audioUrl = out.readLine();
            }

            int exit = proc.waitFor();
            if (exit != 0 || audioUrl == null || !audioUrl.startsWith("https")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Now stream the audio from YouTube through our backend
            URL url = new URL(audioUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            // Set appropriate headers
            HttpHeaders headers = new HttpHeaders();
            String contentType = connection.getContentType();
            if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                headers.setContentLength(contentLength);
            }
            
            // Enable range requests for seeking
            headers.set("Accept-Ranges", "bytes");
            headers.set("Cache-Control", "no-cache");

            // Stream the content
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                } catch (Exception e) {
                    System.err.println("Error streaming audio: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);

        } catch (Exception e) {
            System.err.println("Error in stream endpoint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
