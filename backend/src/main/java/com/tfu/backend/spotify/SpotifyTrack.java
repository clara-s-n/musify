package com.tfu.backend.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyTrack {
    private String id;
    private String name;
    private List<SpotifyArtist> artists;
    private SpotifyAlbum album;
    
    @JsonProperty("preview_url")
    private String previewUrl;
}
