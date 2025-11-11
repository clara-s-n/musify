import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../enviroment/enviroment';

export interface TrackInfo {
  id: string;
  name: string;
  artist: string;
  album: string;
  imageUrl: string;
  audioUrl: string;
  duration: number;
}

export interface PlayerState {
  status: 'playing' | 'paused' | 'stopped' | 'error';
  currentTrack: TrackInfo | null;
  queue: TrackInfo[];
  currentIndex: number;
  shuffle: boolean;
  repeat: boolean;
  position: number;
  duration: number;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({
  providedIn: 'root',
})
export class PlayerService {
  private apiUrl = `${environment.apiUrl}/api/player`;

  // Estado global del reproductor
  private playerStateSubject = new BehaviorSubject<PlayerState>({
    status: 'stopped',
    currentTrack: null,
    queue: [],
    currentIndex: 0,
    shuffle: false,
    repeat: false,
    position: 0,
    duration: 0
  });

  public playerState$ = this.playerStateSubject.asObservable();

  constructor(private http: HttpClient) {
    // Obtener estado inicial del servidor
    this.refreshState();

    // Actualizar estado cada 5 segundos si está reproduciendo
    setInterval(() => {
      const currentState = this.playerStateSubject.value;
      if (currentState.status === 'playing') {
        this.refreshState();
      }
    }, 5000);
  }

  /**
   * Reproduce una canción específica
   */
  play(trackId: string): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/play?trackId=${trackId}`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Reproduce la siguiente canción
   */
  playNext(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/next`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Reproduce la canción anterior
   */
  playPrevious(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/previous`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Pausa la reproducción
   */
  pause(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/pause`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Reanuda la reproducción
   */
  resume(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/resume`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Detiene la reproducción
   */
  stop(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/stop`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Activa/desactiva shuffle
   */
  toggleShuffle(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/shuffle`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Activa/desactiva repeat
   */
  toggleRepeat(): Observable<PlayerState> {
    return this.http.post<ApiResponse<PlayerState>>(`${this.apiUrl}/repeat`, {})
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Obtiene el estado actual del reproductor
   */
  getState(): Observable<PlayerState> {
    return this.http.get<ApiResponse<PlayerState>>(`${this.apiUrl}/state`)
      .pipe(
        map(response => {
          this.updateState(response.data);
          return response.data;
        })
      );
  }

  /**
   * Actualiza el estado local del reproductor
   */
  private updateState(newState: PlayerState): void {
    this.playerStateSubject.next(newState);
  }

  /**
   * Refresca el estado desde el servidor
   */
  private refreshState(): void {
    this.getState().subscribe({
      error: (error) => {
        console.error('Error refreshing player state:', error);
      }
    });
  }

  /**
   * Obtiene el estado actual sincronamente
   */
  getCurrentState(): PlayerState {
    return this.playerStateSubject.value;
  }
}