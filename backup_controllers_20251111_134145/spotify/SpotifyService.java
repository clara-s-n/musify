package com.tfu.backend.spotify;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
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
      System.out.println("Token still valid, skipping refresh");
      return; // Token still valid for more than a minute
    }

    System.out.println("Fetching new Spotify access token with client ID: " + clientId);

    try {
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
        System.out.println("Successfully obtained Spotify access token. Expires in: "
            + response.getBody().getExpiresIn() + " seconds");
      } else {
        System.err.println("Error: Spotify token response body is null");
      }
    } catch (Exception e) {
      System.err.println("Error fetching Spotify token: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "getRandomTracksFallback")
  @Cacheable(value = "randomTracks", key = "#limit")
  public List<SpotifyTrackDto> getRandomTracks(int limit) {
    System.out.println("Getting random tracks from Spotify, limit: " + limit);

    try {
      HttpHeaders headers = getAuthHeaders();
      HttpEntity<String> entity = new HttpEntity<>(headers);

      String url = "https://api.spotify.com/v1/browse/new-releases?limit=" + limit;
      System.out.println("Making request to: " + url);

      ResponseEntity<SpotifyNewReleasesResponse> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          SpotifyNewReleasesResponse.class);

      System.out.println("Response status: " + response.getStatusCode());

      if (response.getBody() != null) {
        System.out.println("Response body received");

        if (response.getBody().getAlbums() != null) {
          System.out.println("Albums found: " + response.getBody().getAlbums().getTotal());

          List<SpotifyTrackDto> tracks = new ArrayList<>();

          // Convert albums to DTOs
          if (response.getBody().getAlbums().getItems() != null) {
            for (SpotifyAlbum album : response.getBody().getAlbums().getItems()) {
              System.out.println("Processing album: " + album.getName());

              // Create a track DTO from album data (since the API doesn't actually return
              // tracks)
              String artistNames = album.getArtists().stream()
                  .map(SpotifyArtist::getName)
                  .collect(Collectors.joining(", "));

              String imageUrl = null;
              if (album.getImages() != null && !album.getImages().isEmpty()) {
                imageUrl = album.getImages().get(0).getUrl();
              }

              SpotifyTrackDto trackDto = new SpotifyTrackDto(
                  album.getId(),
                  album.getName(),
                  artistNames,
                  album.getName(), // Album name is same as track name for this simplification
                  imageUrl,
                  null // No preview URL available from this endpoint
              );

              tracks.add(trackDto);

              if (tracks.size() >= limit) {
                break;
              }
            }
          }

          System.out.println("Returning " + tracks.size() + " tracks");
          return tracks;
        } else {
          System.out.println("No albums found in response");
        }
      } else {
        System.out.println("Response body is null");
      }
    } catch (Exception e) {
      System.err.println("Error getting random tracks: " + e.getMessage());
      e.printStackTrace();
    }

    return Collections.emptyList();
  }

  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "searchTracksFallback")
  @Cacheable(value = "searchTracks", key = "#query + '_' + #limit")
  public List<SpotifyTrackDto> searchTracks(String query, int limit) {
    HttpHeaders headers = getAuthHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
        "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=" + limit,
        HttpMethod.GET,
        entity,
        SpotifySearchResponse.class);

    if (response.getBody() != null && response.getBody().getTracks() != null) {
      System.out.println("Search response received: " + response.getBody());
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

  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "getTrackPlaybackFallback")
  @Cacheable(value = "trackPlayback", key = "#trackId")
  public SpotifyPlaybackResponse getTrackPlayback(String trackId) {
    System.out.println("Getting playback data for track: " + trackId);

    try {
      HttpHeaders headers = getAuthHeaders();
      HttpEntity<String> entity = new HttpEntity<>(headers);

      // Primero, obtenemos los datos completos de la canción
      String url = "https://api.spotify.com/v1/tracks/" + trackId;
      System.out.println("Making request to: " + url);

      ResponseEntity<SpotifyTrack> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          SpotifyTrack.class);

      System.out.println("Response status: " + response.getStatusCode());

      if (response.getBody() != null) {
        SpotifyTrack track = response.getBody();

        // Convertimos los datos a nuestra respuesta de reproducción
        SpotifyPlaybackResponse playbackResponse = new SpotifyPlaybackResponse();
        playbackResponse.setTrackId(track.getId());
        playbackResponse.setName(track.getName());

        // Obtenemos los nombres de los artistas
        if (track.getArtists() != null) {
          String artistsString = track.getArtists().stream()
              .map(SpotifyArtist::getName)
              .collect(Collectors.joining(", "));
          playbackResponse.setArtists(artistsString);
        }

        // Información del álbum
        if (track.getAlbum() != null) {
          playbackResponse.setAlbum(track.getAlbum().getName());

          // Imagen del álbum
          if (track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
            playbackResponse.setImageUrl(track.getAlbum().getImages().get(0).getUrl());
          }
        }

        // URL de previsualización
        playbackResponse.setPreviewUrl(track.getPreviewUrl());

        // Por defecto, usamos la URL de previsualización como URL de streaming
        // En un caso real, aquí implementaríamos la lógica para generar una URL de
        // streaming
        // basada en acuerdos con Spotify o utilizando su SDK para Web Playback
        playbackResponse.setStreamUrl(track.getPreviewUrl());

        // Indicamos si es reproducible (si tiene URL de previsualización)
        playbackResponse.setIsPlayable(track.getPreviewUrl() != null);

        System.out.println("Playback data retrieved successfully for track: " + track.getName());
        return playbackResponse;
      }
    } catch (Exception e) {
      System.err.println("Error getting track playback data: " + e.getMessage());
      e.printStackTrace();
    }

    return null;
  }

  // Fallback method for track playback
  public SpotifyPlaybackResponse getTrackPlaybackFallback(String trackId, Throwable t) {
    System.err.println("Fallback for track playback. Error: " + t.getMessage());
    // Podríamos devolver datos en caché o una respuesta genérica
    return null;
  }

  /**
   * Evicts all cache entries periodically to prevent stale data.
   * Runs every 10 minutes.
   */
  @CacheEvict(value = { "randomTracks", "searchTracks", "trackPlayback" }, allEntries = true)
  @Scheduled(fixedRate = 600000) // Every 10 minutes
  public void evictAllCaches() {
    System.out.println("Evicting all Spotify caches to refresh data");
  }
}
