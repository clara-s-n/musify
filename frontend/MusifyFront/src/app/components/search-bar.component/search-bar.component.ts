import { Component, inject, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SpotifyService } from '../../services/spotify.service';
import { SpotifyTrack } from '../../models/spotify-track-model';

@Component({
  selector: 'search-bar',
  imports: [FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css',
})
export class SearchBarComponent {
  private spotifyService = inject(SpotifyService);
  searchQuery: string = '';

  searchResults = output<SpotifyTrack[]>();

  searchSong() {
    this.spotifyService.searchTracks(this.searchQuery, 15).subscribe((tracks) => {
      this.searchResults.emit(tracks);
    });
  }
}
