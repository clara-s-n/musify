package com.tfu.backend.player;

import lombok.Data;
import java.util.List;

/**
 * DTO para el estado del reproductor
 */
@Data
public class PlayerState {
    private String status; // "playing", "paused", "stopped"
    private TrackInfo currentTrack;
    private List<TrackInfo> queue;
    private int currentIndex;
    private boolean shuffle;
    private boolean repeat;
    private long position; // Posición actual en ms
    private long duration; // Duración total en ms
    
    public PlayerState() {
        this.status = "stopped";
        this.currentIndex = 0;
        this.shuffle = false;
        this.repeat = false;
        this.position = 0;
        this.duration = 0;
    }
}

/**
 * DTO para información de track en el reproductor
 */
@Data
class TrackInfo {
    private String id;
    private String name;
    private String artist;
    private String album;
    private String imageUrl;
    private String audioUrl;
    private long duration;
    
    public TrackInfo() {}
    
    public TrackInfo(String id, String name, String artist, String album, String imageUrl, String audioUrl, long duration) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.duration = duration;
    }
}