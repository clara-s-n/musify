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
    public ResponseEntity<String> getAudioURL(@RequestParam String name,String artist ) {
        try {
            // Busqueda de canción
            String query = String.format("ytsearch:%s %s", name, artist);
    
            ProcessBuilder pb = new ProcessBuilder(
                "yt-dlp",
                "-f", "bestaudio",
                "--get-url",
                "-q",
                "--no-warnings",
                "--no-progress",
                query
        );
        Process proc = pb.start();
        String url;
        try (BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            url = out.readLine();
        }


        int exit = proc.waitFor();
        if (exit != 0 || url == null || !url.startsWith("https")) {
            return ResponseEntity.status(502).body("Hubo un error al procesar la solicitud.");
        }
        return ResponseEntity.ok(url);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Fallo yt-dlp: " + e.getMessage());
    }

        } 
    }
    
