package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyArtist {
    private String id;
    private String name;
    
    @JsonProperty("external_urls")
    private Map<String, String> externalUrls;
    
    // Constructor para retrocompatibilidad
    public SpotifyArtist(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
