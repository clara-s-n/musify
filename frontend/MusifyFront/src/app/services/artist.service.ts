import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ArtistTrack {
  id: string;
  name: string;
  album: string;
  durationMs: number;
  formattedDuration: string;
  popularity: number;
  explicit: boolean;
  previewUrl: string | null;
  spotifyUrl: string;
  imageUrl: string | null;
}

export interface PaginationInfo {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ArtistTracksResponse {
  artistId: string;
  artistName: string;
  artistImageUrl: string | null;
  artistSpotifyUrl: string;
  tracks: ArtistTrack[];
  pagination: PaginationInfo;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ArtistService {
  private apiUrl = `${environment.apiBaseUrl}/api/artists`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene las canciones de un artista con paginación
   */
  getArtistTracks(artistId: string, page: number = 0, size: number = 20): Observable<ArtistTracksResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<ArtistTracksResponse>>(`${this.apiUrl}/${artistId}/tracks`, { params }).pipe(
      map(response => {
        if (response.success) {
          return response.data;
        } else {
          throw new Error(response.message || 'Error al obtener canciones del artista');
        }
      }),
      catchError(error => {
        console.error('Error en getArtistTracks:', error);
        return throwError(() => new Error(
          error.error?.message ||
          'No se pudieron cargar las canciones del artista. Por favor, inténtalo de nuevo.'
        ));
      })
    );
  }

  /**
   * Obtiene las canciones más populares de un artista (top tracks)
   */
  getArtistTopTracks(artistId: string): Observable<ArtistTracksResponse> {
    return this.http.get<ApiResponse<ArtistTracksResponse>>(`${this.apiUrl}/${artistId}/top-tracks`).pipe(
      map(response => {
        if (response.success) {
          return response.data;
        } else {
          throw new Error(response.message || 'Error al obtener top tracks del artista');
        }
      }),
      catchError(error => {
        console.error('Error en getArtistTopTracks:', error);
        return throwError(() => new Error(
          error.error?.message ||
          'No se pudieron cargar las canciones populares del artista. Por favor, inténtalo de nuevo.'
        ));
      })
    );
  }

  /**
   * Extrae el ID de Spotify de una URL de Spotify
   */
  extractSpotifyId(spotifyUrl: string): string | null {
    if (!spotifyUrl) return null;

    // URL format: https://open.spotify.com/artist/4Z8W4fKeB5YxbusRsdQVPb
    const match = spotifyUrl.match(/\/artist\/([a-zA-Z0-9]+)/);
    return match ? match[1] : null;
  }

  /**
   * Formatea la duración en milisegundos a formato MM:SS
   */
  formatDuration(durationMs: number): string {
    const totalSeconds = Math.floor(durationMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  /**
   * Obtiene un color basado en la popularidad de la canción
   */
  getPopularityColor(popularity: number): string {
    if (popularity >= 80) return '#1db954'; // Verde Spotify
    if (popularity >= 60) return '#1ed760';
    if (popularity >= 40) return '#ffa500'; // Naranja
    if (popularity >= 20) return '#ff6b6b'; // Rojo claro
    return '#999999'; // Gris
  }

  /**
   * Obtiene el texto de popularidad basado en el valor
   */
  getPopularityText(popularity: number): string {
    if (popularity >= 80) return 'Muy Popular';
    if (popularity >= 60) return 'Popular';
    if (popularity >= 40) return 'Moderada';
    if (popularity >= 20) return 'Baja';
    return 'Muy Baja';
  }
}