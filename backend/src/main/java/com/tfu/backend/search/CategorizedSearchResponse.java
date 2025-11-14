package com.tfu.backend.search;

import com.tfu.backend.spotify.SpotifyTrackDto;
import lombok.Data;
import java.util.List;

/**
 * DTO para respuesta de búsqueda categorizada
 */
@Data
public class CategorizedSearchResponse {
    private List<SpotifyTrackDto> songs;
    private List<AlbumDto> albums;
    private List<ArtistDto> artists;
    private List<ConcertDto> concerts;
    
    public CategorizedSearchResponse() {
    }
    
    public CategorizedSearchResponse(List<SpotifyTrackDto> songs, List<AlbumDto> albums, 
                                   List<ArtistDto> artists, List<ConcertDto> concerts) {
        this.songs = songs;
        this.albums = albums;
        this.artists = artists;
        this.concerts = concerts;
    }
}