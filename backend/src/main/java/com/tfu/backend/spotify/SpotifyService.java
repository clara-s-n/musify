package com.tfu.backend.spotify;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpotifyService {
  @Value("${spotify.client.id}")
  private String clientId;

  @Value("${spotify.client.secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;
  private String accessToken;
  private long tokenExpiration = 0;

  public SpotifyService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @PostConstruct
  @Scheduled(fixedRate = 3000000) // Refresh token every 50 minutes (token valid for 1 hour)
  public void fetchAccessToken() {
    if (System.currentTimeMillis() < tokenExpiration - 60000) {
      return; // Token still valid for more than a minute
    }

    String authHeader = "Basic " + Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Authorization", authHeader);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(
        "https://accounts.spotify.com/api/token",
        HttpMethod.POST,
        request,
        SpotifyTokenResponse.class);

    if (response.getBody() != null) {
      this.accessToken = response.getBody().getAccessToken();
      this.tokenExpiration = System.currentTimeMillis() +
          (response.getBody().getExpiresIn() * 1000);
    }
  }

  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "getRandomTracksFallback")
  public List<SpotifyTrackDto> getRandomTracks(int limit) {
    HttpHeaders headers = getAuthHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<SpotifyNewReleasesResponse> response = restTemplate.exchange(
        "https://api.spotify.com/v1/browse/new-releases?limit=" + limit,
        HttpMethod.GET,
        entity,
        SpotifyNewReleasesResponse.class);

    if (response.getBody() != null && response.getBody().getAlbums() != null) {
      return response.getBody().getAlbums().getItems().stream()
          .filter(album -> album.getTracks() != null)
          .flatMap(album -> album.getTracks().getItems().stream())
          .limit(limit)
          .map(this::convertToDto)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "searchTracksFallback")
  public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    HttpHeaders headers = getAuthHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
        "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=" + limit,
        HttpMethod.GET,
        entity,
        SpotifySearchResponse.class);

    if (response.getBody() != null && response.getBody().getTracks() != null) {
      return response.getBody().getTracks().getItems().stream()
          .map(this::convertToDto)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  // Fallback methods
  public List<SpotifyTrackDto> getRandomTracksFallback(int limit, Throwable t) {
    // Return empty list or cached data
    return Collections.emptyList();
  }

  public List<SpotifyTrackDto> searchTracksFallback(String query, int limit, Throwable t) {
    // Return empty list or cached data
    return Collections.emptyList();
  }

  private HttpHeaders getAuthHeaders() {
    if (System.currentTimeMillis() > tokenExpiration - 60000) {
      fetchAccessToken();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    return headers;
  }

  private SpotifyTrackDto convertToDto(SpotifyTrack track) {
    // Safe conversion with null checks
    String artistsString = "";
    if (track.getArtists() != null) {
        artistsString = track.getArtists().stream()
            .map(SpotifyArtist::getName)
            .collect(Collectors.joining(", "));
    }
    
    String albumName = "";
    String imageUrl = null;
    if (track.getAlbum() != null) {
        albumName = track.getAlbum().getName();
        if (track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
            imageUrl = track.getAlbum().getImages().get(0).getUrl();
        }
    }
    
    return new SpotifyTrackDto(
        track.getId(),
        track.getName(),
        artistsString,
        albumName,
        imageUrl,
        track.getPreviewUrl());
  }
}
