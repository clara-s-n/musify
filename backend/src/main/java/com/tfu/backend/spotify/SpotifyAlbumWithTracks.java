package com.tfu.backend.spotify;

import lombok.Data;

import java.util.List;

@Data
public class SpotifyAlbumWithTracks extends SpotifyAlbum {
    // This class extends SpotifyAlbum which already has a tracks field
    // We don't need to override or redefine it
}