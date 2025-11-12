import { Component, effect, inject, input, OnInit } from '@angular/core';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { SpotifyTrack } from '../../models/spotify-track-model';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { YoutubeService } from '../../services/youtube.service';
import { PlayerService } from '../../services/player.service';
import { MusicPlayerComponent } from '../../components/music-player/music-player.component';

@Component({
  selector: 'app-results',
  imports: [LogoComponent, SearchBarComponent, CommonModule, FormsModule, MusicPlayerComponent],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css',
})
export class ResultsComponent {
  searchResults: SpotifyTrack[] | null = null;
  router = inject(Router);
  youtubeService = inject(YoutubeService);
  playerService = inject(PlayerService);
  audioURL: string = '';

  // Estados de la UI
  viewMode: 'grid' | 'list' = 'grid';
  sortBy: 'name' | 'artist' | 'popularity' = 'name';
  isLoading: boolean = false;
  searchQuery: string = '';

  constructor() {
    const nav = this.router.getCurrentNavigation();
    const fromState = (nav?.extras.state?.['searchResultsHome'] ??
      history.state?.['searchResultsHome']) as SpotifyTrack[] | null;
    if (fromState) {
      this.searchResults = fromState;
      this.extractSearchQuery();
    }
  }

  /**
   * Extrae la query de búsqueda del router state
   */
  private extractSearchQuery(): void {
    const nav = this.router.getCurrentNavigation();
    this.searchQuery = nav?.extras.state?.['query'] || 'Resultados de búsqueda';
  }

  /**
   * Maneja nuevos resultados de búsqueda
   */
  onSearchResults(results: SpotifyTrack[]): void {
    this.isLoading = true;
    setTimeout(() => {
      this.searchResults = results;
      this.isLoading = false;
    }, 300); // Simular carga para mejor UX
  }

  /**
   * Reproduce una pista usando el nuevo PlayerService
   */
  playTrack(track: SpotifyTrack): void {
    // Pasar los datos completos del track al PlayerService
    this.playerService.play(track.id, track).subscribe({
      next: (state: any) => {
        console.log('Track started playing:', state.currentTrack?.name);
      },
      error: (error: any) => {
        console.error('Error playing track:', error);
        // Fallback to YouTube service
        this.youtubeService.getAudio(track.name, track.artists).subscribe((url: any) => {
          this.audioURL = url;
        });
      }
    });
  }

  /**
   * Cambia el modo de vista (grid/list)
   */
  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }

  /**
   * Cambia el criterio de ordenamiento
   */
  changeSortBy(criteria: 'name' | 'artist' | 'popularity'): void {
    this.sortBy = criteria;
  }

  /**
   * Obtiene los resultados ordenados
   */
  getSortedResults(): SpotifyTrack[] {
    if (!this.searchResults) return [];

    return [...this.searchResults].sort((a, b) => {
      switch (this.sortBy) {
        case 'name':
          return a.name.localeCompare(b.name);
        case 'artist':
          return a.artists.localeCompare(b.artists);
        case 'popularity':
          // Como no tenemos popularity en SpotifyTrack, ordenamos por nombre como fallback
          return a.name.localeCompare(b.name);
        default:
          return 0;
      }
    });
  }

  /**
   * Formatea la duración de la pista
   */
  formatDuration(durationMs: number): string {
    if (!durationMs) return '--:--';
    const minutes = Math.floor(durationMs / 60000);
    const seconds = Math.floor((durationMs % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  /**
   * Añade una pista a la cola del reproductor
   */
  addToQueue(track: SpotifyTrack): void {
    // Esta funcionalidad podría implementarse en el PlayerService
    console.log('Adding to queue:', track.name);
  }

  /**
   * Navega al detalle del artista
   */
  goToArtist(track: SpotifyTrack): void {
    if (track.primaryArtistId) {
      this.router.navigate(['/artist', track.primaryArtistId]);
    } else {
      console.warn('No se encontró ID del artista para:', track.primaryArtistName || track.artists);
    }
  }

  /**
   * Va hacia atrás
   */
  goBack(): void {
    this.router.navigate(['/']);
  }
}
