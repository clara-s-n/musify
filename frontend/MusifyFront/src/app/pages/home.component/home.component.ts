import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { MatCardModule } from '@angular/material/card';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { Router } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { PlaybackService } from '../../services/playback.service';
import { CatalogTrack } from '../../models/catalog-track.model';
import { PlaybackSession } from '../../models/playback-session.model';

@Component({
  selector: 'app-home',
  imports: [CommonModule, SearchBarComponent, MatCardModule, LogoComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private catalogService = inject(CatalogService);
  private playbackService = inject(PlaybackService);
  private router = inject(Router);
  tracks: CatalogTrack[] = [];
  searchedSongs: CatalogTrack[] | null = null;
  playbackSession: PlaybackSession | null = null;
  isLoadingPlayback = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.catalogService
      .getAllTracks()
      .subscribe((tracks: CatalogTrack[]) => (this.tracks = tracks.slice(0, 12)));
  }

  get audioUrl(): string | null {
    return this.playbackSession?.streamUrl ?? null;
  }

  get nextTrackLabel(): string {
    const next = this.playbackSession?.nextTrack;
    return next ? `${next.title} – ${next.artist}` : 'Sin pista precargada';
  }

  get previousTrackLabel(): string {
    const previous = this.playbackSession?.previousTrack;
    return previous ? `${previous.title} – ${previous.artist}` : 'Sin pista anterior';
  }

  startPlayback(track: CatalogTrack): void {
    this.errorMessage = null;
    this.isLoadingPlayback = true;
    this.playbackService.start(track.id).subscribe({
      next: (session: PlaybackSession) => {
        this.playbackSession = session;
        this.isLoadingPlayback = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.getErrorMessage(error);
        this.isLoadingPlayback = false;
      },
    });
  }

  pausePlayback(): void {
    this.errorMessage = null;
    this.playbackService.pause().subscribe({
      next: () => {
        if (this.playbackSession) {
          this.playbackSession = {
            ...this.playbackSession,
            streamUrl: this.playbackSession.streamUrl,
          };
        }
      },
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  resumePlayback(): void {
    this.errorMessage = null;
    this.playbackService.resume().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  stopPlayback(): void {
    this.errorMessage = null;
    this.playbackService.stop().subscribe({
      next: () => (this.playbackSession = null),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  skipToNext(): void {
    this.errorMessage = null;
    this.playbackService.skipToNext().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  skipToPrevious(): void {
    this.errorMessage = null;
    this.playbackService.skipToPrevious().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  playRecommended(track: CatalogTrack): void {
    this.startPlayback(track);
  }

  searchHome(tracks: CatalogTrack[]) {
    this.searchedSongs = tracks;
    this.router.navigate(['/result'], {
      state: { searchResultsHome: this.searchedSongs },
    });
  }

  private getErrorMessage(error: unknown): string {
    if (!error) {
      return 'Error desconocido';
    }
    if (typeof error === 'string') {
      return error;
    }
    const anyError = error as { error?: { message?: string; detail?: string }; message?: string };
    return (
      anyError?.error?.message ??
      anyError?.error?.detail ??
      anyError?.message ??
      'No fue posible completar la acción de reproducción'
    );
  }
}
