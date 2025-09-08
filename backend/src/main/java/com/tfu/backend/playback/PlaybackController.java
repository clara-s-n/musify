package com.tfu.backend.playback;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/playback")
public class PlaybackController {
  private final StreamClient client;

  public PlaybackController(StreamClient client) {
    this.client = client;
  }

  @PostMapping("/start")
  @CircuitBreaker(name = "streamSource", fallbackMethod = "fallbackUrl")
  @Retry(name = "streamSource")
  @TimeLimiter(name = "streamSource")
  public CompletableFuture<ResponseEntity<PlaybackDTO>> start(@RequestParam String trackId) {
    return CompletableFuture.supplyAsync(() -> {
      var url = client.getStreamUrl(trackId);
      return ResponseEntity.ok(new PlaybackDTO(url));
    });
  }

  public CompletableFuture<ResponseEntity<PlaybackDTO>> fallbackUrl(String trackId, Throwable t) {
    return CompletableFuture.completedFuture(
        ResponseEntity.ok(new PlaybackDTO("https://cdn.example/low-bitrate/" + trackId)));
  }
}

record PlaybackDTO(String url) {
}
