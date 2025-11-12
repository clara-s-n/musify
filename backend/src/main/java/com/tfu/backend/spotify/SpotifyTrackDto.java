package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyTrackDto {
    private String id;
    private String name;
    private String artists;
    private String album;
    private String imageUrl;
    private String previewUrl;
    
    // Información adicional del artista principal para navegación
    private String primaryArtistId;
    private String primaryArtistName;
    private String primaryArtistSpotifyUrl;
    
    // Constructor para retrocompatibilidad
    public SpotifyTrackDto(String id, String name, String artists, String album, String imageUrl, String previewUrl) {
        this.id = id;
        this.name = name;
        this.artists = artists;
        this.album = album;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
        
        // Extraer el artista principal del string de artistas (compatibilidad hacia atrás)
        this.primaryArtistName = extractPrimaryArtist(artists);
    }
    
    private String extractPrimaryArtist(String artistsString) {
        if (artistsString == null || artistsString.trim().isEmpty()) {
            return null;
        }
        // Si hay varios artistas separados por coma, tomar el primero
        String[] artistArray = artistsString.split(",");
        return artistArray[0].trim();
    }
}
