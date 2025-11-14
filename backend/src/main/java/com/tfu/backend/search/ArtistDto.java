package com.tfu.backend.search;

import lombok.Data;

/**
 * DTO para artista en búsqueda categorizada
 */
@Data
public class ArtistDto {
    private String id;
    private String name;
    private String imageUrl;
    private String genres;
    private int followers;
    
    public ArtistDto() {}
    
    public ArtistDto(String id, String name, String imageUrl, String genres, int followers) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.genres = genres;
        this.followers = followers;
    }
}