package com.tfu.backend.playback;

import com.tfu.backend.common.ApiResponse;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar la reproducción de pistas.
 */
@Tag(name = "Reproducción", description = "API para control de reproducción y gestión de historial")
@RestController
@RequestMapping("/playback")
public class PlaybackController {

  private static final Logger logger = LoggerFactory.getLogger(PlaybackController.class);

  private final PlaybackService playbackService;

  public PlaybackController(PlaybackService playbackService) {
    this.playbackService = playbackService;
  }

  @Operation(
      summary = "Iniciar reproducción",
      description = "Comienza la reproducción de una pista específica",
      security = {@SecurityRequirement(name = "Bearer Authentication")})
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Reproducción iniciada correctamente",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PlaybackSessionDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "Error al iniciar reproducción",
          content = @Content(mediaType = "application/json"))})
  @PostMapping("/start")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackSessionDTO>>> start(
      @Parameter(description = "ID de la pista a reproducir") @RequestParam Long trackId,
      Authentication authentication) {
    logger.debug("Solicitud para iniciar reproducción de pista: {}", trackId);

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        PlaybackSessionDTO session = playbackService.startPlayback(trackId, userId);
        return ResponseEntity.ok(ApiResponse.success(session, "Reproducción iniciada correctamente"));
      } catch (Exception e) {
        logger.error("Error al iniciar reproducción", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al iniciar reproducción", e.getMessage()));
      }
    });
  }

  @PostMapping("/pause")
  public ResponseEntity<ApiResponse<Void>> pause(Authentication authentication) {
    logger.debug("Solicitud para pausar reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      boolean paused = playbackService.pausePlayback(userId);
      if (paused) {
        return ResponseEntity.ok(ApiResponse.successMessage("Reproducción pausada correctamente"));
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("No hay reproducción activa para pausar"));
    } catch (Exception e) {
      logger.error("Error al pausar reproducción", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al pausar reproducción", e.getMessage()));
    }
  }

  @PostMapping("/resume")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackSessionDTO>>> resume(
      Authentication authentication) {
    logger.debug("Solicitud para reanudar reproducción");

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        return playbackService.resumePlayback(userId)
            .map(session -> ResponseEntity.ok(
                ApiResponse.success(session, "Reproducción reanudada correctamente")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("No hay reproducción pausada para reanudar")));
      } catch (Exception e) {
        logger.error("Error al reanudar reproducción", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al reanudar reproducción", e.getMessage()));
      }
    });
  }

  @Operation(
      summary = "Saltar a la siguiente pista",
      description = "Adelanta la reproducción a la siguiente pista precargada",
      security = {@SecurityRequirement(name = "Bearer Authentication")})
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Siguiente pista lista",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PlaybackSessionDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "No hay pista siguiente disponible",
          content = @Content(mediaType = "application/json")),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "500",
      description = "Error al adelantar la pista",
      content = @Content(mediaType = "application/json"))})
  @PostMapping("/next")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackSessionDTO>>> next(
      Authentication authentication) {
    logger.debug("Solicitud para adelantar reproducción");

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        return playbackService.skipToNext(userId)
            .map(session -> ResponseEntity.ok(
                ApiResponse.success(session, "Siguiente pista preparada")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("No hay pista siguiente disponible")));
      } catch (Exception e) {
        logger.error("Error al adelantar reproducción", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al adelantar reproducción", e.getMessage()));
      }
    });
  }

  @Operation(
      summary = "Retroceder a la pista previa",
      description = "Retrocede la reproducción a la pista anterior registrada",
      security = {@SecurityRequirement(name = "Bearer Authentication")})
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Pista previa lista",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PlaybackSessionDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "No hay pista previa disponible",
          content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "Error al retroceder la pista",
          content = @Content(mediaType = "application/json"))})
  @PostMapping("/previous")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<ApiResponse<PlaybackSessionDTO>>> previous(
      Authentication authentication) {
    logger.debug("Solicitud para retroceder reproducción");

    return CompletableFuture.supplyAsync(() -> {
      try {
        Long userId = Long.parseLong(authentication.getName());
        return playbackService.skipToPrevious(userId)
            .map(session -> ResponseEntity.ok(
                ApiResponse.success(session, "Pista previa preparada")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("No hay pista previa disponible")));
      } catch (Exception e) {
        logger.error("Error al retroceder reproducción", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error al retroceder reproducción", e.getMessage()));
      }
    });
  }

  @PostMapping("/stop")
  public ResponseEntity<ApiResponse<Void>> stop(Authentication authentication) {
    logger.debug("Solicitud para detener reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      boolean stopped = playbackService.stopPlayback(userId);
      if (stopped) {
        return ResponseEntity.ok(ApiResponse.successMessage("Reproducción detenida correctamente"));
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("No hay reproducción activa para detener"));
    } catch (Exception e) {
      logger.error("Error al detener reproducción", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al detener reproducción", e.getMessage()));
    }
  }

  @Operation(
      summary = "Obtener estado de reproducción",
      description = "Devuelve el estado actual de reproducción del usuario",
      security = {@SecurityRequirement(name = "Bearer Authentication")})
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Estado de reproducción obtenido correctamente",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PlaybackStatusDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "No hay reproducción activa",
          content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "Error al obtener estado de reproducción",
          content = @Content(mediaType = "application/json"))})
  @GetMapping("/status")
  public ResponseEntity<ApiResponse<PlaybackStatusDTO>> getStatus(Authentication authentication) {
    logger.debug("Solicitud para obtener estado de reproducción");

    try {
      Long userId = Long.parseLong(authentication.getName());
      return playbackService.getCurrentPlaybackStatus(userId)
          .map(status -> ResponseEntity.ok(
              ApiResponse.success(status, "Estado de reproducción actual")))
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("No hay reproducción activa")));
    } catch (Exception e) {
      logger.error("Error al obtener estado de reproducción", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al obtener estado de reproducción", e.getMessage()));
    }
  }

  @Operation(
      summary = "Obtener historial de reproducciones",
      description = "Devuelve el historial de reproducciones del usuario paginado",
      security = {@SecurityRequirement(name = "Bearer Authentication")})
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "Historial obtenido correctamente",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = PlaybackHistory.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "Error al obtener historial",
          content = @Content(mediaType = "application/json"))})
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
      logger.error("Error al obtener historial de reproducciones", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al obtener historial de reproducciones", e.getMessage()));
    }
  }

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
      logger.error("Error al obtener historial de reproducciones por fecha", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Error al obtener historial de reproducciones", e.getMessage()));
    }
  }
}
