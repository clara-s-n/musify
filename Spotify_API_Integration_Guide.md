# Guía de Integración de la API de Spotify en Musify

Este documento explica la estructura de datos de Spotify, cómo se implementa en el backend de Musify, y cómo consumirla desde el frontend.

## Estructura de Datos de Spotify

El backend de Musify ofrece una abstracción simplificada de la API de Spotify, exponiendo dos endpoints principales:

### 1. Modelo de Datos Expuesto (SpotifyTrackDto)

Los endpoints de la API devuelven objetos `SpotifyTrackDto` con la siguiente estructura:

```json
{
  "id": "5SQnZzUb1W1JGye7fVaBoP",
  "name": "Viva La Vida",
  "artists": "Coldplay",
  "album": "Viva La Vida or Death and All His Friends",
  "imageUrl": "https://i.scdn.co/image/ab67616d0000b273e21cc1db05580b6f2d2a3b6e",
  "previewUrl": "https://p.scdn.co/mp3-preview/7..."
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | String | Identificador único de la canción en Spotify |
| name | String | Nombre de la canción |
| artists | String | Artistas separados por comas |
| album | String | Nombre del álbum |
| imageUrl | String | URL de la imagen de portada del álbum |
| previewUrl | String | URL para reproducir una vista previa de la canción (puede ser null) |

## Endpoints de la API

### 1. Obtener Canciones Aleatorias

```
GET /music/spotify/random?limit={limit}
```

Este endpoint devuelve una lista de canciones basadas en nuevos lanzamientos de Spotify. Internamente, el backend consulta el endpoint de nuevos lanzamientos de Spotify y convierte los álbumes en objetos `SpotifyTrackDto`.

**Parámetros:**
- `limit` (opcional): Número de canciones a devolver (predeterminado: 10, máximo: 50)

**Respuesta:**
```json
{
  "success": true,
  "message": "Canciones aleatorias obtenidas correctamente",
  "data": [
    {
      "id": "1NDJk94691Vydq1IrIeGC0",
      "name": "PAPOTA",
      "artists": "CA7RIEL & Paco Amoroso, CA7RIEL, Paco Amoroso",
      "album": "PAPOTA",
      "imageUrl": "https://i.scdn.co/image/ab67616d00001e02c9f24a86cf421f7f9455ec8f",
      "previewUrl": null
    },
    // Más canciones...
  ],
  "timestamp": "2025-10-01T17:31:22.028Z"
}
```

### 2. Buscar Canciones

```
GET /music/spotify/search?q={búsqueda}&limit={limit}
```

Este endpoint permite buscar canciones en Spotify basadas en un término de búsqueda.

**Parámetros:**
- `q` (obligatorio): Término de búsqueda (artista, canción, álbum)
- `limit` (opcional): Número máximo de resultados (predeterminado: 20, máximo: 50)

**Respuesta:**
```json
{
  "success": true,
  "message": "Búsqueda realizada correctamente",
  "data": [
    {
      "id": "1mea3bSkSGXuIRvnydlB5b",
      "name": "Viva La Vida",
      "artists": "Coldplay",
      "album": "Viva La Vida or Death and All His Friends",
      "imageUrl": "https://i.scdn.co/image/ab67616d0000b273e21cc1db05580b6f2d2a3b6e",
      "previewUrl": null
    },
    // Más canciones...
  ],
  "timestamp": "2025-10-01T17:31:22.032Z"
}
```

## Consumo desde el Frontend

Para consumir estos endpoints desde el frontend Angular, puedes crear un servicio específico para la integración con Spotify.

### 1. Creación del Modelo

Crea una interfaz para representar el modelo de datos:

```typescript
// src/app/models/spotify-track.model.ts
export interface SpotifyTrack {
  id: string;
  name: string;
  artists: string;
  album: string;
  imageUrl: string;
  previewUrl: string | null;
}
```

### 2. Creación del Servicio

Implementa un servicio para comunicarte con los endpoints de la API:

```typescript
// src/app/services/spotify.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SpotifyTrack } from '../models/spotify-track.model';
import { environment } from '../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class SpotifyService {
  private apiUrl = `${environment.apiUrl}/music/spotify`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene canciones aleatorias (nuevos lanzamientos)
   * @param limit Número de canciones a obtener
   */
  getRandomTracks(limit: number = 10): Observable<SpotifyTrack[]> {
    return this.http.get<ApiResponse<SpotifyTrack[]>>(`${this.apiUrl}/random?limit=${limit}`)
      .pipe(map(response => response.data));
  }

  /**
   * Busca canciones por término de búsqueda
   * @param query Término de búsqueda
   * @param limit Número máximo de resultados
   */
  searchTracks(query: string, limit: number = 20): Observable<SpotifyTrack[]> {
    return this.http.get<ApiResponse<SpotifyTrack[]>>(
      `${this.apiUrl}/search?q=${encodeURIComponent(query)}&limit=${limit}`
    ).pipe(map(response => response.data));
  }
}
```

### 3. Uso en Componentes

Ejemplo de uso en un componente:

```typescript
// src/app/components/music-explorer/music-explorer.component.ts
import { Component, OnInit } from '@angular/core';
import { SpotifyService } from '../../services/spotify.service';
import { SpotifyTrack } from '../../models/spotify-track.model';

