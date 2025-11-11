package com.tfu.backend.search;

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

/**
 * Controlador para búsqueda categorizada
 */
@Tag(name = "Categorized Search", description = "API para búsqueda categorizada que devuelve resultados por tipo")
@RestController
@RequestMapping("/api/search")
@Validated
public class CategorizedSearchController {
    
    private final CategorizedSearchService searchService;
    
    public CategorizedSearchController(CategorizedSearchService searchService) {
        this.searchService = searchService;
    }
    
    @Operation(
        summary = "Búsqueda categorizada", 
        description = "Busca contenido y devuelve resultados organizados por categorías: songs, albums, artists, concerts"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Búsqueda categorizada completada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorizedSearchResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Parámetros de búsqueda inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CategorizedSearchResponse>> searchCategorized(
        @Parameter(description = "Término de búsqueda", required = true, example = "jazz")
        @RequestParam 
        @Size(min = 1, max = 100, message = "La consulta debe tener entre 1 y 100 caracteres") 
        String q,
        
        @Parameter(description = "Límite de resultados por categoría", example = "5")
        @RequestParam(defaultValue = "5") 
        @Min(1) @Max(20) 
        int limit
    ) {
        CategorizedSearchResponse results = searchService.searchCategorized(q, limit);
        
        return ResponseEntity.ok(ApiResponse.success(
            results, 
            String.format("Búsqueda categorizada completada para '%s'", q)
        ));
    }
}