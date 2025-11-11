package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyTrackDto {
    private String id;
    private String name;
    private String artists;
    private String album;
    private String imageUrl;
    private String previewUrl;
}