@Component({
  selector: 'app-music-explorer',
  templateUrl: './music-explorer.component.html',
  styleUrls: ['./music-explorer.component.css']
})
export class MusicExplorerComponent implements OnInit {
  randomTracks: SpotifyTrack[] = [];
  searchResults: SpotifyTrack[] = [];
  searchQuery: string = '';
  loading: boolean = false;

  constructor(private spotifyService: SpotifyService) { }

  ngOnInit(): void {
    this.loadRandomTracks();
  }

  loadRandomTracks(): void {
    this.loading = true;
    this.spotifyService.getRandomTracks(10).subscribe({
      next: (tracks) => {
        this.randomTracks = tracks;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando canciones aleatorias:', error);
        this.loading = false;
      }
    });
  }

  searchSpotify(): void {
    if (!this.searchQuery.trim()) return;
    
    this.loading = true;
    this.spotifyService.searchTracks(this.searchQuery).subscribe({
      next: (tracks) => {
        this.searchResults = tracks;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error en búsqueda:', error);
        this.loading = false;
      }
    });
  }
}
```

### 4. Visualización en la Plantilla

Ejemplo de plantilla HTML:

```html
<!-- src/app/components/music-explorer/music-explorer.component.html -->
<div class="music-explorer">
  <h2>Explorador de Música</h2>
  
  <!-- Búsqueda -->
  <div class="search-container">
    <input 
      type="text" 
      [(ngModel)]="searchQuery" 
      placeholder="Buscar artistas, canciones o álbumes..." 
      (keyup.enter)="searchSpotify()"
    >
    <button (click)="searchSpotify()">Buscar</button>
  </div>
  
  <!-- Resultados de búsqueda -->
  <div *ngIf="searchResults.length > 0" class="search-results">
    <h3>Resultados de la búsqueda</h3>
    <div class="track-grid">
      <div class="track-card" *ngFor="let track of searchResults">
        <img [src]="track.imageUrl || 'assets/images/default-album.png'" alt="{{ track.album }}">
        <div class="track-info">
          <h4>{{ track.name }}</h4>
          <p>{{ track.artists }}</p>
          <p class="album-name">{{ track.album }}</p>
          <audio *ngIf="track.previewUrl" controls [src]="track.previewUrl"></audio>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Nuevos lanzamientos -->
  <div class="new-releases">
    <h3>Nuevos lanzamientos</h3>
    <div class="track-grid">
      <div class="track-card" *ngFor="let track of randomTracks">
        <img [src]="track.imageUrl || 'assets/images/default-album.png'" alt="{{ track.album }}">
        <div class="track-info">
          <h4>{{ track.name }}</h4>
          <p>{{ track.artists }}</p>
          <p class="album-name">{{ track.album }}</p>
          <audio *ngIf="track.previewUrl" controls [src]="track.previewUrl"></audio>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Spinner de carga -->
  <div *ngIf="loading" class="loading-spinner">
    <div class="spinner"></div>
  </div>
</div>
```

## Manejo de Errores y Resilencia

El backend está configurado con patrones de resiliencia para manejar problemas de conexión con la API de Spotify:

1. **Circuit Breaker**: Evita sobrecarga de solicitudes cuando la API de Spotify está experimentando problemas.
2. **Retry**: Reintenta automáticamente las solicitudes fallidas con backoff exponencial.
3. **Timeout**: Establece límites de tiempo para las operaciones.

Si estos mecanismos se activan, los endpoints pueden devolver una lista vacía o datos en caché.

## Limitaciones y Consideraciones

1. **previewUrl**: Este campo puede ser `null` ya que no todas las canciones tienen vista previa disponible.
2. **Información de álbumes**: El endpoint `/random` devuelve información de álbumes convertida en formato de canciones.
3. **Cobertura SSL**: Todas las peticiones deben hacerse con HTTPS.
4. **Control de errores**: Implementar manejo adecuado de errores en el frontend para una buena experiencia de usuario.

## Ejemplos de Estilos CSS

Para mejorar la visualización de las canciones, puedes aplicar este CSS:

```css
/* src/app/components/music-explorer/music-explorer.component.css */
.track-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.track-card {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.track-card:hover {
  transform: translateY(-5px);
}

.track-card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.track-info {
  padding: 15px;
}

.track-info h4 {
  margin: 0 0 8px;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.track-info p {
  margin: 0 0 5px;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.album-name {
  font-style: italic;
}

audio {
  width: 100%;
  margin-top: 10px;
}

.search-container {
  display: flex;
  margin: 20px 0;
}

.search-container input {
  flex-grow: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
}

.search-container button {
  padding: 10px 20px;
  background: #1DB954;
  border: none;
  color: white;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  margin: 40px 0;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #1DB954;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
```

---

Con esta guía, deberías tener toda la información necesaria para integrar y consumir los endpoints de Spotify en tu frontend de Musify.