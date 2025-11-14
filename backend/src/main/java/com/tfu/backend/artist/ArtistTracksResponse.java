package com.tfu.backend.artist;

import lombok.Data;
import java.util.List;

/**
 * Response DTO para las canciones de un artista con paginación
 */
@Data
public class ArtistTracksResponse {
    private String artistId;
    private String artistName;
    private String artistImageUrl;
    private String artistSpotifyUrl;
    private List<ArtistTrackDto> tracks;
    private PaginationInfo pagination;
    
    public ArtistTracksResponse() {}
    
    public ArtistTracksResponse(String artistId, String artistName, String artistImageUrl, 
                               String artistSpotifyUrl, List<ArtistTrackDto> tracks, PaginationInfo pagination) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.artistImageUrl = artistImageUrl;
        this.artistSpotifyUrl = artistSpotifyUrl;
        this.tracks = tracks;
        this.pagination = pagination;
    }
    
    @Data
    public static class PaginationInfo {
        private int currentPage;
        private int totalPages;
        private int totalTracks;
        private int tracksPerPage;
        private boolean hasNext;
        private boolean hasPrevious;
        
        public PaginationInfo() {}
        
        public PaginationInfo(int currentPage, int totalPages, int totalTracks, 
                             int tracksPerPage, boolean hasNext, boolean hasPrevious) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalTracks = totalTracks;
            this.tracksPerPage = tracksPerPage;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }
    }
}