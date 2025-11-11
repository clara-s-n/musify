package com.tfu.backend.search;

import lombok.Data;

/**
 * DTO para álbum en búsqueda categorizada
 */
@Data
public class AlbumDto {
    private String id;
    private String name;
    private String artist;
    private String imageUrl;
    private String releaseDate;
    private int totalTracks;
    
    public AlbumDto() {}
    
    public AlbumDto(String id, String name, String artist, String imageUrl, String releaseDate, int totalTracks) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.releaseDate = releaseDate;
        this.totalTracks = totalTracks;
    }
}