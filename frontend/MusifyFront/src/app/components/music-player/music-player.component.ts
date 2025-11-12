import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { PlayerService, PlayerState, TrackInfo } from '../../services/player.service';
import { YoutubeService } from '../../services/youtube.service';
import { environment } from '../../enviroment/enviroment';

@Component({
  selector: 'app-music-player',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="music-player" *ngIf="playerState.currentTrack">
      <!-- Track Information -->
      <div class="track-info">
        <img 
          [src]="playerState.currentTrack.imageUrl" 
          [alt]="playerState.currentTrack.name"
          class="track-image"
          (error)="onImageError($event)"
        >
        <div class="track-details">
          <h3 class="track-name">{{ playerState.currentTrack.name }}</h3>
          <p class="track-artist">{{ playerState.currentTrack.artist }}</p>
        </div>
      </div>

      <!-- Audio Element (Hidden) -->
      <audio 
        #audioElement
        [src]="currentAudioUrl"
        (loadstart)="onLoadStart()"
        (canplay)="onCanPlay()"
        (timeupdate)="onTimeUpdate($event)"
        (ended)="onTrackEnd()"
        (error)="onAudioError($event)"
        preload="metadata"
      ></audio>

      <!-- Controls -->
      <div class="player-controls">
        <button 
          class="control-btn shuffle-btn"
          [class.active]="playerState.shuffle"
          (click)="toggleShuffle()"
          title="Shuffle"
        >
          🔀
        </button>

        <button 
          class="control-btn"
          (click)="playPrevious()"
          title="Previous"
        >
          ⏮️
        </button>

        <button 
          class="control-btn play-pause-btn"
          (click)="togglePlayPause()"
          [title]="playerState.status === 'playing' ? 'Pause' : 'Play'"
        >
          {{ playerState.status === 'playing' ? '⏸️' : '▶️' }}
        </button>

        <button 
          class="control-btn"
          (click)="playNext()"
          title="Next"
        >
          ⏭️
        </button>

        <button 
          class="control-btn repeat-btn"
          [class.active]="playerState.repeat"
          (click)="toggleRepeat()"
          title="Repeat"
        >
          🔁
        </button>
      </div>

      <!-- Progress Bar -->
      <div class="progress-section">
        <span class="time-display">{{ formatTime(currentPosition) }}</span>
        <div class="progress-bar-container">
          <div class="progress-bar">
            <div 
              class="progress-fill"
              [style.width.%]="progressPercentage"
            ></div>
            <input 
              type="range"
              class="progress-slider"
              min="0"
              [max]="playerState.duration || 100"
              [value]="currentPosition"
              (input)="onSeek($event)"
            >
          </div>
        </div>
        <span class="time-display">{{ formatTime(playerState.duration) }}</span>
      </div>

      <!-- Queue Display (Hidden for compact player) -->
      <!-- <div class="queue-section" *ngIf="playerState.queue.length > 1">
        <h4>En cola ({{ playerState.queue.length }} canciones):</h4>
        <div class="queue-list">
          <div 
            *ngFor="let track of playerState.queue; let i = index"
            class="queue-item"
            [class.current]="i === playerState.currentIndex"
          >
            <span class="queue-number">{{ i + 1 }}</span>
            <span class="queue-track">{{ track.name }} - {{ track.artist }}</span>
          </div>
        </div>
      </div> -->

      <!-- Loading/Error States -->
      <div class="player-status" *ngIf="isLoading">
        <p>🎵 Cargando música...</p>
      </div>

      <div class="player-status error" *ngIf="playerState.status === 'error'">
        <p>❌ Error al reproducir la canción</p>
      </div>
      
      <!-- Debug info (remove in production) -->
      <div class="player-status debug" *ngIf="!currentAudioUrl && playerState.currentTrack && !isLoading">
        <p>⚠️ No hay URL de audio disponible para esta canción</p>
        <small>Preview URL: {{ playerState.currentTrack.audioUrl || 'No disponible' }}</small>
      </div>
    </div>
  `,
  styles: [`
    .music-player {
      background: linear-gradient(135deg, rgba(30, 60, 114, 0.9), rgba(42, 82, 152, 0.9));
      color: white;
      padding: 15px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 20px;
      height: 100%;
      backdrop-filter: blur(15px);
    }

    .track-info {
      display: flex;
      align-items: center;
      gap: 15px;
      flex: 0 0 300px;
      min-width: 250px;
    }

    .track-image {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      object-fit: cover;
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      flex-shrink: 0;
    }

    .track-details {
      flex: 1;
      min-width: 0;
    }

    .track-details h3 {
      margin: 0 0 4px 0;
      font-size: 1em;
      font-weight: bold;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .track-details p {
      margin: 0;
      opacity: 0.8;
      font-size: 0.9em;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .player-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 10px;
      flex: 0 0 auto;
    }

    .control-btn {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      font-size: 1em;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .control-btn:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }

    .control-btn.active {
      background: rgba(255, 255, 255, 0.4);
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
    }

    .play-pause-btn {
      width: 48px;
      height: 48px;
      font-size: 1.2em;
      background: rgba(255, 255, 255, 0.3);
    }

    .progress-section {
      display: flex;
      align-items: center;
      gap: 10px;
      flex: 1;
      max-width: 400px;
    }

    .progress-bar-container {
      flex: 1;
      position: relative;
    }

    .progress-bar {
      height: 6px;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 3px;
      position: relative;
      overflow: hidden;
    }

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #ff6b6b, #4ecdc4);
      border-radius: 3px;
      transition: width 0.3s ease;
    }

    .progress-slider {
      position: absolute;
      top: -8px;
      left: 0;
      width: 100%;
      height: 20px;
      opacity: 0;
      cursor: pointer;
    }

    .time-display {
      font-size: 0.9em;
      opacity: 0.8;
      min-width: 40px;
      text-align: center;
    }

    .queue-section {
      margin-top: 20px;
      padding-top: 15px;
      border-top: 1px solid rgba(255, 255, 255, 0.2);
    }

    .queue-section h4 {
      margin: 0 0 10px 0;
      font-size: 1em;
      opacity: 0.9;
    }

    .queue-list {
      max-height: 150px;
      overflow-y: auto;
    }

    .queue-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 5px 0;
      font-size: 0.9em;
      opacity: 0.7;
    }

    .queue-item.current {
      opacity: 1;
      font-weight: bold;
      color: #4ecdc4;
    }

    .queue-number {
      width: 20px;
      text-align: center;
      font-size: 0.8em;
    }

    .player-status {
      text-align: center;
      padding: 10px;
      margin-top: 10px;
      border-radius: 5px;
      background: rgba(255, 255, 255, 0.1);
    }

    .player-status.error {
      background: rgba(255, 0, 0, 0.2);
    }

    .player-status.debug {
      background: rgba(255, 165, 0, 0.2);
      font-size: 0.8em;
    }

    .player-status small {
      display: block;
      opacity: 0.7;
      margin-top: 5px;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .music-player {
        padding: 15px;
        margin: 10px;
      }

      .track-info {
        flex-direction: column;
        text-align: center;
      }

      .control-btn {
        width: 45px;
        height: 45px;
        font-size: 1em;
      }

      .play-pause-btn {
        width: 55px;
        height: 55px;
        font-size: 1.3em;
      }
    }
  `]
})
export class MusicPlayerComponent implements OnInit, OnDestroy {
  playerState: PlayerState = {
    status: 'stopped',
    currentTrack: null,
    queue: [],
    currentIndex: 0,
    shuffle: false,
    repeat: false,
    position: 0,
    duration: 0
  };

  currentPosition = 0;
  currentAudioUrl = '';
  isLoading = false;

  private subscription: Subscription = new Subscription();
  private audioElement?: HTMLAudioElement;

  constructor(
    private playerService: PlayerService,
    private youtubeService: YoutubeService
  ) { }

  ngOnInit(): void {
    // Suscribirse al estado del reproductor
    this.subscription.add(
      this.playerService.playerState$.subscribe(state => {
        this.playerState = state;
        this.loadAudioForCurrentTrack();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Carga la URL de audio para la canción actual
   */
  private async loadAudioForCurrentTrack(): Promise<void> {
    if (!this.playerState.currentTrack) {
      return;
    }

    this.isLoading = true;
    console.log('Loading audio for track:', this.playerState.currentTrack.name);

    try {
      // Extraer nombre del artista y nombre de la canción
      const artistName = this.playerState.currentTrack.artist || 'Unknown Artist';
      const trackName = this.playerState.currentTrack.name || 'Unknown Track';

      console.log(`Requesting YouTube audio for: "${trackName}" by "${artistName}"`);

      // Llamar directamente al endpoint de YouTube usando HTTPS para evitar timeouts de NGINX
      const youtubeUrl = `https://${window.location.hostname}:8443/api/youtube/audio?name=${encodeURIComponent(trackName)}&artist=${encodeURIComponent(artistName)}`;
      const response = await fetch(youtubeUrl, {
        method: 'GET',
        // Ignorar certificados SSL auto-firmados en desarrollo
        // En producción, esto debería manejarse con certificados válidos
      });

      if (response.ok) {
        const audioUrl = await response.text();
        if (audioUrl && audioUrl.startsWith('http')) {
          console.log('YouTube audio URL obtained:', audioUrl.substring(0, 80) + '...');
          this.currentAudioUrl = audioUrl;
          this.isLoading = false;
          return;
        }
      }

      // Fallback: intentar usar la URL de preview de Spotify si está disponible
      if (this.playerState.currentTrack.audioUrl) {
        console.log('Fallback to Spotify preview URL:', this.playerState.currentTrack.audioUrl);
        this.currentAudioUrl = this.playerState.currentTrack.audioUrl;
        this.isLoading = false;
        return;
      }

      // Si nada funciona, marcar como error
      console.warn('No audio URL available from any source');
      this.currentAudioUrl = '';
      this.isLoading = false;

    } catch (error) {
      console.error('Error loading audio URL:', error);
      this.currentAudioUrl = '';
      this.isLoading = false;
    }
  }

  /**
   * Toggle play/pause
   */
  togglePlayPause(): void {
    if (this.playerState.status === 'playing') {
      this.playerService.pause().subscribe();
      this.audioElement?.pause();
    } else {
      this.playerService.resume().subscribe();
      this.audioElement?.play();
    }
  }

  /**
   * Reproduce la siguiente canción
   */
  playNext(): void {
    this.playerService.playNext().subscribe();
  }

  /**
   * Reproduce la canción anterior
   */
  playPrevious(): void {
    this.playerService.playPrevious().subscribe();
  }

  /**
   * Toggle shuffle
   */
  toggleShuffle(): void {
    this.playerService.toggleShuffle().subscribe();
  }

  /**
   * Toggle repeat
   */
  toggleRepeat(): void {
    this.playerService.toggleRepeat().subscribe();
  }

  /**
   * Evento cuando se puede reproducir el audio
   */
  onCanPlay(): void {
    this.isLoading = false;
    if (this.playerState.status === 'playing') {
      this.audioElement?.play();
    }
  }

  /**
   * Evento cuando se inicia la carga
   */
  onLoadStart(): void {
    this.isLoading = true;
  }

  /**
   * Actualización del tiempo de reproducción
   */
  onTimeUpdate(event: Event): void {
    const audio = event.target as HTMLAudioElement;
    this.currentPosition = audio.currentTime * 1000; // Convertir a ms
  }

  /**
   * Evento cuando termina la canción (autoplay)
   */
  onTrackEnd(): void {
    console.log('Track ended, triggering autoplay...');
    this.playNext();
  }

  /**
   * Seek en la canción
   */
  onSeek(event: Event): void {
    const input = event.target as HTMLInputElement;
    const seekTime = parseInt(input.value) / 1000; // Convertir a segundos
    if (this.audioElement) {
      this.audioElement.currentTime = seekTime;
    }
  }

  /**
   * Error de imagen
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://via.placeholder.com/80x80?text=🎵';
  }

  /**
   * Error de audio
   */
  onAudioError(event: Event): void {
    console.error('Audio error:', event);
    this.isLoading = false;
  }

  /**
   * Calcula el porcentaje de progreso
   */
  get progressPercentage(): number {
    if (!this.playerState.duration) return 0;
    return (this.currentPosition / this.playerState.duration) * 100;
  }

  /**
   * Formatea el tiempo en mm:ss
   */
  formatTime(timeMs: number): string {
    if (!timeMs) return '0:00';

    const totalSeconds = Math.floor(timeMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  /**
   * Referencia al elemento de audio después de la vista inicializada
   */
  ngAfterViewInit(): void {
    const audioElement = document.querySelector('audio') as HTMLAudioElement;
    if (audioElement) {
      this.audioElement = audioElement;
    }
  }
}