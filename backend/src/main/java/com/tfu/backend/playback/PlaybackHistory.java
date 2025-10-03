package com.tfu.backend.playback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de reproducción de una pista.
 * Almacena información sobre quién, cuándo y qué pista ha sido reproducida.
 */
@Schema(description = "Registro histórico de una reproducción de pista musical")
@Entity
@Table(name = "playback_history")
public class PlaybackHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "track_id", nullable = false)
  private Long trackId;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "completed")
  private Boolean completed;

  // Constructores
  public PlaybackHistory() {
  }

  public PlaybackHistory(Long userId, Long trackId) {
    this.userId = userId;
    this.trackId = trackId;
    this.timestamp = LocalDateTime.now();
  }

  // Getters y Setters
  @Schema(description = "ID único del registro de reproducción", example = "1")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Schema(description = "ID del usuario que reprodujo la pista", example = "42")
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  @Schema(description = "ID de la pista reproducida", example = "123")
  public Long getTrackId() {
    return trackId;
  }

  public void setTrackId(Long trackId) {
    this.trackId = trackId;
  }

  @Schema(description = "Fecha y hora en que se inició la reproducción", example = "2023-10-15T14:30:15")
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Schema(description = "Duración de la reproducción en segundos", example = "217")
  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  @Schema(description = "Indica si la pista fue reproducida completamente", example = "true")
  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  @Override
  public String toString() {
    return "PlaybackHistory{" +
        "id=" + id +
        ", userId=" + userId +
        ", trackId=" + trackId +
        ", timestamp=" + timestamp +
        ", completed=" + completed +
        '}';
  }
}