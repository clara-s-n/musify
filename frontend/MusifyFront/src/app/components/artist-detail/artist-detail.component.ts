import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';

import { ArtistService, ArtistTracksResponse, ArtistTrack } from '../../services/artist.service';
import { PlayerService } from '../../services/player.service';

@Component({
  selector: 'app-artist-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
    MatCardModule,
    MatSnackBarModule
  ],
  template: `
    <div class="artist-detail-container">
      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner diameter="50"></mat-spinner>
        <p>Cargando información del artista...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="error && !isLoading" class="error-container">
        <mat-icon>error</mat-icon>
        <h3>Error al cargar el artista</h3>
        <p>{{ error }}</p>
        <button mat-raised-button color="primary" (click)="loadArtistTracks()">
          <mat-icon>refresh</mat-icon>
          Reintentar
        </button>
      </div>

      <!-- Artist Content -->
      <div *ngIf="artistData && !isLoading" class="artist-content">
        <!-- Artist Header -->
        <div class="artist-header">
          <div class="artist-image-container">
            <img 
              [src]="artistData.artistImageUrl || 'assets/images/artist-placeholder.png'" 
              [alt]="artistData.artistName"
              class="artist-image"
              (error)="onImageError($event)"
            />
          </div>
          <div class="artist-info">
            <h1 class="artist-name">{{ artistData.artistName }}</h1>
            <p class="artist-stats">
              {{ artistData.pagination.totalElements }} canciones disponibles
            </p>
            <div class="artist-actions">
              <button 
                mat-raised-button 
                color="primary" 
                class="play-all-btn"
                (click)="playAllTracks()"
                [disabled]="!artistData.tracks.length"
              >
                <mat-icon>play_arrow</mat-icon>
                Reproducir Todo
              </button>
              <button 
                mat-stroked-button 
                color="primary"
                (click)="openSpotifyProfile()"
                *ngIf="artistData.artistSpotifyUrl"
              >
                <mat-icon>open_in_new</mat-icon>
                Ver en Spotify
              </button>
            </div>
          </div>
        </div>

        <!-- Tracks Section -->
        <div class="tracks-section">
          <div class="section-header">
            <h2>Canciones</h2>
            <mat-chip-set>
              <mat-chip>{{ artistData.tracks.length }} de {{ artistData.pagination.totalElements }} canciones</mat-chip>
            </mat-chip-set>
          </div>

          <!-- Tracks List -->
          <div class="tracks-list" *ngIf="artistData.tracks.length > 0">
            <div 
              *ngFor="let track of artistData.tracks; let i = index" 
              class="track-item"
              [class.playing]="isTrackPlaying(track.id)"
            >
              <div class="track-number">
                <span *ngIf="!isTrackPlaying(track.id)">{{ (currentPage * pageSize) + i + 1 }}</span>
                <mat-icon *ngIf="isTrackPlaying(track.id)" class="playing-icon">volume_up</mat-icon>
              </div>
              
              <div class="track-image">
                <img 
                  [src]="track.imageUrl || 'assets/images/track-placeholder.png'" 
                  [alt]="track.name"
                  (error)="onTrackImageError($event)"
                />
                <button 
                  mat-icon-button 
                  class="play-btn-overlay"
                  (click)="playTrack(track)"
                  [attr.aria-label]="'Reproducir ' + track.name"
                >
                  <mat-icon>{{ isTrackPlaying(track.id) ? 'pause' : 'play_arrow' }}</mat-icon>
                </button>
              </div>

              <div class="track-info" (click)="playTrack(track)">
                <div class="track-name">{{ track.name }}</div>
                <div class="track-album">{{ track.album }}</div>
              </div>

              <div class="track-popularity">
                <div 
                  class="popularity-bar"
                  [style.width.%]="track.popularity"
                  [style.background-color]="getPopularityColor(track.popularity)"
                  [title]="getPopularityText(track.popularity) + ' (' + track.popularity + '%)'">
                </div>
              </div>

              <div class="track-explicit" *ngIf="track.explicit">
                <mat-chip color="warn">E</mat-chip>
              </div>

              <div class="track-duration">{{ track.formattedDuration }}</div>

              <div class="track-actions">
                <button 
                  mat-icon-button 
                  (click)="addToQueue(track)"
                  matTooltip="Agregar a cola"
                >
                  <mat-icon>playlist_add</mat-icon>
                </button>
                <button 
                  mat-icon-button 
                  (click)="openSpotifyTrack(track.spotifyUrl)"
                  matTooltip="Ver en Spotify"
                  *ngIf="track.spotifyUrl"
                >
                  <mat-icon>open_in_new</mat-icon>
                </button>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div *ngIf="artistData.tracks.length === 0" class="empty-state">
            <mat-icon>music_off</mat-icon>
            <h3>No se encontraron canciones</h3>
            <p>Este artista no tiene canciones disponibles en este momento.</p>
          </div>

          <!-- Pagination -->
          <mat-paginator 
            *ngIf="artistData.pagination.totalPages > 1"
            [length]="artistData.pagination.totalElements"
            [pageSize]="pageSize"
            [pageIndex]="currentPage"
            [pageSizeOptions]="[10, 20, 30, 50]"
            (page)="onPageChange($event)"
            showFirstLastButtons
          >
          </mat-paginator>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .artist-detail-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
      min-height: calc(100vh - 64px);
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 50vh;
      gap: 20px;
    }

    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 50vh;
      gap: 15px;
      text-align: center;
    }

    .error-container mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #f44336;
    }

    .artist-header {
      display: flex;
      gap: 30px;
      margin-bottom: 40px;
      align-items: flex-end;
      background: linear-gradient(135deg, rgba(29, 185, 84, 0.1) 0%, rgba(0, 0, 0, 0.05) 100%);
      padding: 30px;
      border-radius: 16px;
    }

    .artist-image-container {
      flex-shrink: 0;
    }

    .artist-image {
      width: 200px;
      height: 200px;
      border-radius: 50%;
      object-fit: cover;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    }

    .artist-info {
      flex: 1;
    }

    .artist-name {
      font-size: 3rem;
      font-weight: 900;
      margin: 0 0 10px 0;
      color: #1db954;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .artist-stats {
      font-size: 1.1rem;
      color: #666;
      margin-bottom: 20px;
    }

    .artist-actions {
      display: flex;
      gap: 15px;
      flex-wrap: wrap;
    }

    .play-all-btn {
      background: #1db954 !important;
      color: white !important;
      padding: 12px 30px;
      font-size: 1rem;
      font-weight: 600;
    }

    .tracks-section {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      overflow: hidden;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 30px;
      border-bottom: 1px solid #eee;
      background: #fafafa;
    }

    .section-header h2 {
      margin: 0;
      color: #333;
    }

    .tracks-list {
      padding: 0;
    }

    .track-item {
      display: grid;
      grid-template-columns: 60px 60px 1fr 120px 40px 80px 120px;
      gap: 15px;
      padding: 15px 30px;
      align-items: center;
      border-bottom: 1px solid #f5f5f5;
      transition: all 0.2s ease;
      cursor: pointer;
    }

    .track-item:hover {
      background: #f8f9fa;
    }

    .track-item.playing {
      background: rgba(29, 185, 84, 0.1);
      border-left: 4px solid #1db954;
    }

    .track-number {
      text-align: center;
      font-weight: 500;
      color: #666;
      font-size: 0.9rem;
    }

    .playing-icon {
      color: #1db954;
      font-size: 20px;
    }

    .track-image {
      position: relative;
      width: 50px;
      height: 50px;
    }

    .track-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 6px;
    }

    .play-btn-overlay {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      opacity: 0;
      transition: opacity 0.2s ease;
      background: rgba(0, 0, 0, 0.7) !important;
      color: white !important;
      width: 40px !important;
      height: 40px !important;
    }

    .track-item:hover .play-btn-overlay {
      opacity: 1;
    }

    .track-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;
    }

    .track-name {
      font-weight: 600;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .track-album {
      font-size: 0.9rem;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .track-popularity {
      width: 100px;
      height: 4px;
      background: #e0e0e0;
      border-radius: 2px;
      overflow: hidden;
    }

    .popularity-bar {
      height: 100%;
      border-radius: 2px;
      transition: width 0.3s ease;
    }

    .track-explicit mat-chip {
      font-size: 0.75rem;
      min-height: 20px;
      padding: 0 6px;
    }

    .track-duration {
      color: #666;
      font-size: 0.9rem;
      text-align: right;
    }

    .track-actions {
      display: flex;
      gap: 5px;
      opacity: 0;
      transition: opacity 0.2s ease;
    }

    .track-item:hover .track-actions {
      opacity: 1;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #666;
      text-align: center;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 20px;
      opacity: 0.5;
    }

    mat-paginator {
      border-top: 1px solid #eee;
    }

    .truncate-text {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .artist-header {
        flex-direction: column;
        text-align: center;
        gap: 20px;
      }

      .artist-image {
        width: 150px;
        height: 150px;
      }

      .artist-name {
        font-size: 2rem;
      }

      .track-item {
        grid-template-columns: 40px 1fr 60px 80px;
        gap: 10px;
        padding: 12px 20px;
      }

      .track-image,
      .track-popularity,
      .track-explicit {
        display: none;
      }

      .track-actions {
        opacity: 1;
        justify-content: flex-end;
      }
    }
  `]
})
export class ArtistDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private artistService = inject(ArtistService);
  private playerService = inject(PlayerService);
  private snackBar = inject(MatSnackBar);

  private destroy$ = new Subject<void>();

  artistId: string = '';
  artistData: ArtistTracksResponse | null = null;
  isLoading = false;
  error: string | null = null;
  currentPage = 0;
  pageSize = 20;

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe((params: any) => {
      this.artistId = params['id'];
      if (this.artistId) {
        this.loadArtistTracks();
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadArtistTracks() {
    if (!this.artistId) return;

    this.isLoading = true;
    this.error = null;

    this.artistService.getArtistTracks(this.artistId, this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          this.artistData = data;
          this.isLoading = false;
        },
        error: (error: any) => {
          this.error = error.message;
          this.isLoading = false;
          this.snackBar.open('Error al cargar las canciones del artista', 'Cerrar', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadArtistTracks();

    // Scroll to top of tracks list
    document.querySelector('.tracks-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  playTrack(track: ArtistTrack) {
    this.playerService.play(track.id).subscribe({
      next: (state: any) => {
        this.snackBar.open(`Reproduciendo: ${track.name}`, 'Cerrar', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error: any) => {
        console.error('Error playing track:', error);
        this.snackBar.open('Error al reproducir la canción', 'Cerrar', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  playAllTracks() {
    if (!this.artistData?.tracks.length) return;

    // Por ahora solo reproducimos la primera canción
    // En una implementación completa se podría agregar soporte para cola
    this.playerService.play(this.artistData.tracks[0].id).subscribe({
      next: (state: any) => {
        this.snackBar.open(`Reproduciendo todas las canciones de ${this.artistData?.artistName || 'el artista'}`, 'Cerrar', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error: any) => {
        console.error('Error playing tracks:', error);
        this.snackBar.open('Error al reproducir las canciones', 'Cerrar', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  addToQueue(track: ArtistTrack) {
    // Por ahora solo mostramos un mensaje
    // En una implementación completa se agregaría soporte para cola
    this.snackBar.open(`${track.name} - funcionalidad de cola en desarrollo`, 'Cerrar', {
      duration: 2000,
      panelClass: ['info-snackbar']
    });
  }

  isTrackPlaying(trackId: string): boolean {
    const currentState = this.playerService.getCurrentState();
    return currentState.currentTrack?.id === trackId && currentState.status === 'playing';
  }

  openSpotifyProfile() {
    if (this.artistData?.artistSpotifyUrl) {
      window.open(this.artistData.artistSpotifyUrl, '_blank');
    }
  }

  openSpotifyTrack(spotifyUrl: string) {
    if (spotifyUrl) {
      window.open(spotifyUrl, '_blank');
    }
  }

  getPopularityColor(popularity: number): string {
    return this.artistService.getPopularityColor(popularity);
  }

  getPopularityText(popularity: number): string {
    return this.artistService.getPopularityText(popularity);
  }

  onImageError(event: any) {
    event.target.src = 'assets/images/artist-placeholder.png';
  }

  onTrackImageError(event: any) {
    event.target.src = 'assets/images/track-placeholder.png';
  }
}