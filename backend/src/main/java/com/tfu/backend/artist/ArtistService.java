package com.tfu.backend.artist;

import com.tfu.backend.spotify.SpotifyService;
import com.tfu.backend.spotify.SpotifyTrackDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para obtener información detallada de artistas y sus canciones
 */
@Service
public class ArtistService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtistService.class);
    
    private final SpotifyService spotifyService;

    public ArtistService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    /**
     * Obtiene las canciones de un artista con paginación
     */
    @Retry(name = "streamSource")
    @CircuitBreaker(name = "streamSource", fallbackMethod = "getArtistTracksFallback")
    @Cacheable(value = "artistTracks", key = "#artistId + '_' + #page + '_' + #size")
    public ArtistTracksResponse getArtistTracks(String artistId, int page, int size) {
        logger.info("Getting tracks for artist: {} (page: {}, size: {})", artistId, page, size);
        
        try {
            // Para esta implementación simplificada, buscamos el artista por ID
            // En una implementación real, usaríamos la API de Spotify para obtener artistas por ID
            String artistName = getArtistNameById(artistId);
            if (artistName == null) {
                return createEmptyResponse(artistId, "Artista no encontrado");
            }
            
            // Buscar canciones del artista usando el servicio existente
            List<SpotifyTrackDto> allTracks = spotifyService.searchTracks("artist:" + artistName, 50);
            
            // Filtrar solo las canciones del artista específico
            List<SpotifyTrackDto> artistTracks = allTracks.stream()
                .filter(track -> track.getPrimaryArtistName() != null && 
                               track.getPrimaryArtistName().toLowerCase().contains(artistName.toLowerCase()))
                .collect(Collectors.toList());
            
            // Aplicar paginación manual
            int start = page * size;
            int end = Math.min(start + size, artistTracks.size());
            
            if (start >= artistTracks.size()) {
                return createEmptyResponse(artistId, artistName);
            }
            
            List<SpotifyTrackDto> pageTracksDto = artistTracks.subList(start, end);
            
            // Convertir a ArtistTrackDto
            List<ArtistTrackDto> pageTracks = pageTracksDto.stream()
                .map(this::convertToArtistTrack)
                .collect(Collectors.toList());
            
            // Crear información de paginación
            ArtistTracksResponse.PaginationInfo pagination = new ArtistTracksResponse.PaginationInfo();
            pagination.setCurrentPage(page);
            pagination.setTracksPerPage(size);
            pagination.setTotalTracks(artistTracks.size());
            pagination.setTotalPages((int) Math.ceil((double) artistTracks.size() / size));
            pagination.setHasNext(end < artistTracks.size());
            pagination.setHasPrevious(page > 0);
            
            // Crear respuesta
            ArtistTracksResponse response = new ArtistTracksResponse();
            response.setArtistId(artistId);
            response.setArtistName(artistName);
            response.setArtistImageUrl(null); // Simplificado por ahora
            response.setArtistSpotifyUrl("https://open.spotify.com/artist/" + artistId);
            response.setTracks(pageTracks);
            response.setPagination(pagination);
            
            logger.info("Found {} tracks for artist: {}", pageTracks.size(), artistName);
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting tracks for artist: {}", artistId, e);
            throw e;
        }
    }
    
    /**
     * Método simplificado para obtener el nombre del artista por ID
     * En una implementación real, esto consultaría la API de Spotify
     */
    private String getArtistNameById(String artistId) {
        // Mapeo básico de algunos IDs conocidos para demostración
        // En producción, esto debería consultar la API de Spotify
        return switch (artistId) {
            case "4Z8W4fKeB5YxbusRsdQVPb" -> "Radiohead";
            case "6vWDO969PvNqNYHIOW5v0m" -> "Beyoncé";
            case "1vCWHaC5f2uS3yhpwWbIA6" -> "Avicii";
            case "1dfeR4HaWDbWqFHLkxsg1d" -> "Queen";
            case "66CXWjxzNUsdJxJ2JdwvnR" -> "Ariana Grande";
            default -> {
                // Intentar buscar usando el ID como query
                try {
                    List<SpotifyTrackDto> results = spotifyService.searchTracks(artistId, 1);
                    if (!results.isEmpty()) {
                        yield results.get(0).getPrimaryArtistName();
                    }
                } catch (Exception e) {
                    logger.warn("Could not resolve artist name for ID: {}", artistId);
                }
                yield null;
            }
        };
    }
    
    /**
     * Convierte SpotifyTrackDto a ArtistTrackDto
     */
    private ArtistTrackDto convertToArtistTrack(SpotifyTrackDto track) {
        ArtistTrackDto artistTrack = new ArtistTrackDto();
        artistTrack.setId(track.getId());
        artistTrack.setName(track.getName());
        artistTrack.setAlbum(track.getAlbum());
        artistTrack.setAlbumName(track.getAlbum()); // Usar el mismo valor para albumName
        artistTrack.setImageUrl(track.getImageUrl());
        artistTrack.setPreviewUrl(track.getPreviewUrl());
        artistTrack.setSpotifyUrl("https://open.spotify.com/track/" + track.getId());
        artistTrack.setArtistSpotifyUrl("https://open.spotify.com/artist/" + track.getPrimaryArtistName());
        artistTrack.setDurationMs(0); // Simplificado - no disponible en el modelo actual
        artistTrack.setPopularity(50); // Valor por defecto
        artistTrack.setExplicit(false); // Valor por defecto
        artistTrack.setTrackNumber(1); // Valor por defecto
        artistTrack.setReleaseDate(null); // No disponible en el modelo actual
        
        return artistTrack;
    }
    
    /**
     * Crea una respuesta vacía
     */
    private ArtistTracksResponse createEmptyResponse(String artistId, String artistName) {
        ArtistTracksResponse response = new ArtistTracksResponse();
        response.setArtistId(artistId);
        response.setArtistName(artistName);
        response.setArtistImageUrl(null);
        response.setArtistSpotifyUrl("https://open.spotify.com/artist/" + artistId);
        response.setTracks(new ArrayList<>());
        
        ArtistTracksResponse.PaginationInfo pagination = new ArtistTracksResponse.PaginationInfo();
        pagination.setCurrentPage(0);
        pagination.setTracksPerPage(20);
        pagination.setTotalTracks(0);
        pagination.setTotalPages(0);
        pagination.setHasNext(false);
        pagination.setHasPrevious(false);
        
        response.setPagination(pagination);
        return response;
    }
    
    /**
     * Método de fallback para cuando falla el servicio
     */
    public ArtistTracksResponse getArtistTracksFallback(String artistId, int page, int size, Exception ex) {
        logger.error("Fallback triggered for artist tracks - artistId: {}, page: {}, size: {}", artistId, page, size, ex);
        return createEmptyResponse(artistId, "Error al cargar artista");
    }
}