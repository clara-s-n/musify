import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlaybackSession } from '../models/playback-session.model';
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
export class PlaybackService {
  private readonly baseUrl = `${environment.apiUrl}/playback`;

  constructor(private http: HttpClient) {}

  start(trackId: number): Observable<PlaybackSession> {
    const params = new HttpParams().set('trackId', trackId.toString());
    return this.http
      .post<ApiResponse<PlaybackSession>>(`${this.baseUrl}/start`, null, { params })
      .pipe(map((response: ApiResponse<PlaybackSession>) => response.data));
  }

  resume(): Observable<PlaybackSession> {
    return this.http
      .post<ApiResponse<PlaybackSession>>(`${this.baseUrl}/resume`, null)
      .pipe(map((response: ApiResponse<PlaybackSession>) => response.data));
  }

  pause(): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/pause`, null)
      .pipe(map(() => void 0));
  }

  stop(): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/stop`, null)
      .pipe(map(() => void 0));
  }

  skipToNext(): Observable<PlaybackSession> {
    return this.http
      .post<ApiResponse<PlaybackSession>>(`${this.baseUrl}/next`, null)
      .pipe(map((response: ApiResponse<PlaybackSession>) => response.data));
  }

  skipToPrevious(): Observable<PlaybackSession> {
    return this.http
      .post<ApiResponse<PlaybackSession>>(`${this.baseUrl}/previous`, null)
      .pipe(map((response: ApiResponse<PlaybackSession>) => response.data));
  }
}
