package com.tfu.backend.player;

import com.tfu.backend.spotify.SpotifyService;
import com.tfu.backend.spotify.SpotifyTrackDto;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para manejo de reproductor con cola, navegación y autoplay
 */
@Service
public class PlayerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    
    private final SpotifyService spotifyService;
    private PlayerState playerState;
    
    public PlayerService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        this.playerState = new PlayerState();
    }
    
    /**
     * Inicia reproducción de una canción y la añade a la cola
     */
    public CompletableFuture<PlayerState> play(String trackId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Iniciando reproducción del track: {}", trackId);
                
                // Buscar información del track (simulado con búsqueda)
                List<SpotifyTrackDto> searchResults = spotifyService.searchTracks(trackId, 1);
                if (searchResults.isEmpty()) {
                    // Si no encuentra por ID, buscar tracks aleatorios
                    searchResults = spotifyService.getRandomTracks(1);
                }
                
                if (!searchResults.isEmpty()) {
                    SpotifyTrackDto track = searchResults.get(0);
                    TrackInfo trackInfo = convertToTrackInfo(track);
                    
                    // Añadir a la cola si no existe
                    if (playerState.getQueue() == null) {
                        playerState.setQueue(new ArrayList<>());
                    }
                    
                    // Si es una nueva canción, añadirla a la cola
                    if (playerState.getQueue().stream().noneMatch(t -> t.getId().equals(trackInfo.getId()))) {
                        playerState.getQueue().add(trackInfo);
                        playerState.setCurrentIndex(playerState.getQueue().size() - 1);
                    } else {
                        // Encontrar el índice de la canción existente
                        for (int i = 0; i < playerState.getQueue().size(); i++) {
                            if (playerState.getQueue().get(i).getId().equals(trackInfo.getId())) {
                                playerState.setCurrentIndex(i);
                                break;
                            }
                        }
                    }
                    
                    playerState.setCurrentTrack(trackInfo);
                    playerState.setStatus("playing");
                    playerState.setPosition(0);
                    playerState.setDuration(trackInfo.getDuration());
                    
                    logger.info("Reproducción iniciada: {} - {}", trackInfo.getName(), trackInfo.getArtist());
                }
                
                return playerState;
            } catch (Exception e) {
                logger.error("Error al iniciar reproducción: {}", e.getMessage(), e);
                playerState.setStatus("error");
                return playerState;
            }
        });
    }
    
    /**
     * Reproduce la siguiente canción en la cola
     */
    public CompletableFuture<PlayerState> playNext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Reproduciendo siguiente canción");
                
                if (playerState.getQueue() == null || playerState.getQueue().isEmpty()) {
                    // Si no hay cola, obtener canciones aleatorias
                    return generateAndPlayRecommended().join();
                }
                
                int nextIndex = playerState.getCurrentIndex() + 1;
                
                // Si llegamos al final y repeat está activado, volver al inicio
                if (nextIndex >= playerState.getQueue().size()) {
                    if (playerState.isRepeat()) {
                        nextIndex = 0;
                    } else {
                        // Generar nueva recomendación
                        return generateAndPlayRecommended().join();
                    }
                }
                
                playerState.setCurrentIndex(nextIndex);
                TrackInfo nextTrack = playerState.getQueue().get(nextIndex);
                playerState.setCurrentTrack(nextTrack);
                playerState.setStatus("playing");
                playerState.setPosition(0);
                playerState.setDuration(nextTrack.getDuration());
                
                logger.info("Siguiente canción: {} - {}", nextTrack.getName(), nextTrack.getArtist());
                return playerState;
                
            } catch (Exception e) {
                logger.error("Error al reproducir siguiente canción: {}", e.getMessage(), e);
                return playerState;
            }
        });
    }
    
    /**
     * Reproduce la canción anterior en la cola
     */
    public CompletableFuture<PlayerState> playPrevious() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Reproduciendo canción anterior");
                
                if (playerState.getQueue() == null || playerState.getQueue().isEmpty()) {
                    return playerState;
                }
                
                int prevIndex = playerState.getCurrentIndex() - 1;
                
                // Si llegamos al inicio y repeat está activado, ir al final
                if (prevIndex < 0) {
                    if (playerState.isRepeat()) {
                        prevIndex = playerState.getQueue().size() - 1;
                    } else {
                        prevIndex = 0; // Quedarse en la primera canción
                    }
                }
                
                playerState.setCurrentIndex(prevIndex);
                TrackInfo prevTrack = playerState.getQueue().get(prevIndex);
                playerState.setCurrentTrack(prevTrack);
                playerState.setStatus("playing");
                playerState.setPosition(0);
                playerState.setDuration(prevTrack.getDuration());
                
                logger.info("Canción anterior: {} - {}", prevTrack.getName(), prevTrack.getArtist());
                return playerState;
                
            } catch (Exception e) {
                logger.error("Error al reproducir canción anterior: {}", e.getMessage(), e);
                return playerState;
            }
        });
    }
    
    /**
     * Pausa la reproducción
     */
    public PlayerState pause() {
        logger.info("Pausando reproducción");
        playerState.setStatus("paused");
        return playerState;
    }
    
    /**
     * Reanuda la reproducción
     */
    public PlayerState resume() {
        logger.info("Reanudando reproducción");
        playerState.setStatus("playing");
        return playerState;
    }
    
    /**
     * Detiene la reproducción
     */
    public PlayerState stop() {
        logger.info("Deteniendo reproducción");
        playerState.setStatus("stopped");
        playerState.setPosition(0);
        return playerState;
    }
    
    /**
     * Obtiene el estado actual del reproductor
     */
    public PlayerState getState() {
        return playerState;
    }
    
    /**
     * Activa/desactiva shuffle
     */
    public PlayerState toggleShuffle() {
        playerState.setShuffle(!playerState.isShuffle());
        
        if (playerState.isShuffle() && playerState.getQueue() != null) {
            // Guardar la canción actual
            TrackInfo currentTrack = playerState.getCurrentTrack();
            
            // Mezclar la cola
            Collections.shuffle(playerState.getQueue());
            
            // Encontrar la nueva posición de la canción actual
            if (currentTrack != null) {
                for (int i = 0; i < playerState.getQueue().size(); i++) {
                    if (playerState.getQueue().get(i).getId().equals(currentTrack.getId())) {
                        playerState.setCurrentIndex(i);
                        break;
                    }
                }
            }
        }
        
        logger.info("Shuffle {}", playerState.isShuffle() ? "activado" : "desactivado");
        return playerState;
    }
    
    /**
     * Activa/desactiva repeat
     */
    public PlayerState toggleRepeat() {
        playerState.setRepeat(!playerState.isRepeat());
        logger.info("Repeat {}", playerState.isRepeat() ? "activado" : "desactivado");
        return playerState;
    }
    
    /**
     * Genera y reproduce una canción recomendada (autoplay)
     */
    private CompletableFuture<PlayerState> generateAndPlayRecommended() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Generando canción recomendada para autoplay");
                
                // Obtener canciones aleatorias como recomendación
                List<SpotifyTrackDto> recommendations = spotifyService.getRandomTracks(1);
                
                if (!recommendations.isEmpty()) {
                    SpotifyTrackDto track = recommendations.get(0);
                    TrackInfo trackInfo = convertToTrackInfo(track);
                    
                    // Añadir a la cola
                    if (playerState.getQueue() == null) {
                        playerState.setQueue(new ArrayList<>());
                    }
                    
                    playerState.getQueue().add(trackInfo);
                    playerState.setCurrentIndex(playerState.getQueue().size() - 1);
                    playerState.setCurrentTrack(trackInfo);
                    playerState.setStatus("playing");
                    playerState.setPosition(0);
                    playerState.setDuration(trackInfo.getDuration());
                    
                    logger.info("Autoplay iniciado: {} - {}", trackInfo.getName(), trackInfo.getArtist());
                }
                
                return playerState;
            } catch (Exception e) {
                logger.error("Error en autoplay: {}", e.getMessage(), e);
                return playerState;
            }
        });
    }
    
    /**
     * Convierte SpotifyTrackDto a TrackInfo
     */
    private TrackInfo convertToTrackInfo(SpotifyTrackDto spotifyTrack) {
        return new TrackInfo(
            spotifyTrack.getId(),
            spotifyTrack.getName(),
            spotifyTrack.getArtists(),
            spotifyTrack.getAlbum(),
            spotifyTrack.getImageUrl(),
            spotifyTrack.getPreviewUrl(), // URL de audio
            180000L // Duración aproximada en ms (3 minutos)
        );
    }
}