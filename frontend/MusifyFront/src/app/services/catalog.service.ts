import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CatalogTrack } from '../models/catalog-track.model';
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
export class CatalogService {
  private readonly baseUrl = `${environment.apiUrl}/tracks`;

  constructor(private http: HttpClient) {}

  getAllTracks(): Observable<CatalogTrack[]> {
    return this.http
      .get<ApiResponse<CatalogTrack[]>>(this.baseUrl)
      .pipe(map((response: ApiResponse<CatalogTrack[]>) => response.data));
  }

  search(query: string): Observable<CatalogTrack[]> {
    const params = new HttpParams().set('q', query);
    return this.http
      .get<ApiResponse<CatalogTrack[]>>(`${this.baseUrl}/search`, { params })
      .pipe(map((response: ApiResponse<CatalogTrack[]>) => response.data));
  }
}
