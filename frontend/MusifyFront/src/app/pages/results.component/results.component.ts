import { Component, effect, inject, input, OnInit } from '@angular/core';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { SpotifyTrack } from '../../models/spotify-track-model';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { YoutubeService } from '../../services/youtube.service';

@Component({
  selector: 'app-results',
  imports: [LogoComponent, SearchBarComponent, CommonModule],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css',
})
export class ResultsComponent {
  searchResults: SpotifyTrack[] | null = null;
  router = inject(Router);
  youtubeService = inject(YoutubeService);
  audioURL: string = '';

  constructor() {
    const nav = this.router.getCurrentNavigation();
    const fromState = (nav?.extras.state?.['searchResultsHome'] ??
      history.state?.['searchResultsHome']) as SpotifyTrack[] | null;
    if (fromState) this.searchResults = fromState;
  }
  onSearchResults(results: SpotifyTrack[]) {
    this.searchResults = results;
  }
  playTrack(trackName: string, trackArtist: string): void {
    this.youtubeService.getAudio(trackName, trackArtist).subscribe((url) => (this.audioURL = url));
  }
}
