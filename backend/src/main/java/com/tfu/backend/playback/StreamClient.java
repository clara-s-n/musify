package com.tfu.backend.playback;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StreamClient {
  private final RestClient http = RestClient.builder().baseUrl("http://flaky-service:9090").build();

  public String getStreamUrl(String trackId) {
    return http.get().uri("/source?trackId={id}", trackId).retrieve().body(String.class);
  }
}
