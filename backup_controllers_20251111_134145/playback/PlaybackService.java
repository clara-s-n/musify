package com.tfu.backend.playback;

import com.tfu.backend.catalog.TrackService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar la reproducción de pistas.
 */
@Service
public class PlaybackService {
  private static final Logger logger = LoggerFactory.getLogger(PlaybackService.class);

  private final StreamClient streamClient;
  private final PlaybackHistoryRepository historyRepository;
  private final TrackService trackService;

  // Cache para mantener estado de reproducción actual por usuario
  private final ConcurrentHashMap<Long, ActivePlayback> activePlaybacks = new ConcurrentHashMap<>();

  /**
   * Constructor que inyecta dependencias necesarias.
   * 
   * @param streamClient      Cliente para obtener URLs de streaming
   * @param historyRepository Repositorio para historial de reproducciones
   * @param trackService      Servicio de pistas musicales
   */
  public PlaybackService(
      StreamClient streamClient,
      PlaybackHistoryRepository historyRepository,
      TrackService trackService) {
    this.streamClient = streamClient;
    this.historyRepository = historyRepository;
    this.trackService = trackService;
  }

  /**
   * Inicia la reproducción de una pista para un usuario.
   * 
   * @param trackId ID de la pista a reproducir
   * @param userId  ID del usuario que reproduce la pista
   * @return DTO con la URL de reproducción
   */
  @Transactional
  @CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
  @Retry(name = "streamSource")
  public PlaybackDTO startPlayback(Long trackId, Long userId) {
    logger.debug("Iniciando reproducción de pista {} para usuario {}", trackId, userId);

    // Verificar si el usuario tiene una reproducción activa y marcarla como
    // completada
    Optional.ofNullable(activePlaybacks.get(userId))
        .ifPresent(active -> {
          PlaybackHistory history = historyRepository.findById(active.getHistoryId()).orElse(null);
          if (history != null) {
            history.setCompleted(false);
            historyRepository.save(history);
          }
        });

    // Registrar nueva reproducción
    PlaybackHistory history = new PlaybackHistory(userId, trackId);
    history = historyRepository.save(history);

    // Obtener URL de streaming
    String streamUrl = streamClient.getStreamUrl(trackId.toString());

    // Registrar reproducción activa
    ActivePlayback activePlayback = new ActivePlayback(history.getId(), trackId);
    activePlaybacks.put(userId, activePlayback);

    logger.info("Reproducción iniciada para usuario {} y pista {}", userId, trackId);
    return new PlaybackDTO(streamUrl);
  }

  /**
   * Método de fallback que retorna una URL alternativa si falla el streaming
   * principal.
   * 
   * @param trackId ID de la pista solicitada
   * @param userId  ID del usuario
   * @param t       Excepción capturada
   * @return URL alternativa en un DTO
   */
  public PlaybackDTO fallbackUrl(Long trackId, Long userId, Throwable t) {
    logger.warn("Fallback activado para pista: {} y usuario: {}. Error: {}", trackId, userId, t.getMessage());
    return new PlaybackDTO("https://cdn.example/low-bitrate/" + trackId);
  }

  /**
   * Pausa la reproducción actual de un usuario.
   * 
   * @param userId ID del usuario
   * @return true si se pausó correctamente, false en caso contrario
   */
  @Transactional
  public boolean pausePlayback(Long userId) {
    logger.debug("Pausando reproducción para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null) {
      logger.info("No hay reproducción activa para el usuario {}", userId);
      return false;
    }

    activePlayback.setPaused(true);
    logger.info("Reproducción pausada para usuario {}", userId);
    return true;
  }

