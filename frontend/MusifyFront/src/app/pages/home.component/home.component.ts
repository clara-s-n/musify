import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { SpotifyService } from '../../services/spotify.service';
import { SpotifyPlaybackData, SpotifyTrack } from '../../models/spotify-track-model';
import { MatCardModule } from '@angular/material/card';
import { LogoComponent } from '../../components/logo.component/logo.component';

@Component({
  selector: 'app-home',
  imports: [CommonModule, SearchBarComponent, MatCardModule, LogoComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private spotifyService = inject(SpotifyService);
  randomSongs: SpotifyTrack[] | null = null;
  currentPlayback: SpotifyPlaybackData | null = null;
  isPlaying: boolean = false;

  ngOnInit(): void {
    this.spotifyService.getRandomTracks(12).subscribe((tracks) => (this.randomSongs = tracks));
  }
  playTrack(trackId: string): void {
    this.spotifyService.getTrackPlayback(trackId).subscribe({
      next: (playbackData) => {
        this.currentPlayback = playbackData;
        this.isPlaying = true;

        if (playbackData.streamUrl) {
          const audioPlayer = document.getElementById('audioPlayer') as HTMLAudioElement;
          audioPlayer.src = playbackData.streamUrl;
          audioPlayer.play();
        }
      },
      error: (error) => {
        console.error('Error al obtener datos de reproducción:', error);
      },
    });
  }
}
