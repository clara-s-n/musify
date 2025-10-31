package com.tfu.backend.playback;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tfu.backend.catalog.TrackDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Representa el estado enriquecido de una sesión de reproducción.
 * Incluye URL de streaming, metadatos actuales y sugerencias precargadas
 * para reducir la latencia percibida por el cliente.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Información enriquecida de la sesión de reproducción")
public record PlaybackSessionDTO(
    @Schema(description = "URL para iniciar la reproducción de la pista", example = "https://api.musify.com/stream/123456") String streamUrl,
    @Schema(description = "Metadatos de la pista actualmente en reproducción") TrackDTO currentTrack,
    @Schema(description = "Metadatos de la pista anterior disponible para retroceder") TrackDTO previousTrack,
    @Schema(description = "Metadatos de la siguiente pista precargada") TrackDTO nextTrack,
    @Schema(description = "Lista de sugerencias adicionales en cola para autoplay/skip") List<TrackDTO> recommendations,
    @Schema(description = "Tiempo medido (ms) desde la solicitud hasta disponer de la URL de reproducción") long timeToPlayMs) {
}
