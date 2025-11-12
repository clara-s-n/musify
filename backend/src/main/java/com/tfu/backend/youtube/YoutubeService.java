package com.tfu.backend.youtube;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    }
    
