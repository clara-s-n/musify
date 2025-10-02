// src/app/services/spotify.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SpotifyTrack, SpotifyPlaybackData } from '../models/spotify-track-model';
import { environment } from '../enviroment/enviroment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({
  providedIn: 'root',
})
export class SpotifyService {
  private apiUrl = `${environment.apiUrl}/music/spotify`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene canciones aleatorias (nuevos lanzamientos)
   * @param limit Número de canciones a obtener
   */
  getRandomTracks(limit: number = 10): Observable<SpotifyTrack[]> {
    return this.http
      .get<ApiResponse<SpotifyTrack[]>>(`${this.apiUrl}/random?limit=${limit}`)
      .pipe(map((response) => response.data));
  }

  /**
   * Busca canciones por término de búsqueda
   * @param query Término de búsqueda
   * @param limit Número máximo de resultados
   */
  searchTracks(query: string, limit: number = 20): Observable<SpotifyTrack[]> {
    return this.http
      .get<ApiResponse<SpotifyTrack[]>>(
        `${this.apiUrl}/search?q=${encodeURIComponent(query)}&limit=${limit}`
      )
      .pipe(map((response) => response.data));
  }

  /**
   * Obtiene los datos de reproducción para una canción específica
   * @param trackId ID único de Spotify de la canción
   */
  getTrackPlayback(trackId: string): Observable<SpotifyPlaybackData> {
    return this.http
      .get<ApiResponse<SpotifyPlaybackData>>(`${this.apiUrl}/play/${trackId}`)
      .pipe(map((response) => response.data));
  }
}
