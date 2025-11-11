package com.tfu.backend.player;

import com.tfu.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controlador para funcionalidades avanzadas del reproductor
 */
@Tag(name = "Player", description = "API para control avanzado del reproductor con cola, navegación y autoplay")
@RestController
@RequestMapping("/api/player")
public class PlayerController {
    
    private final PlayerService playerService;
    
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    @Operation(summary = "Reproducir canción", description = "Inicia la reproducción de una canción específica")
    @PostMapping("/play")
    public CompletableFuture<ResponseEntity<ApiResponse<PlayerState>>> play(
        @Parameter(description = "ID de la canción a reproducir", required = true)
        @RequestParam String trackId
    ) {
        return playerService.play(trackId)
            .thenApply(state -> ResponseEntity.ok(
                ApiResponse.success(state, "Reproducción iniciada")
            ));
    }
    
    @Operation(summary = "Siguiente canción", description = "Reproduce la siguiente canción en la cola")
    @PostMapping("/next")
    public CompletableFuture<ResponseEntity<ApiResponse<PlayerState>>> playNext() {
        return playerService.playNext()
            .thenApply(state -> ResponseEntity.ok(
                ApiResponse.success(state, "Reproduciendo siguiente canción")
            ));
    }
    
    @Operation(summary = "Canción anterior", description = "Reproduce la canción anterior en la cola")
    @PostMapping("/previous")
    public CompletableFuture<ResponseEntity<ApiResponse<PlayerState>>> playPrevious() {
        return playerService.playPrevious()
            .thenApply(state -> ResponseEntity.ok(
                ApiResponse.success(state, "Reproduciendo canción anterior")
            ));
    }
    
    @Operation(summary = "Pausar", description = "Pausa la reproducción actual")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<PlayerState>> pause() {
        PlayerState state = playerService.pause();
        return ResponseEntity.ok(ApiResponse.success(state, "Reproducción pausada"));
    }
    
    @Operation(summary = "Reanudar", description = "Reanuda la reproducción pausada")
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<PlayerState>> resume() {
        PlayerState state = playerService.resume();
        return ResponseEntity.ok(ApiResponse.success(state, "Reproducción reanudada"));
    }
    
    @Operation(summary = "Detener", description = "Detiene la reproducción")
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<PlayerState>> stop() {
        PlayerState state = playerService.stop();
        return ResponseEntity.ok(ApiResponse.success(state, "Reproducción detenida"));
    }
    
    @Operation(summary = "Estado del reproductor", description = "Obtiene el estado actual del reproductor")
    @GetMapping("/state")
    public ResponseEntity<ApiResponse<PlayerState>> getState() {
        PlayerState state = playerService.getState();
        return ResponseEntity.ok(ApiResponse.success(state, "Estado del reproductor obtenido"));
    }
    
    @Operation(summary = "Toggle Shuffle", description = "Activa o desactiva el modo shuffle")
    @PostMapping("/shuffle")
    public ResponseEntity<ApiResponse<PlayerState>> toggleShuffle() {
        PlayerState state = playerService.toggleShuffle();
        return ResponseEntity.ok(ApiResponse.success(state, 
            "Shuffle " + (state.isShuffle() ? "activado" : "desactivado")));
    }
    
    @Operation(summary = "Toggle Repeat", description = "Activa o desactiva el modo repeat")
    @PostMapping("/repeat")
    public ResponseEntity<ApiResponse<PlayerState>> toggleRepeat() {
        PlayerState state = playerService.toggleRepeat();
        return ResponseEntity.ok(ApiResponse.success(state, 
            "Repeat " + (state.isRepeat() ? "activado" : "desactivado")));
    }
}