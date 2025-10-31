import { Component, inject } from '@angular/core';
import { SearchBarComponent } from '../../components/search-bar.component/search-bar.component';
import { LogoComponent } from '../../components/logo.component/logo.component';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CatalogTrack } from '../../models/catalog-track.model';
import { PlaybackService } from '../../services/playback.service';
import { PlaybackSession } from '../../models/playback-session.model';

@Component({
  selector: 'app-results',
  imports: [LogoComponent, SearchBarComponent, CommonModule],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css',
})
export class ResultsComponent {
  searchResults: CatalogTrack[] | null = null;
  private router = inject(Router);
  private playbackService = inject(PlaybackService);
  playbackSession: PlaybackSession | null = null;
  errorMessage: string | null = null;

  constructor() {
    const nav = this.router.getCurrentNavigation();
    const fromState = (nav?.extras.state?.['searchResultsHome'] ??
      history.state?.['searchResultsHome']) as CatalogTrack[] | null;
    if (fromState) this.searchResults = fromState;
  }
  onSearchResults(results: CatalogTrack[]) {
    this.searchResults = results;
  }

  playTrack(track: CatalogTrack): void {
    this.errorMessage = null;
    this.playbackService.start(track.id).subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  resume(): void {
    this.playbackService.resume().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  pause(): void {
    this.playbackService.pause().subscribe({
      next: () => {},
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  next(): void {
    this.playbackService.skipToNext().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  previous(): void {
    this.playbackService.skipToPrevious().subscribe({
      next: (session: PlaybackSession) => (this.playbackSession = session),
      error: (error: unknown) => (this.errorMessage = this.getErrorMessage(error)),
    });
  }

  get audioUrl(): string | null {
    return this.playbackSession?.streamUrl ?? null;
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
