import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { SpotifyService } from '../../services/spotify.service';
import { SpotifyPlaybackData, SpotifyTrack } from '../../models/spotify-track-model';
import { MatCardModule } from '@angular/material/card';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { Router } from '@angular/router';
import { YoutubeService } from '../../services/youtube.service';

@Component({
  selector: 'app-home',
  imports: [CommonModule, SearchBarComponent, MatCardModule, LogoComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private spotifyService = inject(SpotifyService);
  private youtubeService = inject(YoutubeService);
  private router = inject(Router);
  randomSongs: SpotifyTrack[] | null = null;
  currentPlayback: SpotifyPlaybackData | null = null;
  isPlaying: boolean = false;
  searchedSongs: SpotifyTrack[] | null = null;
  audioURL: string = '';
  ngOnInit(): void {
    this.spotifyService.getRandomTracks(12).subscribe((tracks) => (this.randomSongs = tracks));
  }
  playTrack(trackName: string, trackArtist: string): void {
    this.youtubeService.getAudio(trackName, trackArtist).subscribe((url) => (this.audioURL = url));
  }
  searchHome(tracks: SpotifyTrack[]) {
    this.searchedSongs = tracks;
    this.router.navigate(['/result'], {
      state: { searchResultsHome: this.searchedSongs },
    });
  }
}
