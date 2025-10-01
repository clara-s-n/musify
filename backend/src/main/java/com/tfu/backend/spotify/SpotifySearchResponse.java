package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifySearchResponse {
    private TracksContainer tracks;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TracksContainer {
        private SpotifyPagingObject<SpotifyTrack> items;
    }
}
