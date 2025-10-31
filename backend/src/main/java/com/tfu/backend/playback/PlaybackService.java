package com.tfu.backend.playback;

import com.tfu.backend.catalog.TrackDTO;
import com.tfu.backend.catalog.TrackService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la reproducción de pistas con métricas y sugerencias precargadas.
 */
@Service
public class PlaybackService {
  private static final Logger logger = LoggerFactory.getLogger(PlaybackService.class);
  private static final int RECOMMENDATION_LIMIT = 5;
  private static final int MAX_PREVIOUS_TRACKS = 10;

  private final StreamClient streamClient;
  private final PlaybackHistoryRepository historyRepository;
  private final TrackService trackService;
  private final MeterRegistry meterRegistry;

  // Cache para mantener estado de reproducción actual por usuario
  private final ConcurrentHashMap<Long, ActivePlayback> activePlaybacks = new ConcurrentHashMap<>();

  /**
   * Constructor que inyecta dependencias necesarias.
   *
   * @param streamClient      Cliente para obtener URLs de streaming
   * @param historyRepository Repositorio para historial de reproducciones
   * @param trackService      Servicio de pistas musicales
   * @param meterRegistry     Registro de métricas para instrumentar el TTP
   */
  public PlaybackService(
      StreamClient streamClient,
      PlaybackHistoryRepository historyRepository,
      TrackService trackService,
      MeterRegistry meterRegistry) {
    this.streamClient = streamClient;
    this.historyRepository = historyRepository;
    this.trackService = trackService;
    this.meterRegistry = meterRegistry;
  }

  /**
   * Inicia la reproducción de una pista para un usuario.
   *
   * @param trackId ID de la pista a reproducir
   * @param userId  ID del usuario que reproduce la pista
   * @return DTO enriquecido con información de reproducción
   */
  @Transactional
  @CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
  @Retry(name = "streamSource")
  public PlaybackSessionDTO startPlayback(Long trackId, Long userId) {
    logger.debug("Iniciando reproducción de pista {} para usuario {}", trackId, userId);

    ActivePlayback previousActive = activePlaybacks.get(userId);
    Deque<Long> previousStack = previousActive != null
        ? new ArrayDeque<>(previousActive.getPreviousTrackStack())
        : new ArrayDeque<>();

    if (previousActive != null) {
      pushPreviousTrack(previousStack, previousActive.getCurrentTrackId());
      markHistoryAsIncomplete(previousActive.getHistoryId());
    }

    Deque<Long> nextQueue = buildNextQueue(trackId, previousStack);

    PlaybackSessionDTO session = initializePlaybackSession(userId, trackId, previousStack, nextQueue, "start");

    logger.info("Reproducción iniciada para usuario {} y pista {}", userId, trackId);
    return session;
  }

