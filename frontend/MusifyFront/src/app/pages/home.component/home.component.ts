import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { EnhancedSearchComponent } from '../../components/enhanced-search/enhanced-search.component';
import { MusicPlayerComponent } from '../../components/music-player/music-player.component';
import { DevInfoComponent } from '../../components/dev-info/dev-info.component';
import { SpotifyService } from '../../services/spotify.service';
import { PlayerService } from '../../services/player.service';
import { SpotifyPlaybackData, SpotifyTrack } from '../../models/spotify-track-model';
import { MatCardModule } from '@angular/material/card';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { Router } from '@angular/router';
import { YoutubeService } from '../../services/youtube.service';

@Component({
  selector: 'app-home',
  imports: [
    CommonModule,
    SearchBarComponent,
    EnhancedSearchComponent,
    MusicPlayerComponent,
    DevInfoComponent,
    MatCardModule,
    LogoComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private spotifyService = inject(SpotifyService);
  private youtubeService = inject(YoutubeService);
  private playerService = inject(PlayerService);
  private router = inject(Router);

  randomSongs: SpotifyTrack[] | null = null;
  currentPlayback: SpotifyPlaybackData | null = null;
  isPlaying: boolean = false;
  searchedSongs: SpotifyTrack[] | null = null;
  audioURL: string = '';
  showEnhancedSearch: boolean = false;

  ngOnInit(): void {
    this.spotifyService.getRandomTracks(12).subscribe((tracks: any) => (this.randomSongs = tracks));
  }

  /**
   * Reproduce una canción usando el nuevo PlayerService
   */
  playTrack(trackName: string, trackArtist: string): void {
    // Buscar el track completo para obtener el ID
    const track = this.randomSongs?.find(t =>
      t.name === trackName && t.artists === trackArtist
    );

    if (track) {
      this.playerService.play(track.id).subscribe({
        next: (state: any) => {
          console.log('Track started playing:', state.currentTrack?.name);
        },
        error: (error: any) => {
          console.error('Error playing track:', error);
          // Fallback al método anterior
          this.youtubeService.getAudio(trackName, trackArtist).subscribe((url: any) => (this.audioURL = url));
        }
      });
    } else {
      // Fallback al método anterior
      this.youtubeService.getAudio(trackName, trackArtist).subscribe((url: any) => (this.audioURL = url));
    }
  }

  /**
   * Navega a los resultados de búsqueda
   */
  searchHome(tracks: SpotifyTrack[]) {
    this.searchedSongs = tracks;
    this.router.navigate(['/result'], {
      state: { searchResultsHome: this.searchedSongs },
    });
  }

  /**
   * Alterna entre búsqueda simple y avanzada
   */
  toggleSearchMode(): void {
    this.showEnhancedSearch = !this.showEnhancedSearch;
  }
}
