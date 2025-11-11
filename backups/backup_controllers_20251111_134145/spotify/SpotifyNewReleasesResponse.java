package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyNewReleasesResponse {
    private SpotifyPagingObject albums;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpotifyPagingObject {
        private String href;
        private int limit;
        private String next;
        private int offset;
        private String previous;
        private int total;
        private List<SpotifyAlbum> items;
    }
}