  /**
   * Método de fallback que retorna una URL alternativa si falla el streaming principal.
   *
   * @param trackId ID de la pista solicitada
   * @param userId  ID del usuario
   * @param t       Excepción capturada
   * @return Sesión con URL alternativa
   */
  public PlaybackSessionDTO fallbackUrl(Long trackId, Long userId, Throwable t) {
    logger.warn("Fallback activado para pista: {} y usuario: {}. Error: {}", trackId, userId, t.getMessage());
    TrackDTO trackDTO = trackService.getTrackById(trackId).orElse(null);
    recordTimeToPlay(0, "fallback");
    return new PlaybackSessionDTO(
        "https://cdn.example/low-bitrate/" + trackId,
        trackDTO,
        null,
        null,
        List.of(),
        0L);
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
   * @return Sesión de reproducción o vacío si no hay reproducción pausada
   */
  @Transactional
  public Optional<PlaybackSessionDTO> resumePlayback(Long userId) {
    logger.debug("Reanudando reproducción para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null || !activePlayback.isPaused()) {
      logger.info("No hay reproducción pausada para el usuario {}", userId);
      return Optional.empty();
    }

    activePlayback.setPaused(false);

    long start = System.nanoTime();
    String streamUrl = streamClient.getStreamUrl(activePlayback.getCurrentTrackId().toString());
    long elapsed = System.nanoTime() - start;
    recordTimeToPlay(elapsed, "resume");

    logger.info("Reproducción reanudada para usuario {}", userId);
    return Optional.of(buildSessionResponse(streamUrl, activePlayback, elapsed));
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

    markHistoryAsCompleted(activePlayback.getHistoryId());
    activePlaybacks.remove(userId);

    logger.info("Reproducción detenida para usuario {}", userId);
    return true;
  }

  /**
   * Obtiene el historial de reproducciones de un usuario.
   */
  @Transactional(readOnly = true)
  public Page<PlaybackHistory> getUserPlaybackHistory(Long userId, Pageable pageable) {
    logger.debug("Obteniendo historial de reproducciones para usuario {}", userId);
    return historyRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
  }

  /**
   * Obtiene el historial de reproducciones de un usuario en un rango de fechas.
   */
  @Transactional(readOnly = true)
  public List<PlaybackHistory> getUserPlaybackHistoryInDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
    logger.debug("Obteniendo historial de reproducciones para usuario {} entre {} y {}", userId, start, end);
    return historyRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, start, end);
  }

  /**
   * Obtiene el estado actual de reproducción de un usuario.
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
   * Adelanta a la siguiente pista disponible en la cola precargada.
   */
  @Transactional
  public Optional<PlaybackSessionDTO> skipToNext(Long userId) {
    logger.debug("Saltando a la siguiente pista para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null) {
      logger.info("No hay reproducción activa para el usuario {}", userId);
      return Optional.empty();
    }

    pushPreviousTrack(activePlayback.getPreviousTrackStack(), activePlayback.getCurrentTrackId());
    markHistoryAsIncomplete(activePlayback.getHistoryId());

    Long nextTrackId = activePlayback.getNextTrackQueue().pollFirst();
    Deque<Long> residualQueue = new ArrayDeque<>(activePlayback.getNextTrackQueue());

    if (nextTrackId == null) {
      Deque<Long> fallbackQueue = buildNextQueue(activePlayback.getCurrentTrackId(), activePlayback.getPreviousTrackStack());
      nextTrackId = fallbackQueue.pollFirst();
      residualQueue = fallbackQueue;
      if (nextTrackId == null) {
        logger.info("No hay pista siguiente disponible para el usuario {}", userId);
        return Optional.empty();
      }
    }

    return Optional.of(updatePlaybackToTrack(userId, activePlayback, nextTrackId, "next", residualQueue));
  }

  /**
   * Retrocede a la pista anterior si existe en la pila de navegación.
   */
  @Transactional
  public Optional<PlaybackSessionDTO> skipToPrevious(Long userId) {
    logger.debug("Retrocediendo pista para usuario {}", userId);

    ActivePlayback activePlayback = activePlaybacks.get(userId);
    if (activePlayback == null) {
      logger.info("No hay reproducción activa para el usuario {}", userId);
      return Optional.empty();
    }

    Long previousTrackId = activePlayback.getPreviousTrackStack().pollLast();
    if (previousTrackId == null) {
      logger.info("No hay pista previa registrada para el usuario {}", userId);
      return Optional.empty();
    }

    markHistoryAsIncomplete(activePlayback.getHistoryId());

    Deque<Long> seedQueue = new ArrayDeque<>();
    seedQueue.offerLast(activePlayback.getCurrentTrackId());
    activePlayback.getNextTrackQueue().forEach(seedQueue::offerLast);

    return Optional.of(updatePlaybackToTrack(userId, activePlayback, previousTrackId, "previous", seedQueue));
  }

  /**
   * Extrae el ID de usuario del contexto de seguridad.
   */
  public Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
      return Long.parseLong(userDetails.getUsername());
    }
    return null;
  }

  private PlaybackSessionDTO initializePlaybackSession(Long userId, Long trackId, Deque<Long> previousTracks,
      Deque<Long> nextTracks, String trigger) {
    long start = System.nanoTime();
    PlaybackHistory history = historyRepository.save(new PlaybackHistory(userId, trackId));
    String streamUrl = streamClient.getStreamUrl(trackId.toString());
    long elapsed = System.nanoTime() - start;
    recordTimeToPlay(elapsed, trigger);

    ActivePlayback activePlayback = new ActivePlayback(history.getId(), trackId, previousTracks, nextTracks);
    activePlaybacks.put(userId, activePlayback);
    return buildSessionResponse(streamUrl, activePlayback, elapsed);
  }

  private PlaybackSessionDTO updatePlaybackToTrack(Long userId, ActivePlayback activePlayback, Long trackId,
      String trigger, Deque<Long> seedQueue) {
    long start = System.nanoTime();
    PlaybackHistory history = historyRepository.save(new PlaybackHistory(userId, trackId));
    String streamUrl = streamClient.getStreamUrl(trackId.toString());
    long elapsed = System.nanoTime() - start;
    recordTimeToPlay(elapsed, trigger);

    activePlayback.setHistoryId(history.getId());
    activePlayback.setCurrentTrackId(trackId);
    activePlayback.setPaused(false);

    Deque<Long> refreshedQueue = buildNextQueue(trackId, activePlayback.getPreviousTrackStack());
    if (seedQueue != null) {
      for (Long candidate : seedQueue) {
        if (candidate != null && !Objects.equals(candidate, trackId) && !refreshedQueue.contains(candidate)) {
          refreshedQueue.offerLast(candidate);
        }
      }
    }
    activePlayback.setNextTrackQueue(refreshedQueue);

    return buildSessionResponse(streamUrl, activePlayback, elapsed);
  }

  private PlaybackSessionDTO buildSessionResponse(String streamUrl, ActivePlayback activePlayback, long elapsedNanos) {
    TrackDTO current = trackService.getTrackById(activePlayback.getCurrentTrackId()).orElse(null);
    TrackDTO previous = Optional.ofNullable(activePlayback.getPreviousTrackStack().peekLast())
        .flatMap(trackService::getTrackById)
        .orElse(null);
    TrackDTO next = Optional.ofNullable(activePlayback.getNextTrackQueue().peekFirst())
        .flatMap(trackService::getTrackById)
        .orElse(null);

    List<TrackDTO> recommendations = activePlayback.getNextTrackQueue().stream()
        .map(trackService::getTrackById)
        .flatMap(Optional::stream)
        .collect(Collectors.toList());

    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

    return new PlaybackSessionDTO(
        streamUrl,
        current,
        previous,
        next,
        recommendations,
        elapsedMs);
  }

  private Deque<Long> buildNextQueue(Long trackId, Deque<Long> previousTracks) {
    Set<Long> excluded = new HashSet<>();
    excluded.add(trackId);
    if (previousTracks != null) {
      excluded.addAll(previousTracks);
    }
    List<Long> recommendations = computeRecommendationIds(trackId, excluded);
    return new ArrayDeque<>(recommendations);
  }

  private List<Long> computeRecommendationIds(Long trackId, Set<Long> excludedIds) {
    return trackService.getTrackById(trackId)
        .map(track -> {
          List<TrackDTO> candidates = new ArrayList<>();

          if (track.getGenre() != null && !track.getGenre().isBlank()) {
            candidates.addAll(trackService.getTracksByGenre(track.getGenre()));
          }

          if (track.getArtist() != null && !track.getArtist().isBlank()) {
            candidates.addAll(trackService.getTracksByArtist(track.getArtist()));
          }

          if (candidates.size() < RECOMMENDATION_LIMIT) {
            candidates.addAll(trackService.getAllTracks());
          }

          return candidates.stream()
              .map(TrackDTO::getId)
              .filter(Objects::nonNull)
              .filter(id -> !excludedIds.contains(id))
              .distinct()
              .limit(RECOMMENDATION_LIMIT)
              .collect(Collectors.toList());
        })
        .orElseGet(Collections::emptyList);
  }

  private void pushPreviousTrack(Deque<Long> previousStack, Long trackId) {
    if (trackId == null) {
      return;
    }
    previousStack.removeIf(id -> Objects.equals(id, trackId));
    previousStack.offerLast(trackId);
    while (previousStack.size() > MAX_PREVIOUS_TRACKS) {
      previousStack.pollFirst();
    }
  }

  private void markHistoryAsIncomplete(Long historyId) {
    if (historyId == null) {
      return;
    }
    historyRepository.findById(historyId).ifPresent(history -> {
      history.setCompleted(false);
      historyRepository.save(history);
    });
  }

  private void markHistoryAsCompleted(Long historyId) {
    if (historyId == null) {
      return;
    }
    historyRepository.findById(historyId).ifPresent(history -> {
      history.setCompleted(true);
      historyRepository.save(history);
    });
  }

  private void recordTimeToPlay(long nanos, String trigger) {
    meterRegistry.timer("musify.playback.time_to_play", "trigger", trigger)
        .record(nanos, TimeUnit.NANOSECONDS);
  }

  /**
   * Estado interno de una sesión de reproducción activa.
   */
  private static class ActivePlayback {
    private Long historyId;
    private Long currentTrackId;
    private boolean paused;
    private final Deque<Long> previousTrackStack;
    private Deque<Long> nextTrackQueue;

    ActivePlayback(Long historyId, Long currentTrackId, Deque<Long> previousTrackStack, Deque<Long> nextTrackQueue) {
      this.historyId = historyId;
      this.currentTrackId = currentTrackId;
      this.previousTrackStack = previousTrackStack != null ? new ArrayDeque<>(previousTrackStack) : new ArrayDeque<>();
      this.nextTrackQueue = nextTrackQueue != null ? new ArrayDeque<>(nextTrackQueue) : new ArrayDeque<>();
      this.paused = false;
    }

    public Long getHistoryId() {
      return historyId;
    }

    public void setHistoryId(Long historyId) {
      this.historyId = historyId;
    }

    public Long getCurrentTrackId() {
      return currentTrackId;
    }

    public void setCurrentTrackId(Long currentTrackId) {
      this.currentTrackId = currentTrackId;
    }

    public boolean isPaused() {
      return paused;
    }

    public void setPaused(boolean paused) {
      this.paused = paused;
    }

    public Deque<Long> getPreviousTrackStack() {
      return previousTrackStack;
    }

    public Deque<Long> getNextTrackQueue() {
      return nextTrackQueue;
    }

    public void setNextTrackQueue(Deque<Long> nextTrackQueue) {
      this.nextTrackQueue = nextTrackQueue != null ? new ArrayDeque<>(nextTrackQueue) : new ArrayDeque<>();
    }
  }
}