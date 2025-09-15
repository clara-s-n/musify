package com.tfu.backend.playback;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente para comunicarse con el servicio de streaming externo
 * (flaky-service).
 */
@Component
public class StreamClient {
  /**
   * Cliente HTTP configurado para comunicarse con el servicio de streaming.
   */
  private final RestClient http = RestClient.builder().baseUrl("http://flaky-service:9090").build();

  /**
   * Obtiene la URL de streaming para una pista específica.
   * 
   * @param trackId ID de la pista.
   * @return URL de streaming como String.
   */
  public String getStreamUrl(String trackId) {
    return http.get().uri("/source?trackId={id}", trackId).retrieve().body(String.class);
  }
}