  /**
   * Reanuda la reproducción pausada de un usuario.
   * 
   * @param userId ID del usuario
   * @return URL de reproducción o vacío si no hay reproducción pausada
   */
  @Transactional
  public Optional<PlaybackDTO> resumePlayback(Long userId) {
    logger.debug("Reanudando reproducción para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null || !activePlayback.isPaused()) {
      logger.info("No hay reproducción pausada para el usuario {}", userId);
      return Optional.empty();
    }

    activePlayback.setPaused(false);

    // Obtener URL de streaming
    String streamUrl = streamClient.getStreamUrl(activePlayback.getTrackId().toString());

    logger.info("Reproducción reanudada para usuario {}", userId);
    return Optional.of(new PlaybackDTO(streamUrl));
  }

  /**
   * Detiene la reproducción actual de un usuario.
   * 
   * @param userId ID del usuario
   * @return true si se detuvo correctamente, false en caso contrario
   */
  @Transactional
  public boolean stopPlayback(Long userId) {
    logger.debug("Deteniendo reproducción para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null) {
      logger.info("No hay reproducción activa para el usuario {}", userId);
      return false;
    }

    // Marcar historial como completado
    PlaybackHistory history = historyRepository.findById(activePlayback.getHistoryId()).orElse(null);
    if (history != null) {
      history.setCompleted(true);
      historyRepository.save(history);
    }

    // Eliminar reproducción activa
    activePlaybacks.remove(userId);

    logger.info("Reproducción detenida para usuario {}", userId);
    return true;
  }

  /**
   * Obtiene el historial de reproducciones de un usuario.
   * 
   * @param userId   ID del usuario
   * @param pageable Configuración de paginación
   * @return Página con el historial de reproducciones
   */
  @Transactional(readOnly = true)
  public Page<PlaybackHistory> getUserPlaybackHistory(Long userId, Pageable pageable) {
    logger.debug("Obteniendo historial de reproducciones para usuario {}", userId);
    return historyRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
  }

  /**
   * Obtiene el historial de reproducciones de un usuario en un rango de fechas.
   * 
   * @param userId ID del usuario
   * @param start  Fecha de inicio
   * @param end    Fecha de fin
   * @return Lista de reproducciones en el rango especificado
   */
  @Transactional(readOnly = true)
  public List<PlaybackHistory> getUserPlaybackHistoryInDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
    logger.debug("Obteniendo historial de reproducciones para usuario {} entre {} y {}", userId, start, end);
    return historyRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, start, end);
  }

  /**
   * Obtiene el estado actual de reproducción de un usuario.
   * 
   * @param userId ID del usuario
   * @return Información de la reproducción actual o vacío si no hay reproducción
   *         activa
   */
  @Transactional(readOnly = true)
  public Optional<PlaybackStatusDTO> getCurrentPlaybackStatus(Long userId) {
    logger.debug("Obteniendo estado actual de reproducción para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null) {
      logger.info("No hay reproducción activa para el usuario {}", userId);
      return Optional.empty();
    }

    PlaybackHistory history = historyRepository.findById(activePlayback.getHistoryId()).orElse(null);
    if (history == null) {
      logger.warn("Historia de reproducción no encontrada para el ID: {}", activePlayback.getHistoryId());
      activePlaybacks.remove(userId);
      return Optional.empty();
    }

    PlaybackStatusDTO status = new PlaybackStatusDTO(
        history.getTrackId(),
        activePlayback.isPaused(),
        history.getTimestamp());

    return Optional.of(status);
  }

  /**
   * Extrae el ID de usuario del contexto de seguridad.
   * 
   * @return ID del usuario autenticado
   */
  public Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      return Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());
    }
    return null;
  }

  /**
   * Clase interna para mantener el estado de las reproducciones activas.
   */
  private static class ActivePlayback {
    private final Long historyId;
    private final Long trackId;
    private boolean paused;

    public ActivePlayback(Long historyId, Long trackId) {
      this.historyId = historyId;
      this.trackId = trackId;
      this.paused = false;
    }

    public Long getHistoryId() {
      return historyId;
    }

    public Long getTrackId() {
      return trackId;
    }

    public boolean isPaused() {
      return paused;
    }

    public void setPaused(boolean paused) {
      this.paused = paused;
    }
  }
}