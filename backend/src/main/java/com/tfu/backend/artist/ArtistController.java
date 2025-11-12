package com.tfu.backend.artist;

import com.tfu.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones relacionadas con artistas
 */
@Tag(name = "Artist Management", description = "API para obtener información detallada de artistas y sus canciones")
@RestController
@RequestMapping("/api/artists")
@Validated
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @Operation(
        summary = "Obtener canciones de un artista", 
        description = "Devuelve las canciones de un artista específico con paginación. Incluye información del artista y paginación completa."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Canciones del artista obtenidas correctamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArtistTracksResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Artista no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Parámetros inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{artistId}/tracks")
    public ResponseEntity<ApiResponse<ArtistTracksResponse>> getArtistTracks(
            @PathVariable 
            @Parameter(description = "ID de Spotify del artista", example = "4Z8W4fKeB5YxbusRsdQVPb") 
            String artistId,
            
            @RequestParam(defaultValue = "0") 
            @Min(0) 
            @Parameter(description = "Número de página (comenzando desde 0)", example = "0") 
            int page,
            
            @RequestParam(defaultValue = "20") 
            @Min(1) @Max(50) 
            @Parameter(description = "Número de canciones por página (1-50)", example = "20") 
            int size
    ) {
        
        ArtistTracksResponse response = artistService.getArtistTracks(artistId, page, size);
        
        if (response.getTracks().isEmpty() && page == 0) {
            return ResponseEntity.ok(ApiResponse.success(response, 
                "No se encontraron canciones para este artista"));
        }
        
        String message = String.format(
            "Se encontraron %d canciones del artista '%s' (página %d de %d)", 
            response.getTracks().size(),
            response.getArtistName(),
            response.getPagination().getCurrentPage() + 1,
            response.getPagination().getTotalPages()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(
        summary = "Obtener top tracks de un artista", 
        description = "Devuelve las canciones más populares de un artista (máximo 10 tracks)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Top tracks obtenidos correctamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Artista no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{artistId}/top-tracks")
    public ResponseEntity<ApiResponse<ArtistTracksResponse>> getArtistTopTracks(
            @PathVariable 
            @Parameter(description = "ID de Spotify del artista") 
            String artistId
    ) {
        
        // Para top tracks, siempre devolvemos la primera página con un máximo de 10 canciones
        ArtistTracksResponse response = artistService.getArtistTracks(artistId, 0, 10);
        
        String message = String.format(
            "Top %d canciones del artista '%s'", 
            response.getTracks().size(),
            response.getArtistName()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }
}