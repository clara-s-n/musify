package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyNewReleasesResponse {
    private AlbumsContainer albums;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlbumsContainer {
        private SpotifyPagingObject<SpotifyAlbumWithTracks> items;
    }
}
