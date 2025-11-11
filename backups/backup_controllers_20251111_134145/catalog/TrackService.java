package com.tfu.backend.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de pistas musicales.
 */
@Service
public class TrackService {
  private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

  private final TrackRepository trackRepository;

  /**
   * Constructor que inyecta el repositorio de pistas.
   * 
   * @param trackRepository Repositorio para acceso a datos de pistas
   */
  public TrackService(TrackRepository trackRepository) {
    this.trackRepository = trackRepository;
  }

  /**
   * Obtiene todas las pistas.
   * 
   * @return Lista de DTOs de pistas
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> getAllTracks() {
    logger.debug("Obteniendo todas las pistas");
    return trackRepository.findAll().stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene una pista por su ID.
   * 
   * @param id ID de la pista a buscar
   * @return DTO de la pista encontrada o vacío si no existe
   */
  @Transactional(readOnly = true)
  public Optional<TrackDTO> getTrackById(Long id) {
    logger.debug("Obteniendo pista con ID: {}", id);
    return trackRepository.findById(id)
        .map(TrackDTO::fromEntity);
  }

  /**
   * Busca pistas por título, artista o álbum.
   * 
   * @param query Texto a buscar
   * @return Lista de DTOs de pistas que coinciden con la consulta
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> searchTracks(String query) {
    logger.debug("Buscando pistas que coincidan con: {}", query);
    return trackRepository.search(query).stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene pistas por género.
   * 
   * @param genre Género a buscar
   * @return Lista de DTOs de pistas del género especificado
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> getTracksByGenre(String genre) {
    logger.debug("Obteniendo pistas del género: {}", genre);
    return trackRepository.findByGenreIgnoreCase(genre).stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene pistas por año de lanzamiento.
   * 
   * @param year Año de lanzamiento a buscar
   * @return Lista de DTOs de pistas del año especificado
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> getTracksByYear(Integer year) {
    logger.debug("Obteniendo pistas del año: {}", year);
    return trackRepository.findByReleaseYear(year).stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene pistas por artista.
   * 
   * @param artist Artista a buscar
   * @return Lista de DTOs de pistas del artista especificado
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> getTracksByArtist(String artist) {
    logger.debug("Obteniendo pistas del artista: {}", artist);
    return trackRepository.findByArtistContainingIgnoreCase(artist).stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Obtiene pistas filtradas por tipo premium o gratuito.
   * 
   * @param premium True para pistas premium, false para gratuitas
   * @return Lista de DTOs de pistas según el filtro
   */
  @Transactional(readOnly = true)
  public List<TrackDTO> getTracksByPremium(boolean premium) {
    logger.debug("Obteniendo pistas {}", premium ? "premium" : "gratuitas");
    return (premium ? trackRepository.findByPremiumTrue() : trackRepository.findByPremiumFalse())
        .stream()
        .map(TrackDTO::fromEntity)
        .collect(Collectors.toList());
  }
}