import { Component } from '@angular/core';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { SpotifyTrack } from '../../models/spotify-track-model';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-results',
  imports: [LogoComponent, SearchBarComponent, CommonModule],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css',
})
export class ResultsComponent {
  searchResults: SpotifyTrack[] | null = null;

  onSearchResults(results: SpotifyTrack[]) {
    this.searchResults = results;
  }
}
