package com.tfu.backend.playback;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para representar el estado actual de la reproducción de un usuario.
 */
@Schema(description = "Estado actual de reproducción de una pista musical")
public class PlaybackStatusDTO {
  private final Long trackId;
  private final boolean paused;
  private final LocalDateTime startTime;

  /**
   * Constructor con todos los parámetros.
   * 
   * @param trackId   ID de la pista en reproducción
   * @param paused    Indica si la reproducción está pausada
   * @param startTime Hora de inicio de la reproducción
   */
  public PlaybackStatusDTO(Long trackId, boolean paused, LocalDateTime startTime) {
    this.trackId = trackId;
    this.paused = paused;
    this.startTime = startTime;
  }

  /**
   * Obtiene el ID de la pista en reproducción.
   * 
   * @return ID de la pista
   */
  @Schema(description = "ID de la pista en reproducción", example = "123")
  public Long getTrackId() {
    return trackId;
  }

  /**
   * Indica si la reproducción está pausada.
   * 
   * @return true si está pausada, false en caso contrario
   */
  @Schema(description = "Indica si la reproducción está pausada", example = "false")
  public boolean isPaused() {
    return paused;
  }

  /**
   * Obtiene la hora de inicio de la reproducción.
   * 
   * @return Hora de inicio
   */
  @Schema(description = "Fecha y hora en que comenzó la reproducción", example = "2023-10-15T14:30:15")
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Calcula la duración en segundos desde el inicio de la reproducción.
   * 
   * @return Duración en segundos
   */
  @Schema(description = "Duración en segundos desde que comenzó la reproducción", example = "127")
  public long getDurationSeconds() {
    return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
  }
}