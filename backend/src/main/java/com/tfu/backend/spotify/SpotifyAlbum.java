package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyAlbum {
    private String id;
    private String name;
    private List<SpotifyImage> images;
    private SpotifyPagingObject<SpotifyTrack> tracks;
}