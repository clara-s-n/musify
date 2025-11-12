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
import java.util.Random;
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
  @Cacheable(value = "randomTracks", key = "#limit + '_' + T(java.time.LocalDateTime).now().getMinute() / 5")
  public List<SpotifyTrackDto> getRandomTracks(int limit) {
    System.out.println("Getting random tracks from Spotify, limit: " + limit);
    
    // Lista de queries aleatorias para obtener variedad
    String[] randomQueries = {
      "pop", "rock", "jazz", "electronic", "hip hop", "indie", "alternative", 
      "latin", "reggaeton", "salsa", "bachata", "cumbia", "folk", "blues",
      "classical", "ambient", "funk", "soul", "r&b", "country", "metal"
    };
    
    // Seleccionar query aleatoria
    String randomQuery = randomQueries[(int) (Math.random() * randomQueries.length)];
    
    // Usar diferentes años para más variedad
    int[] years = {2020, 2021, 2022, 2023, 2024};
    int randomYear = years[(int) (Math.random() * years.length)];

    try {
      HttpHeaders headers = getAuthHeaders();
      HttpEntity<String> entity = new HttpEntity<>(headers);

      // Usar search con query aleatoria en lugar de new-releases
      String url = String.format(
        "https://api.spotify.com/v1/search?q=genre:%s year:%d&type=track&limit=%d&offset=%d",
        randomQuery, randomYear, Math.min(limit * 2, 50), (int) (Math.random() * 100)
      );
      System.out.println("Making request to: " + url);

      ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          SpotifySearchResponse.class);

      System.out.println("Response status: " + response.getStatusCode());

      if (response.getBody() != null && response.getBody().getTracks() != null) {
        System.out.println("Response body received");
        
        List<SpotifyTrack> spotifyTracks = response.getBody().getTracks().getItems();
        System.out.println("Tracks found: " + spotifyTracks.size());

        if (!spotifyTracks.isEmpty()) {
          List<SpotifyTrackDto> tracks = spotifyTracks.stream()
              .map(this::convertToDto)
              .collect(Collectors.toList());

          // Randomizar la lista resultante
          Collections.shuffle(tracks);
          
          // Limitar al número solicitado
          if (tracks.size() > limit) {
            tracks = tracks.subList(0, limit);
          }

          System.out.println("Returning " + tracks.size() + " randomized tracks");
          return tracks;
        } else {
          System.out.println("No tracks found in response");
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
    System.out.println("Searching tracks for: " + query + " with limit: " + limit);
    
    HttpHeaders headers = getAuthHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    // Limpiar y preparar la query
    String cleanQuery = query.trim().replaceAll("[\"'`]", "");
    
    // Crear una búsqueda más simple y efectiva
    String searchUrl = String.format(
      "https://api.spotify.com/v1/search?q=%s&type=track&limit=%d&market=US",
      java.net.URLEncoder.encode(cleanQuery, java.nio.charset.StandardCharsets.UTF_8),
      limit
    );
    
    System.out.println("Making search request to: " + searchUrl);

    ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
        searchUrl,
        HttpMethod.GET,
        entity,
        SpotifySearchResponse.class);

    if (response.getBody() != null && response.getBody().getTracks() != null) {
      List<SpotifyTrack> tracks = response.getBody().getTracks().getItems();
      System.out.println("Found " + tracks.size() + " tracks for query: " + query);
      
      return tracks.stream()
          .map(this::convertToDto)
          .collect(Collectors.toList());
    }

    System.out.println("No tracks found for query: " + query);
    return Collections.emptyList();
  }

  /**
   * Alternative method to get truly random tracks using different strategies
   */
  @Retry(name = "spotifyApi")
  @CircuitBreaker(name = "spotifyApi", fallbackMethod = "getTrulyRandomTracksFallback")
  public List<SpotifyTrackDto> getTrulyRandomTracks(int limit) {
    System.out.println("Getting truly random tracks using multiple strategies, limit: " + limit);
    
    List<SpotifyTrackDto> allTracks = new ArrayList<>();
    Random random = new Random();
    
    // Strategy 1: Random search queries
    String[] randomWords = {
      "love", "night", "day", "light", "heart", "time", "life", "dream", "feel", "way",
      "dance", "music", "song", "beat", "rhythm", "soul", "fire", "water", "sun", "moon"
    };
    
    try {
      for (int i = 0; i < 3; i++) { // Try 3 different random searches
        String randomWord = randomWords[random.nextInt(randomWords.length)];
        int randomOffset = random.nextInt(100);
        
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String url = String.format(
          "https://api.spotify.com/v1/search?q=%s&type=track&limit=20&offset=%d",
          randomWord, randomOffset
        );
        
        ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, SpotifySearchResponse.class);
        
        if (response.getBody() != null && response.getBody().getTracks() != null) {
          List<SpotifyTrack> tracks = response.getBody().getTracks().getItems();
          allTracks.addAll(tracks.stream()
              .map(this::convertToDto)
              .collect(Collectors.toList()));
        }
      }
      
      // Shuffle all collected tracks
      Collections.shuffle(allTracks);
      
      // Remove duplicates based on track ID
      List<SpotifyTrackDto> uniqueTracks = allTracks.stream()
          .collect(Collectors.toMap(
              SpotifyTrackDto::getId,
              track -> track,
              (existing, replacement) -> existing))
          .values()
          .stream()
          .collect(Collectors.toList());
      
      // Shuffle again and limit
      Collections.shuffle(uniqueTracks);
      
      if (uniqueTracks.size() > limit) {
        uniqueTracks = uniqueTracks.subList(0, limit);
      }
      
      System.out.println("Returning " + uniqueTracks.size() + " truly random unique tracks");
      return uniqueTracks;
      
    } catch (Exception e) {
      System.err.println("Error getting truly random tracks: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  // Fallback methods
  public List<SpotifyTrackDto> getRandomTracksFallback(int limit, Throwable t) {
    System.out.println("Random tracks fallback triggered: " + t.getMessage());
    // Try the alternative method as fallback
    try {
      return getTrulyRandomTracks(limit);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
  
  public List<SpotifyTrackDto> getTrulyRandomTracksFallback(int limit, Throwable t) {
    System.out.println("Truly random tracks fallback triggered: " + t.getMessage());
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
    String primaryArtistId = null;
    String primaryArtistName = null;
    String primaryArtistSpotifyUrl = null;
    
    if (track.getArtists() != null && !track.getArtists().isEmpty()) {
      artistsString = track.getArtists().stream()
          .map(SpotifyArtist::getName)
          .collect(Collectors.joining(", "));
      
      // Obtener información del artista principal (primero en la lista)
      SpotifyArtist primaryArtist = track.getArtists().get(0);
      primaryArtistId = primaryArtist.getId();
      primaryArtistName = primaryArtist.getName();
      primaryArtistSpotifyUrl = primaryArtist.getExternalUrls() != null 
          ? primaryArtist.getExternalUrls().get("spotify") 
          : null;
    }

    String albumName = "";
    String imageUrl = null;
    if (track.getAlbum() != null) {
      albumName = track.getAlbum().getName();
      if (track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
        imageUrl = track.getAlbum().getImages().get(0).getUrl();
      }
    }

    SpotifyTrackDto dto = new SpotifyTrackDto(
        track.getId(),
        track.getName(),
        artistsString,
        albumName,
        imageUrl,
        track.getPreviewUrl());
    
    // Establecer información del artista principal
    dto.setPrimaryArtistId(primaryArtistId);
    dto.setPrimaryArtistName(primaryArtistName);
    dto.setPrimaryArtistSpotifyUrl(primaryArtistSpotifyUrl);
    
    return dto;
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
   * Runs every 2 minutes for randomTracks to ensure freshness.
   */
  @CacheEvict(value = { "randomTracks" }, allEntries = true)
  @Scheduled(fixedRate = 120000) // Every 2 minutes for random tracks
  public void evictRandomTracksCache() {
    System.out.println("Evicting randomTracks cache to ensure fresh random results");
  }
  
  /**
   * Evicts search cache less frequently as searches are more predictable.
   */
  @CacheEvict(value = { "searchTracks", "trackPlayback" }, allEntries = true)
  @Scheduled(fixedRate = 600000) // Every 10 minutes for search results
  public void evictSearchCache() {
    System.out.println("Evicting search and playback caches to refresh data");
  }
}
