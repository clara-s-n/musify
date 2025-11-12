import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
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
        [src]="currentAudioUrl || null"
        (loadstart)="onLoadStart()"
        (loadedmetadata)="onLoadedMetadata($event)"
        (canplay)="onCanPlay()"
        (timeupdate)="onTimeUpdate($event)"
        (ended)="onTrackEnd()"
        (error)="onAudioError($event)"
        preload="metadata"
      ></audio>

      <!-- Center Section: Controls + Progress -->
      <div class="center-section">
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
      </div>

      <!-- Right Section: Download Button -->
      <div class="right-section">
        <button 
          class="control-btn download-btn"
          (click)="downloadTrack()"
          [disabled]="!currentAudioUrl"
          title="Download"
        >
          ⬇️ Descargar
        </button>
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

      <!-- Loading/Error States (Compact) -->
      <div class="player-status-compact" *ngIf="isLoading">
        <div class="loading-spinner"></div>
        <span class="loading-text">Cargando...</span>
      </div>

      <div class="player-status-compact error" *ngIf="playerState.status === 'error' && !isLoading">
        <span>❌ Error</span>
      </div>
    </div>
  `,
  styles: [`
    .music-player {
      background: linear-gradient(135deg, rgba(30, 60, 114, 0.95), rgba(42, 82, 152, 0.95));
      color: white;
      padding: 10px 20px;
      display: grid;
      grid-template-columns: minmax(200px, 280px) auto minmax(180px, 220px);
      align-items: center;
      gap: 30px;
      height: 100%;
      backdrop-filter: blur(15px);
      box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.3);
    }

    .track-info {
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 0;
    }

    .track-image {
      width: 48px;
      height: 48px;
      border-radius: 6px;
      object-fit: cover;
      box-shadow: 0 3px 10px rgba(0,0,0,0.3);
      flex-shrink: 0;
    }

    .track-details {
      flex: 1;
      min-width: 0;
      overflow: hidden;
    }

    .track-details h3 {
      margin: 0 0 3px 0;
      font-size: 0.9em;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .track-details p {
      margin: 0;
      opacity: 0.75;
      font-size: 0.8em;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .center-section {
      display: flex;
      flex-direction: column;
      gap: 8px;
      width: 100%;
      min-width: 0;
    }

    .right-section {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      min-width: 0;
    }

    .player-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 8px;
    }

    .control-btn {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      border-radius: 50%;
      width: 38px;
      height: 38px;
      font-size: 0.95em;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .control-btn:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }

    .control-btn.active {
      background: rgba(255, 255, 255, 0.4);
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
    }

    .control-btn:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }

    .control-btn:disabled:hover {
      transform: none;
      background: rgba(255, 255, 255, 0.2);
    }

    .download-btn {
      background: rgba(76, 175, 80, 0.4);
      border-radius: 20px;
      padding: 8px 16px;
      width: auto;
      height: auto;
      font-size: 0.9em;
      font-weight: 500;
      white-space: nowrap;
    }

    .download-btn:hover:not(:disabled) {
      background: rgba(76, 175, 80, 0.6);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
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
      width: 100%;
    }

    .progress-bar-container {
      flex: 1;
      position: relative;
      padding: 3px 0;
    }

    .progress-bar {
      height: 8px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 4px;
      position: relative;
      overflow: visible;
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
      cursor: pointer;
    }

    .progress-bar:hover {
      height: 10px;
      margin-top: -1px;
    }

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #1db954, #1ed760, #4ecdc4);
      border-radius: 4px;
      transition: width 0.1s linear;
      box-shadow: 0 2px 8px rgba(29, 185, 84, 0.4);
      position: relative;
    }

    .progress-fill::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 12px;
      height: 12px;
      background: white;
      border-radius: 50%;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      opacity: 0;
      transition: opacity 0.2s;
    }

    .progress-bar:hover .progress-fill::after {
      opacity: 1;
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
      font-size: 0.85em;
      opacity: 1;
      min-width: 42px;
      text-align: center;
      font-weight: 600;
      font-family: 'Courier New', monospace;
      letter-spacing: 0.5px;
      color: #fff;
      text-shadow: 0 1px 3px rgba(0,0,0,0.8);
      background: rgba(0, 0, 0, 0.2);
      padding: 4px 8px;
      border-radius: 4px;
      flex-shrink: 0;
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

    /* Compact Loading Status */
    .player-status-compact {
      position: absolute;
      top: 50%;
      right: 20px;
      transform: translateY(-50%);
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 12px;
      background: rgba(0, 0, 0, 0.3);
      border-radius: 20px;
      font-size: 0.85em;
      z-index: 10;
    }

    .player-status-compact.error {
      background: rgba(255, 0, 0, 0.3);
    }

    .loading-spinner {
      width: 14px;
      height: 14px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #4CAF50;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .loading-text {
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
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
export class MusicPlayerComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('audioElement', { static: false }) audioElementRef!: ElementRef<HTMLAudioElement>;

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

  // Getter para acceder al elemento de audio
  private get audioElement(): HTMLAudioElement | null {
    return this.audioElementRef?.nativeElement || null;
  }

  constructor(
    private playerService: PlayerService,
    private youtubeService: YoutubeService
  ) { }

  ngOnInit(): void {
    // Suscribirse al estado del reproductor
    this.subscription.add(
      this.playerService.playerState$.subscribe(state => {
        const previousTrack = this.playerState.currentTrack;
        this.playerState = state;

        // Solo actualizar el audio si cambió la canción o si tenemos una nueva URL
        if (this.playerState.currentTrack) {
          const trackChanged = !previousTrack || previousTrack.id !== this.playerState.currentTrack.id;
          const hasNewAudioUrl = this.playerState.currentTrack.audioUrl &&
            this.currentAudioUrl !== this.playerState.currentTrack.audioUrl;

          if (trackChanged || hasNewAudioUrl) {
            this.updateAudioFromState();
          }
        }
      })
    );
  }

  ngAfterViewInit(): void {
    // El elemento audio está ahora disponible
    console.log('Audio element initialized:', this.audioElement ? 'Available' : 'Not available');
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Actualiza la URL de audio desde el estado del PlayerService
   */
  private updateAudioFromState(): void {
    if (!this.playerState.currentTrack) {
      this.currentAudioUrl = '';
      return;
    }

    // Si el PlayerService ya tiene la URL de audio, usarla
    if (this.playerState.currentTrack.audioUrl && this.playerState.currentTrack.audioUrl.startsWith('http')) {
      console.log('Using audio URL from PlayerService:', this.playerState.currentTrack.audioUrl.substring(0, 80) + '...');
      this.currentAudioUrl = this.playerState.currentTrack.audioUrl;
      this.isLoading = false;

      // Intentar reproducir automáticamente
      setTimeout(() => {
        if (this.audioElement && this.playerState.status === 'playing') {
          this.audioElement.play().catch(err => {
            console.warn('Auto-play prevented by browser:', err);
          });
        }
      }, 100);
    } else {
      // Si no hay URL aún, mostrar estado de carga
      console.log('Waiting for audio URL from PlayerService...');
      this.isLoading = true;
    }
  }

  /**
   * Toggle play/pause
   */
  togglePlayPause(): void {
    const audioEl = this.audioElement;

    if (!audioEl) {
      console.error('❌ Audio element not available yet. Waiting for initialization...');
      // Intentar nuevamente en el próximo tick
      setTimeout(() => {
        if (this.audioElement) {
          this.togglePlayPause();
        } else {
          alert('El reproductor de audio aún no está listo. Espera un momento e intenta de nuevo.');
        }
      }, 100);
      return;
    }

    if (this.playerState.status === 'playing') {
      console.log('🛑 Pausing playback');
      this.playerService.pause().subscribe();
      audioEl.pause();
    } else {
      console.log('▶️ Starting playback via user interaction');

      // Verificar que tengamos URL antes de intentar reproducir
      if (!this.currentAudioUrl) {
        alert('No hay audio disponible para reproducir. Selecciona una canción primero.');
        return;
      }

      this.playerService.resume().subscribe();

      // User interaction allows play() to succeed
      audioEl.play()
        .then(() => {
          console.log('✅ Playback started successfully via user click');
        })
        .catch(error => {
          console.error('❌ Error starting playback:', error);
          alert(`No se pudo iniciar la reproducción:\n${error.message}\n\nURL: ${this.currentAudioUrl.substring(0, 80)}...`);
        });
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
   * Evento cuando se cargan los metadatos del audio (incluida la duración)
   */
  onLoadedMetadata(event: Event): void {
    const audio = event.target as HTMLAudioElement;
    if (audio.duration && audio.duration !== Infinity && !isNaN(audio.duration)) {
      const duration = audio.duration * 1000; // Convertir a ms
      this.playerState = {
        ...this.playerState,
        duration: duration
      };
      console.log('📊 Metadata loaded - Duration:', this.formatTime(duration));
    }
  }

  /**
   * Evento cuando se puede reproducir el audio
   */
  onCanPlay(): void {
    this.isLoading = false;

    // Capturar la duración del audio cuando esté disponible
    if (this.audioElement && this.audioElement.duration &&
      this.audioElement.duration !== Infinity &&
      !isNaN(this.audioElement.duration)) {
      const duration = this.audioElement.duration * 1000; // Convertir a ms
      this.playerState = {
        ...this.playerState,
        duration: duration
      };
      console.log('Audio ready to play, duration:', this.formatTime(duration), 'current status:', this.playerState.status);
    } else {
      console.log('Audio ready to play, current status:', this.playerState.status);
    }

    // Intentar reproducir si el estado es 'playing'
    if (this.playerState.status === 'playing' && this.audioElement) {
      this.audioElement.play()
        .then(() => {
          console.log('Audio playing successfully');
        })
        .catch(err => {
          console.warn('Auto-play prevented by browser. User interaction required:', err);
          // Puedes mostrar un mensaje al usuario para que haga clic en play
        });
    }
  }

  /**
   * Descarga la canción actual
   */
  downloadTrack(): void {
    if (!this.currentAudioUrl || !this.playerState.currentTrack) {
      console.warn('No audio URL available for download');
      return;
    }

    const track = this.playerState.currentTrack;
    const fileName = `${track.artist} - ${track.name}.mp3`;

    console.log('Downloading track:', fileName);

    // Crear un elemento <a> temporal para descargar
    const link = document.createElement('a');
    link.href = this.currentAudioUrl;
    link.download = fileName;
    link.target = '_blank';

    // Agregar al DOM, hacer clic y remover
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    console.log('Download initiated for:', fileName);
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

    // Actualizar la duración si no está establecida o es diferente
    if (audio.duration && audio.duration !== Infinity && !isNaN(audio.duration)) {
      const newDuration = audio.duration * 1000; // Convertir a ms
      if (this.playerState.duration !== newDuration) {
        this.playerState = {
          ...this.playerState,
          duration: newDuration
        };
      }
    }
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
    const audioElement = event.target as HTMLAudioElement;

    // Ignorar error de src vacío (código 4 = MEDIA_ELEMENT_ERROR)
    if (audioElement.error?.code === 4 && !audioElement.currentSrc) {
      // Este error es esperado cuando aún no hay URL de audio
      return;
    }

    console.error('Audio playback error:', {
      currentSrc: audioElement.currentSrc,
      error: audioElement.error,
      networkState: audioElement.networkState,
      readyState: audioElement.readyState
    });

    // Limpiar la URL de audio con error
    if (this.currentAudioUrl) {
      console.warn('Failed to load audio from:', this.currentAudioUrl.substring(0, 80) + '...');
    }

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
}