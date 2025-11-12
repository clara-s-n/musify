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
  play(trackId: string, trackData?: any): Observable<PlayerState> {
    // Trabajar localmente sin backend para player
    const currentState = this.playerStateSubject.value;

    // Si tenemos trackData, usarlo; sino, crear uno básico
    const trackInfo: TrackInfo = trackData ? {
      id: trackData.id || trackId,
      name: trackData.name || 'Unknown Track',
      artist: trackData.artists || trackData.artist || 'Unknown Artist',
      album: trackData.album || 'Unknown Album',
      imageUrl: trackData.imageUrl || 'https://via.placeholder.com/200x200?text=🎵',
      audioUrl: '', // Se obtendrá del backend YouTube
      duration: trackData.duration || 0
    } : {
      id: trackId,
      name: 'Unknown Track',
      artist: 'Unknown Artist',
      album: 'Unknown Album',
      imageUrl: 'https://via.placeholder.com/200x200?text=🎵',
      audioUrl: '',
      duration: 0
    };

    // Crear estado inicial (sin audioUrl aún)
    const initialState: PlayerState = {
      ...currentState,
      status: 'playing',
      currentTrack: trackInfo,
      currentIndex: 0,
      position: 0
    };

    this.updateState(initialState);

    // Obtener URL de audio de YouTube de forma asíncrona
    const name = encodeURIComponent(trackInfo.name);
    const artist = encodeURIComponent(trackInfo.artist);
    const youtubeUrl = `${environment.backendUrl}/api/youtube/audio?name=${name}&artist=${artist}`;

    // Llamar al backend para obtener la URL de audio
    this.http.get(youtubeUrl, { responseType: 'text' }).subscribe({
      next: (audioUrl) => {
        if (audioUrl && audioUrl.startsWith('http')) {
          // Actualizar el estado con la URL de audio
          const updatedTrackInfo = { ...trackInfo, audioUrl };
          const updatedState: PlayerState = {
            ...initialState,
            currentTrack: updatedTrackInfo
          };
          this.updateState(updatedState);
          console.log('Audio URL obtained for:', trackInfo.name, audioUrl.substring(0, 80) + '...');
        }
      },
      error: (error) => {
        console.error('Error getting YouTube audio URL:', error);
        // Mantener el estado sin audioUrl, el MusicPlayerComponent manejará el fallback
      }
    });

    return new Observable(observer => {
      observer.next(initialState);
      observer.complete();
    });
  }

  /**
   * Reproduce la siguiente canción
   */
  playNext(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState };

    if (currentState.queue.length > 0 && currentState.currentIndex < currentState.queue.length - 1) {
      newState.currentIndex = currentState.currentIndex + 1;
      newState.currentTrack = currentState.queue[newState.currentIndex];
      newState.position = 0;
    }

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Reproduce la canción anterior
   */
  playPrevious(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState };

    if (currentState.queue.length > 0 && currentState.currentIndex > 0) {
      newState.currentIndex = currentState.currentIndex - 1;
      newState.currentTrack = currentState.queue[newState.currentIndex];
      newState.position = 0;
    }

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Pausa la reproducción
   */
  pause(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState, status: 'paused' as const };

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Reanuda la reproducción
   */
  resume(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState, status: 'playing' as const };

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Detiene la reproducción
   */
  stop(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState, status: 'stopped' as const, position: 0 };

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Activa/desactiva shuffle
   */
  toggleShuffle(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState, shuffle: !currentState.shuffle };

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Activa/desactiva repeat
   */
  toggleRepeat(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    const newState = { ...currentState, repeat: !currentState.repeat };

    this.updateState(newState);
    return new Observable(observer => {
      observer.next(newState);
      observer.complete();
    });
  }

  /**
   * Obtiene el estado actual del reproductor
   */
  getState(): Observable<PlayerState> {
    const currentState = this.playerStateSubject.value;
    return new Observable(observer => {
      observer.next(currentState);
      observer.complete();
    });
  }

  /**
   * Actualiza el estado local del reproductor
   */
  private updateState(newState: PlayerState): void {
    this.playerStateSubject.next(newState);
  }

  /**
   * Refresca el estado desde el servidor (no usado en modo local)
   */
  private refreshState(): void {
    // No hacer nada en modo local
    console.log('Player working in local mode - no server refresh needed');
  }

  /**
   * Obtiene el estado actual sincronamente
   */
  getCurrentState(): PlayerState {
    return this.playerStateSubject.value;
  }
}