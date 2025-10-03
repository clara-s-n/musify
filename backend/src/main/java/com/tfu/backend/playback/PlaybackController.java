package com.tfu.backend.playback;

import com.tfu.backend.common.ApiResponse;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para gestionar la reproducción de pistas.
 * Expone endpoints para iniciar, pausar, reanudar y detener la reproducción,
 * así como para consultar el historial de reproducciones.
 */
@Tag(name = "Reproducción", description = "API para control de reproducción y gestión de historial")
@RestController
@RequestMapping("/playback")
public class PlaybackController {
  private static final Logger logger = LoggerFactory.getLogger(PlaybackController.class);

  private final PlaybackService playbackService;

  /**
   * Constructor que inyecta el servicio de reproducción.
   * 
   * @param playbackService Servicio para operaciones de reproducción
   */
  public PlaybackController(PlaybackService playbackService) {
    this.playbackService = playbackService;
  }

  /**
   * Inicia la reproducción de una pista.
   * 
   * @param trackId        ID de la pista a reproducir
   * @param authentication Información de autenticación del usuario
   * @return URL de reproducción en un DTO
   */
  @Operation(summary = "Iniciar reproducción", description = "Comienza la reproducción de una pista específica", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reproducción iniciada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaybackDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al iniciar reproducción", content = @Content(mediaType = "application/json"))
  })
  @PostMapping("/start")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackDTO>>> start(
      @Parameter(description = "ID de la pista a reproducir") @RequestParam Long trackId,
      Authentication authentication) {
    logger.debug("Solicitud para iniciar reproducción de pista: {}", trackId);

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        PlaybackDTO playbackDTO = playbackService.startPlayback(trackId, userId);

        return ResponseEntity.ok(ApiResponse.success(playbackDTO, "Reproducción iniciada correctamente"));
      } catch (Exception e) {
        logger.error("Error al iniciar reproducción: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al iniciar reproducción", e.getMessage()));
      }
    });
  }

  /**
   * Pausa la reproducción actual.
   * 
   * @param authentication Información de autenticación del usuario
   * @return Mensaje de confirmación
   */
  @PostMapping("/pause")
  public ResponseEntity<ApiResponse<Void>> pause(Authentication authentication) {
    logger.debug("Solicitud para pausar reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      boolean paused = playbackService.pausePlayback(userId);

      if (paused) {
        return ResponseEntity.ok(ApiResponse.successMessage("Reproducción pausada correctamente"));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("No hay reproducción activa para pausar"));
      }
    } catch (Exception e) {
      logger.error("Error al pausar reproducción: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al pausar reproducción", e.getMessage()));
    }
  }

  /**
   * Reanuda la reproducción pausada.
   * 
   * @param authentication Información de autenticación del usuario
   * @return URL de reproducción o error si no hay reproducción pausada
   */
  @PostMapping("/resume")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackDTO>>> resume(Authentication authentication) {
    logger.debug("Solicitud para reanudar reproducción");

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        return playbackService.resumePlayback(userId)
            .map(playbackDTO -> ResponseEntity.ok(
                ApiResponse.success(playbackDTO, "Reproducción reanudada correctamente")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("No hay reproducción pausada para reanudar")));
      } catch (Exception e) {
        logger.error("Error al reanudar reproducción: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al reanudar reproducción", e.getMessage()));
      }
    });
  }

  /**
   * Detiene la reproducción actual.
   * 
   * @param authentication Información de autenticación del usuario
   * @return Mensaje de confirmación
   */
  @PostMapping("/stop")
  public ResponseEntity<ApiResponse<Void>> stop(Authentication authentication) {
    logger.debug("Solicitud para detener reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      boolean stopped = playbackService.stopPlayback(userId);

      if (stopped) {
        return ResponseEntity.ok(ApiResponse.successMessage("Reproducción detenida correctamente"));
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("No hay reproducción activa para detener"));
      }
    } catch (Exception e) {
      logger.error("Error al detener reproducción: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al detener reproducción", e.getMessage()));
    }
  }

  /**
   * Obtiene el estado actual de reproducción.
   * 
   * @param authentication Información de autenticación del usuario
   * @return Estado actual de reproducción o error si no hay reproducción activa
   */
  @Operation(summary = "Obtener estado de reproducción", description = "Devuelve el estado actual de reproducción del usuario", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado de reproducción obtenido correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaybackStatusDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No hay reproducción activa", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al obtener estado de reproducción", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/status")
  public ResponseEntity<ApiResponse<PlaybackStatusDTO>> getStatus(Authentication authentication) {
    logger.debug("Solicitud para obtener estado de reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      return playbackService.getCurrentPlaybackStatus(userId)
          .map(status -> ResponseEntity.ok(ApiResponse.success(status, "Estado de reproducción actual")))
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("No hay reproducción activa")));
    } catch (Exception e) {
      logger.error("Error al obtener estado de reproducción: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al obtener estado de reproducción", e.getMessage()));
    }
  }

  /**
   * Obtiene el historial de reproducciones del usuario.
   * 
   * @param page           Número de página (0-indexed)
   * @param size           Tamaño de página
   * @param authentication Información de autenticación del usuario
   * @return Página con el historial de reproducciones
   */
  @Operation(summary = "Obtener historial de reproducciones", description = "Devuelve el historial de reproducciones del usuario paginado", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaybackHistory.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al obtener historial", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/history")
  public ResponseEntity<ApiResponse<Page<PlaybackHistory>>> getHistory(
      @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
      Authentication authentication) {
    logger.debug("Solicitud para obtener historial de reproducciones");

    try {
      Long userId = Long.parseLong(authentication.getName());
      Page<PlaybackHistory> history = playbackService.getUserPlaybackHistory(
          userId, PageRequest.of(page, size));

      return ResponseEntity.ok(ApiResponse.success(history,
          "Historial de reproducciones - Página " + (page + 1)));
    } catch (Exception e) {
      logger.error("Error al obtener historial de reproducciones: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al obtener historial de reproducciones", e.getMessage()));
    }
  }

  /**
   * Obtiene el historial de reproducciones del usuario en una fecha específica.
   * 
   * @param date           Fecha en formato yyyy-MM-dd
   * @param authentication Información de autenticación del usuario
   * @return Lista de reproducciones en la fecha especificada
   */
  @GetMapping("/history/by-date")
  public ResponseEntity<ApiResponse<List<PlaybackHistory>>> getHistoryByDate(
      @RequestParam String date,
      Authentication authentication) {
    logger.debug("Solicitud para obtener historial de reproducciones por fecha: {}", date);

    try {
      Long userId = Long.parseLong(authentication.getName());
      LocalDate localDate = LocalDate.parse(date);
      LocalDateTime start = localDate.atStartOfDay();
      LocalDateTime end = localDate.atTime(LocalTime.MAX);

      List<PlaybackHistory> history = playbackService.getUserPlaybackHistoryInDateRange(
          userId, start, end);

      return ResponseEntity.ok(ApiResponse.success(history,
          "Historial de reproducciones del " + date));
    } catch (Exception e) {
      logger.error("Error al obtener historial de reproducciones por fecha: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Error al obtener historial de reproducciones", e.getMessage()));
    }
  }
}

/**
 * DTO que representa la URL de reproducción de una pista.
 */
@Schema(description = "URL de reproducción de una pista musical")
record PlaybackDTO(
    @Schema(description = "URL para la reproducción de la pista", example = "https://api.musify.com/stream/123456") String url) {
}
