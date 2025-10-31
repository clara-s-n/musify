import { Component, inject, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../services/catalog.service';
import { CatalogTrack } from '../../models/catalog-track.model';
import { Router } from '@angular/router';

@Component({
  selector: 'search-bar',
  imports: [FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css',
})
export class SearchBarComponent {
  private catalogService = inject(CatalogService);
  searchQuery: string = '';
  private router = inject(Router);
  searchResults = output<CatalogTrack[]>();
  searchSong() {
    this.catalogService.search(this.searchQuery).subscribe((tracks: CatalogTrack[]) => {
      this.searchResults.emit(tracks);
    });
  }
}
