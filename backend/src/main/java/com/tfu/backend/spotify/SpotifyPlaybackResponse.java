package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta con datos de reproducción para una canción específica de Spotify
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyPlaybackResponse {
    private String trackId;
    private String name;
    private String artists;
    private String album;
    private String imageUrl;
    private String previewUrl;
    private Integer durationMs;
    private Boolean isPlayable;
    private String streamUrl; // URL para reproducir la canción (puede ser preview_url o una URL generada)
}