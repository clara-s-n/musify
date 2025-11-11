package com.tfu.backend.catalog;

import com.tfu.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión y búsqueda de pistas musicales.
 */
@Tag(name = "Catálogo", description = "API para gestión y búsqueda del catálogo musical")
@RestController
@RequestMapping("/tracks")
public class TrackController {
  private static final Logger logger = LoggerFactory.getLogger(TrackController.class);

  private final TrackService trackService;

  /**
   * Constructor que inyecta el servicio de pistas.
   * 
   * @param trackService Servicio para operaciones con pistas musicales
   */
  public TrackController(TrackService trackService) {
    this.trackService = trackService;
  }

  /**
   * Obtiene todas las pistas.
   * 
   * @return Lista de todas las pistas
   */
  @Operation(summary = "Listar todas las pistas", description = "Obtiene una lista de todas las pistas disponibles en el catálogo", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de pistas obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrackDTO.class)))
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<TrackDTO>>> getAllTracks() {
    logger.debug("Solicitud para obtener todas las pistas");
    List<TrackDTO> tracks = trackService.getAllTracks();
    return ResponseEntity.ok(ApiResponse.success(tracks,
        "Se encontraron " + tracks.size() + " pistas"));
  }

  /**
   * Obtiene una pista por su ID.
   * 
   * @param id ID de la pista a buscar
   * @return Pista encontrada o error 404 si no existe
   */
  @Operation(summary = "Obtener pista por ID", description = "Busca y devuelve una pista específica por su ID", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pista encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrackDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pista no encontrada", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<TrackDTO>> getTrackById(
      @Parameter(description = "ID de la pista a buscar") @PathVariable Long id) {
    logger.debug("Solicitud para obtener pista con ID: {}", id);
    return trackService.getTrackById(id)
        .map(track -> ResponseEntity.ok(ApiResponse.success(track, "Pista encontrada")))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Pista no encontrada", "No existe una pista con ID: " + id)));
  }

  /**
   * Busca pistas por título, artista o álbum.
   * 
   * @param q Consulta de búsqueda
   * @return Lista de pistas que coinciden con la consulta
   */
  @Operation(summary = "Buscar pistas", description = "Busca pistas por título, artista o álbum", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrackDTO.class)))
  })
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<TrackDTO>>> searchTracks(
      @Parameter(description = "Texto a buscar en título, artista o álbum") @RequestParam String q) {
    logger.debug("Solicitud para buscar pistas con consulta: {}", q);
    List<TrackDTO> results = trackService.searchTracks(q);
    return ResponseEntity.ok(ApiResponse.success(results,
        "Se encontraron " + results.size() + " pistas"));
  }

  /**
   * Obtiene pistas por género.
   * 
   * @param genre Género a buscar
   * @return Lista de pistas del género especificado
   */
  @GetMapping("/by-genre")
  public ResponseEntity<ApiResponse<List<TrackDTO>>> getTracksByGenre(@RequestParam String genre) {
    logger.debug("Solicitud para obtener pistas del género: {}", genre);
    List<TrackDTO> tracks = trackService.getTracksByGenre(genre);
    return ResponseEntity.ok(ApiResponse.success(tracks,
        "Se encontraron " + tracks.size() + " pistas del género: " + genre));
  }

  /**
   * Obtiene pistas por año de lanzamiento.
   * 
   * @param year Año de lanzamiento a buscar
   * @return Lista de pistas del año especificado
   */
  @GetMapping("/by-year")
  public ResponseEntity<ApiResponse<List<TrackDTO>>> getTracksByYear(@RequestParam Integer year) {
    logger.debug("Solicitud para obtener pistas del año: {}", year);
    List<TrackDTO> tracks = trackService.getTracksByYear(year);
    return ResponseEntity.ok(ApiResponse.success(tracks,
        "Se encontraron " + tracks.size() + " pistas del año: " + year));
  }

  /**
   * Obtiene pistas por artista.
   * 
   * @param artist Artista a buscar
   * @return Lista de pistas del artista especificado
   */
  @GetMapping("/by-artist")
  public ResponseEntity<ApiResponse<List<TrackDTO>>> getTracksByArtist(@RequestParam String artist) {
    logger.debug("Solicitud para obtener pistas del artista: {}", artist);
    List<TrackDTO> tracks = trackService.getTracksByArtist(artist);
    return ResponseEntity.ok(ApiResponse.success(tracks,
        "Se encontraron " + tracks.size() + " pistas del artista: " + artist));
  }

  /**
   * Obtiene pistas filtradas por tipo premium o gratuito.
   * 
   * @param premium True para pistas premium, false para gratuitas
   * @return Lista de pistas según el filtro
   */
  @GetMapping("/premium")
  public ResponseEntity<ApiResponse<List<TrackDTO>>> getTracksByPremium(@RequestParam boolean premium) {
    logger.debug("Solicitud para obtener pistas {}", premium ? "premium" : "gratuitas");
    List<TrackDTO> tracks = trackService.getTracksByPremium(premium);
    return ResponseEntity.ok(ApiResponse.success(tracks,
        "Se encontraron " + tracks.size() + " pistas " + (premium ? "premium" : "gratuitas")));
  }
}
