package com.tfu.backend.search;

import lombok.Data;

/**
 * DTO para concierto en búsqueda categorizada (datos ficticios para demo)
 */
@Data
public class ConcertDto {
    private String id;
    private String name;
    private String artist;
    private String venue;
    private String date;
    private String city;
    private String imageUrl;
    
    public ConcertDto() {}
    
    public ConcertDto(String id, String name, String artist, String venue, String date, String city, String imageUrl) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.venue = venue;
        this.date = date;
        this.city = city;
        this.imageUrl = imageUrl;
    }
}