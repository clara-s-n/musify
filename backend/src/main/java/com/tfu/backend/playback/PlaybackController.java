package com.tfu.backend.playback;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para gestionar la reproducción de pistas.
 * Expone endpoints para iniciar la reproducción y manejar fallbacks.
 */
@RestController
@RequestMapping("/playback")
public class PlaybackController {
  /**
   * Cliente para obtener la URL de streaming de una pista.
   */
  private final StreamClient client;

  /**
   * Constructor que inyecta el cliente de streaming.
   * 
   * @param client Cliente para obtener URLs de streaming.
   */
  public PlaybackController(StreamClient client) {
    this.client = client;
  }

  /**
   * Inicia la reproducción de una pista solicitando la URL de streaming.
   * Usa circuit breaker, retry y time limiter para tolerancia a fallos.
   * 
   * @param trackId ID de la pista a reproducir.
   * @return URL de reproducción en un DTO envuelto en ResponseEntity.
   */
  @PostMapping("/start")
  @CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
  @Retry(name = "streamSource")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<PlaybackDTO>> start(@RequestParam String trackId) {
    return CompletableFuture.supplyAsync(() -> {
      var url = client.getStreamUrl(trackId);
      return ResponseEntity.ok(new PlaybackDTO(url));
    });
  }

  /**
   * Método de fallback que retorna una URL alternativa de baja calidad si falla
   * el streaming principal.
   * 
   * @param trackId ID de la pista solicitada.
   * @param t       Excepción capturada.
   * @return URL alternativa en un DTO envuelto en ResponseEntity.
   */
  public CompletableFuture<ResponseEntity<PlaybackDTO>> fallbackUrl(String trackId, Throwable t) {
    return CompletableFuture.completedFuture(
        ResponseEntity.ok(new PlaybackDTO("https://cdn.example/low-bitrate/" + trackId)));
  }
}

/**
 * DTO que representa la URL de reproducción de una pista.
 */
record PlaybackDTO(String url) {
}
