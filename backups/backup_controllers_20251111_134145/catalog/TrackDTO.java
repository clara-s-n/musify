package com.tfu.backend.catalog;

import java.util.Objects;

/**
 * DTO para respuestas de pistas musicales.
 * Contiene los datos básicos de una pista musical para mostrar al cliente.
 */
public class TrackDTO {
  private final Long id;
  private final String title;
  private final String artist;
  private final String album;
  private final Integer releaseYear;
  private final String genre;
  private final Integer durationSeconds;
  private final boolean premium;
  private final String coverUrl;

  private TrackDTO(Long id, String title, String artist, String album,
      Integer releaseYear, String genre, Integer durationSeconds,
      boolean premium, String coverUrl) {
    this.id = id;
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.releaseYear = releaseYear;
    this.genre = genre;
    this.durationSeconds = durationSeconds;
    this.premium = premium;
    this.coverUrl = coverUrl;
  }

  /**
   * Convierte una entidad Track a un DTO.
   * 
   * @param track Entidad Track a convertir
   * @return TrackDTO con los datos de la entidad
   */
  public static TrackDTO fromEntity(Track track) {
    return new TrackDTO(
        track.getId(),
        track.getTitle(),
        track.getArtist(),
        track.getAlbum(),
        track.getReleaseYear(),
        track.getGenre(),
        track.getDurationSeconds(),
        track.isPremium(),
        track.getCoverUrl());
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getArtist() {
    return artist;
  }

  public String getAlbum() {
    return album;
  }

  public Integer getReleaseYear() {
    return releaseYear;
  }

  public String getGenre() {
    return genre;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public boolean isPremium() {
    return premium;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  /**
   * Formatea la duración en formato mm:ss.
   * 
   * @return String con formato mm:ss
   */
  public String getFormattedDuration() {
    if (durationSeconds == null)
      return "--:--";
    int minutes = durationSeconds / 60;
    int seconds = durationSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TrackDTO trackDTO = (TrackDTO) o;
    return premium == trackDTO.premium &&
        Objects.equals(id, trackDTO.id) &&
        Objects.equals(title, trackDTO.title) &&
        Objects.equals(artist, trackDTO.artist);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, artist, premium);
  }

  @Override
  public String toString() {
    return "TrackDTO{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", artist='" + artist + '\'' +
        ", album='" + album + '\'' +
        '}';
  }
}