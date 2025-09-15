package com.tfu.backend.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para búsqueda de pistas musicales.
 */
@RestController
@RequestMapping("/tracks")
public class TrackController {
  /** Lista simulada de todas las canciones disponibles */
  private static final List<String> ALL = List.of("Song A", "Song B", "Song C");

  /**
   * Endpoint para buscar canciones por nombre.
   * 
   * @param q Consulta de búsqueda
   * @return Lista de canciones que coinciden con la consulta
   */
  @GetMapping
  public ResponseEntity<List<String>> search(@RequestParam String q) {
    return ResponseEntity.ok(ALL.stream().filter(s -> s.toLowerCase().contains(q.toLowerCase())).toList());
  }
}
