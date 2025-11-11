import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategorizedSearchResponse, AlbumDto, ArtistDto, ConcertDto } from '../../services/categorized-search.service';
import { SpotifyTrack } from '../../models/spotify-track-model';
import { PlayerService } from '../../services/player.service';

@Component({
  selector: 'app-categorized-search-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="search-results" *ngIf="results">
      
      <!-- Songs Section -->
      <div class="results-section" *ngIf="results.songs && results.songs.length > 0">
        <h3 class="section-title">🎵 Canciones</h3>
        <div class="songs-grid">
          <div 
            *ngFor="let song of results.songs" 
            class="song-card"
            (click)="playTrack(song.id)"
          >
            <img 
              [src]="song.imageUrl" 
              [alt]="song.name"
              class="song-image"
              (error)="onImageError($event)"
            >
            <div class="song-info">
              <h4 class="song-name">{{ song.name }}</h4>
              <p class="song-artist">{{ song.artists }}</p>
              <p class="song-album">{{ song.album }}</p>
            </div>
            <button class="play-btn" title="Reproducir">▶️</button>
          </div>
        </div>
      </div>

      <!-- Albums Section -->
      <div class="results-section" *ngIf="results.albums && results.albums.length > 0">
        <h3 class="section-title">💿 Álbumes</h3>
        <div class="albums-grid">
          <div 
            *ngFor="let album of results.albums" 
            class="album-card"
          >
            <img 
              [src]="album.imageUrl" 
              [alt]="album.name"
              class="album-image"
              (error)="onImageError($event)"
            >
            <div class="album-info">
              <h4 class="album-name">{{ album.name }}</h4>
              <p class="album-artist">{{ album.artist }}</p>
              <p class="album-details">{{ album.totalTracks }} canciones • {{ album.releaseDate }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Artists Section -->
      <div class="results-section" *ngIf="results.artists && results.artists.length > 0">
        <h3 class="section-title">🎤 Artistas</h3>
        <div class="artists-grid">
          <div 
            *ngFor="let artist of results.artists" 
            class="artist-card"
          >
            <img 
              [src]="artist.imageUrl" 
              [alt]="artist.name"
              class="artist-image"
              (error)="onImageError($event)"
            >
            <div class="artist-info">
              <h4 class="artist-name">{{ artist.name }}</h4>
              <p class="artist-genres">{{ artist.genres }}</p>
              <p class="artist-followers">{{ formatFollowers(artist.followers) }} seguidores</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Concerts Section -->
      <div class="results-section" *ngIf="results.concerts && results.concerts.length > 0">
        <h3 class="section-title">🎪 Conciertos</h3>
        <div class="concerts-grid">
          <div 
            *ngFor="let concert of results.concerts" 
            class="concert-card"
          >
            <img 
              [src]="concert.imageUrl" 
              [alt]="concert.name"
              class="concert-image"
              (error)="onImageError($event)"
            >
            <div class="concert-info">
              <h4 class="concert-name">{{ concert.name }}</h4>
              <p class="concert-artist">{{ concert.artist }}</p>
              <p class="concert-venue">📍 {{ concert.venue }}, {{ concert.city }}</p>
              <p class="concert-date">📅 {{ formatDate(concert.date) }}</p>
            </div>
            <button class="ticket-btn">🎟️ Tickets</button>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div class="no-results" *ngIf="!hasResults()">
        <p>🔍 No se encontraron resultados</p>
      </div>

    </div>
  `,
  styles: [`
    .search-results {
      padding: 20px;
    }

    .results-section {
      margin-bottom: 40px;
    }

    .section-title {
      font-size: 1.5em;
      margin-bottom: 20px;
      color: #333;
      border-bottom: 2px solid #e0e0e0;
      padding-bottom: 10px;
    }

    /* Songs Grid */
    .songs-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 15px;
      margin-bottom: 20px;
    }

    .song-card {
      display: flex;
      align-items: center;
      background: white;
      border-radius: 10px;
      padding: 15px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
    }

    .song-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0,0,0,0.15);
    }

    .song-image {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      object-fit: cover;
      margin-right: 15px;
    }

    .song-info {
      flex: 1;
    }

    .song-name {
      margin: 0 0 5px 0;
      font-size: 1.1em;
      font-weight: bold;
      color: #333;
    }

    .song-artist, .song-album {
      margin: 2px 0;
      color: #666;
      font-size: 0.9em;
    }

    .play-btn {
      background: #4CAF50;
      border: none;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      font-size: 1.2em;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .play-btn:hover {
      background: #45a049;
      transform: scale(1.1);
    }

    /* Albums Grid */
    .albums-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .album-card {
      background: white;
      border-radius: 15px;
      padding: 20px;
      text-align: center;
      box-shadow: 0 4px 15px rgba(0,0,0,0.1);
      transition: transform 0.3s ease;
    }

    .album-card:hover {
      transform: translateY(-5px);
    }

    .album-image {
      width: 100%;
      aspect-ratio: 1;
      border-radius: 10px;
      object-fit: cover;
      margin-bottom: 15px;
    }

    .album-name {
      margin: 0 0 8px 0;
      font-size: 1.2em;
      font-weight: bold;
      color: #333;
    }

    .album-artist {
      margin: 0 0 5px 0;
      color: #666;
      font-size: 1em;
    }

    .album-details {
      margin: 0;
      color: #999;
      font-size: 0.9em;
    }

    /* Artists Grid */
    .artists-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 20px;
    }

    .artist-card {
      background: white;
      border-radius: 15px;
      padding: 20px;
      text-align: center;
      box-shadow: 0 4px 15px rgba(0,0,0,0.1);
      transition: transform 0.3s ease;
    }

    .artist-card:hover {
      transform: translateY(-5px);
    }

    .artist-image {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      object-fit: cover;
      margin: 0 auto 15px;
      display: block;
    }

    .artist-name {
      margin: 0 0 8px 0;
      font-size: 1.2em;
      font-weight: bold;
      color: #333;
    }

    .artist-genres {
      margin: 0 0 5px 0;
      color: #666;
      font-size: 0.9em;
    }

    .artist-followers {
      margin: 0;
      color: #999;
      font-size: 0.8em;
    }

    /* Concerts Grid */
    .concerts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }

    .concert-card {
      display: flex;
      background: white;
      border-radius: 15px;
      padding: 20px;
      box-shadow: 0 4px 15px rgba(0,0,0,0.1);
      transition: transform 0.3s ease;
      position: relative;
    }

    .concert-card:hover {
      transform: translateY(-3px);
    }

    .concert-image {
      width: 80px;
      height: 80px;
      border-radius: 10px;
      object-fit: cover;
      margin-right: 15px;
    }

    .concert-info {
      flex: 1;
    }

    .concert-name {
      margin: 0 0 8px 0;
      font-size: 1.2em;
      font-weight: bold;
      color: #333;
    }

    .concert-artist, .concert-venue, .concert-date {
      margin: 4px 0;
      color: #666;
      font-size: 0.9em;
    }

    .ticket-btn {
      position: absolute;
      top: 15px;
      right: 15px;
      background: #FF6B6B;
      color: white;
      border: none;
      border-radius: 20px;
      padding: 8px 12px;
      font-size: 0.8em;
      cursor: pointer;
      transition: background 0.3s ease;
    }

    .ticket-btn:hover {
      background: #FF5252;
    }

    /* No Results */
    .no-results {
      text-align: center;
      padding: 60px 20px;
      color: #999;
      font-size: 1.2em;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .songs-grid {
        grid-template-columns: 1fr;
      }

      .albums-grid, .artists-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      }

      .concerts-grid {
        grid-template-columns: 1fr;
      }

      .song-card {
        padding: 12px;
      }

      .song-image {
        width: 50px;
        height: 50px;
        margin-right: 12px;
      }

      .concert-card {
        flex-direction: column;
        text-align: center;
      }

      .concert-image {
        width: 60px;
        height: 60px;
        margin: 0 auto 10px;
      }

      .ticket-btn {
        position: static;
        margin-top: 10px;
      }
    }
  `]
})
export class CategorizedSearchResultsComponent {
  @Input() results: CategorizedSearchResponse | null = null;

  constructor(private playerService: PlayerService) { }

  /**
   * Reproduce una canción
   */
  playTrack(trackId: string): void {
    this.playerService.play(trackId).subscribe({
      next: (state) => {
        console.log('Track started playing:', state.currentTrack?.name);
      },
      error: (error) => {
        console.error('Error playing track:', error);
      }
    });
  }

  /**
   * Verifica si hay resultados para mostrar
   */
  hasResults(): boolean {
    if (!this.results) return false;

    return (
      (this.results.songs && this.results.songs.length > 0) ||
      (this.results.albums && this.results.albums.length > 0) ||
      (this.results.artists && this.results.artists.length > 0) ||
      (this.results.concerts && this.results.concerts.length > 0)
    );
  }

  /**
   * Maneja errores de imágenes
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://via.placeholder.com/200x200?text=🎵';
  }

  /**
   * Formatea el número de seguidores
   */
  formatFollowers(followers: number): string {
    if (followers >= 1000000) {
      return (followers / 1000000).toFixed(1) + 'M';
    } else if (followers >= 1000) {
      return (followers / 1000).toFixed(1) + 'K';
    }
    return followers.toString();
  }

  /**
   * Formatea la fecha del concierto
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}