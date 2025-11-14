package com.tfu.backend.search;

import com.tfu.backend.spotify.SpotifyService;
import com.tfu.backend.spotify.SpotifyTrackDto;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para búsqueda categorizada que combina diferentes tipos de contenido
 */
@Service
public class CategorizedSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategorizedSearchService.class);
    
    private final SpotifyService spotifyService;
    
    public CategorizedSearchService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }
    
    /**
     * Realiza búsqueda categorizada devolviendo resultados separados por tipo
     * @param query término de búsqueda
     * @param limit límite de resultados por categoría
     * @return respuesta categorizada con songs, albums, artists, concerts
     */
    public CategorizedSearchResponse searchCategorized(String query, int limit) {
        logger.info("Iniciando búsqueda categorizada para: '{}' con límite: {}", query, limit);
        
        // Obtener canciones de Spotify
        List<SpotifyTrackDto> songs = spotifyService.searchTracks(query, limit);
        
        // Generar álbumes basados en las canciones encontradas (simular diversidad)
        List<AlbumDto> albums = generateAlbumsFromTracks(songs, limit);
        
        // Generar artistas basados en las canciones (simular búsqueda de artistas)
        List<ArtistDto> artists = generateArtistsFromTracks(songs, limit);
        
        // Generar conciertos ficticios basados en la búsqueda
        List<ConcertDto> concerts = generateMockConcerts(query, limit);
        
        logger.info("Búsqueda categorizada completada - Songs: {}, Albums: {}, Artists: {}, Concerts: {}", 
                   songs.size(), albums.size(), artists.size(), concerts.size());
        
        return new CategorizedSearchResponse(songs, albums, artists, concerts);
    }
    
    /**
     * Genera álbumes únicos basados en las canciones encontradas
     */
    private List<AlbumDto> generateAlbumsFromTracks(List<SpotifyTrackDto> tracks, int limit) {
        List<AlbumDto> albums = new ArrayList<>();
        List<String> seenAlbums = new ArrayList<>();
        
        for (SpotifyTrackDto track : tracks) {
            if (albums.size() >= limit) break;
            
            String albumKey = track.getAlbum() + "-" + track.getArtists();
            if (!seenAlbums.contains(albumKey)) {
                seenAlbums.add(albumKey);
                albums.add(new AlbumDto(
                    track.getId() + "-album",
                    track.getAlbum(),
                    track.getArtists(),
                    track.getImageUrl(),
                    "2024",
                    10 + (int)(Math.random() * 15) // Número aleatorio de tracks
                ));
            }
        }
        
        return albums;
    }
    
    /**
     * Genera artistas únicos basados en las canciones encontradas
     */
    private List<ArtistDto> generateArtistsFromTracks(List<SpotifyTrackDto> tracks, int limit) {
        List<ArtistDto> artists = new ArrayList<>();
        List<String> seenArtists = new ArrayList<>();
        
        for (SpotifyTrackDto track : tracks) {
            if (artists.size() >= limit) break;
            
            String artistName = track.getArtists().split(",")[0].trim(); // Tomar el primer artista
            if (!seenArtists.contains(artistName)) {
                seenArtists.add(artistName);
                artists.add(new ArtistDto(
                    track.getId() + "-artist",
                    artistName,
                    track.getImageUrl(),
                    "Pop, Rock", // Géneros genéricos
                    100000 + (int)(Math.random() * 5000000) // Followers aleatorios
                ));
            }
        }
        
        return artists;
    }
    
    /**
     * Genera conciertos ficticios basados en el término de búsqueda
     */
    private List<ConcertDto> generateMockConcerts(String query, int limit) {
        List<ConcertDto> concerts = new ArrayList<>();
        
        List<String> venues = Arrays.asList(
            "Madison Square Garden", "Wembley Stadium", "Red Rocks Amphitheatre", 
            "Hollywood Bowl", "Royal Albert Hall", "Coachella Festival"
        );
        
        List<String> cities = Arrays.asList(
            "New York", "London", "Los Angeles", "Chicago", "Barcelona", "Tokyo"
        );
        
        List<String> dates = Arrays.asList(
            "2024-12-15", "2024-12-20", "2025-01-10", "2025-01-25", 
            "2025-02-14", "2025-03-08"
        );
        
        for (int i = 0; i < Math.min(limit, 3); i++) {
            concerts.add(new ConcertDto(
                "concert-" + i,
                query + " Live Tour",
                query + " Band",
                venues.get(i % venues.size()),
                dates.get(i % dates.size()),
                cities.get(i % cities.size()),
                "https://via.placeholder.com/300x200?text=Concert+" + (i + 1)
            ));
        }
        
        return concerts;
    }
}