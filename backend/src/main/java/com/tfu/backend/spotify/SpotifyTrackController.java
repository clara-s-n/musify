package com.tfu.backend.spotify;

import com.tfu.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Spotify Music", description = "API para búsqueda y descubrimiento de música a través de Spotify")
@RestController
@RequestMapping("/music/spotify")
@Validated
public class SpotifyTrackController {
  private final SpotifyService spotifyService;

  public SpotifyTrackController(SpotifyService spotifyService) {
    this.spotifyService = spotifyService;
  }

  @Operation(summary = "Obtener canciones aleatorias", description = "Devuelve un listado de nuevos lanzamientos o canciones recomendadas de Spotify")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Canciones obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpotifyTrackListResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al obtener canciones", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/random")
  public ResponseEntity<ApiResponse<List<SpotifyTrackDto>>> getRandomTracks(
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
    List<SpotifyTrackDto> tracks = spotifyService.getRandomTracks(limit);
    return ResponseEntity.ok(ApiResponse.success(tracks, "Canciones aleatorias obtenidas correctamente"));
  }

  @Operation(summary = "Buscar canciones", description = "Busca canciones en Spotify según un término de búsqueda")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpotifyTrackListResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Parámetro de búsqueda inválido", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al buscar canciones", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<SpotifyTrackDto>>> searchTracks(
      @RequestParam @Size(min = 1, max = 100) String q,
      @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
    List<SpotifyTrackDto> tracks = spotifyService.searchTracks(q, limit);
    return ResponseEntity.ok(ApiResponse.success(tracks, "Búsqueda realizada correctamente"));
  }
}
