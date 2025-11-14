package com.tfu.backend.artist;

import lombok.Data;

/**
 * DTO para representar una canción de un artista específico
 */
@Data
public class ArtistTrackDto {
    private String id;
    private String name;
    private String album;
    private String albumName;
    private String imageUrl;
    private String previewUrl;
    private String spotifyUrl;
    private String artistSpotifyUrl;
    private int durationMs;
    private int popularity;
    private boolean explicit;
    private int trackNumber;
    private String releaseDate;
    
    public ArtistTrackDto() {}
    
    public ArtistTrackDto(String id, String name, String album, String albumName, String imageUrl, 
                         String previewUrl, String spotifyUrl, String artistSpotifyUrl, int durationMs, int popularity, 
                         boolean explicit, int trackNumber, String releaseDate) {
        this.id = id;
        this.name = name;
        this.album = album;
        this.albumName = albumName;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
        this.spotifyUrl = spotifyUrl;
        this.artistSpotifyUrl = artistSpotifyUrl;
        this.durationMs = durationMs;
        this.popularity = popularity;
        this.explicit = explicit;
        this.trackNumber = trackNumber;
        this.releaseDate = releaseDate;
    }
    
    /**
     * Formatea la duración en formato mm:ss
     */
    public String getFormattedDuration() {
        if (durationMs <= 0) return "--:--";
        int minutes = durationMs / 60000;
        int seconds = (durationMs % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }
}