package com.tfu.backend.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpotifyTokenResponse {
  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("expires_in")
  private int expiresIn;

  // Getters and setters
}
