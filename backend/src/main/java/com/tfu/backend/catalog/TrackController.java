package com.tfu.backend.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {
  private static final List<String> ALL = List.of("Song A", "Song B", "Song C");

  @GetMapping
  public ResponseEntity<List<String>> search(@RequestParam String q) {
    return ResponseEntity.ok(ALL.stream().filter(s -> s.toLowerCase().contains(q.toLowerCase())).toList());
  }
}
