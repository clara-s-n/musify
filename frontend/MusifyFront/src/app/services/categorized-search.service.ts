import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../enviroment/enviroment';
import { SpotifyTrack } from '../models/spotify-track-model';

export interface ArtistDto {
  id: string;
  name: string;
  imageUrl: string;
  genres: string;
  followers: number;
}

export interface AlbumDto {
  id: string;
  name: string;
  artist: string;
  imageUrl: string;
  releaseDate: string;
  totalTracks: number;
}

export interface ConcertDto {
  id: string;
  name: string;
  artist: string;
  venue: string;
  date: string;
  city: string;
  imageUrl: string;
}

export interface CategorizedSearchResponse {
  songs: SpotifyTrack[];
  albums: AlbumDto[];
  artists: ArtistDto[];
  concerts: ConcertDto[];
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
export class CategorizedSearchService {
  private apiUrl = `${environment.apiUrl}/api/search`;

  constructor(private http: HttpClient) { }

  /**
   * Realiza búsqueda categorizada
   * @param query término de búsqueda
   * @param limit límite de resultados por categoría
   */
  searchCategorized(query: string, limit: number = 5): Observable<CategorizedSearchResponse> {
    return this.http
      .get<ApiResponse<CategorizedSearchResponse>>(`${this.apiUrl}?q=${encodeURIComponent(query)}&limit=${limit}`)
      .pipe(map((response) => response.data));
  }
}