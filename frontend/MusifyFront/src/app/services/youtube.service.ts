import { Injectable } from '@angular/core';
import { environment } from '../enviroment/enviroment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class YoutubeService {
  private apiUrl = `${environment.apiUrl}/api/youtube`;

  constructor(private http: HttpClient) {}

  getAudio(trackName: string, trackArtist: string): Observable<string> {
    const params = new HttpParams().set('name', trackName).set('artist', trackArtist);
    return this.http.get(`${this.apiUrl}/audio`, {
      params,
      responseType: 'text',
    }) as Observable<string>;
  }
}
