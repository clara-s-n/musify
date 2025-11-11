package com.tfu.backend.playback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para operaciones de acceso a datos del historial de
 * reproducciones.
 */
@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, Long> {

  /**
   * Obtiene el historial de reproducciones de un usuario específico.
   * 
   * @param userId   ID del usuario
   * @param pageable Configuración de paginación
   * @return Página con el historial de reproducciones
   */
  Page<PlaybackHistory> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

  /**
   * Obtiene las reproducciones de una pista específica.
   * 
   * @param trackId ID de la pista
   * @return Lista de reproducciones de la pista
   */
  List<PlaybackHistory> findByTrackIdOrderByTimestampDesc(Long trackId);

  /**
   * Obtiene el historial de reproducciones de un usuario en un rango de fechas.
   * 
   * @param userId ID del usuario
   * @param start  Fecha de inicio
   * @param end    Fecha de fin
   * @return Lista de reproducciones en el rango especificado
   */
  List<PlaybackHistory> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
      Long userId, LocalDateTime start, LocalDateTime end);

  /**
   * Obtiene el último registro de reproducción de un usuario.
   * 
   * @param userId ID del usuario
   * @return Último registro de reproducción o vacío si no hay registros
   */
  PlaybackHistory findFirstByUserIdOrderByTimestampDesc(Long userId);

  /**
   * Obtiene el último registro de reproducción de un usuario para una pista
   * específica.
   * 
   * @param userId  ID del usuario
   * @param trackId ID de la pista
   * @return Último registro de reproducción de la pista o vacío si no hay
   *         registros
   */
  PlaybackHistory findFirstByUserIdAndTrackIdOrderByTimestampDesc(Long userId, Long trackId);
}