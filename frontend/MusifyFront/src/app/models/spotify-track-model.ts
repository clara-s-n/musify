export interface SpotifyTrack {
  id: string;
  name: string;
  artists: string;
  album: string;
  imageUrl: string;
  previewUrl: string | null;

  // Información adicional del artista principal para navegación
  primaryArtistId?: string;
  primaryArtistName?: string;
  primaryArtistSpotifyUrl?: string;
}

export interface SpotifyPlaybackData {
  trackId: string;
  name: string;
  artists: string;
  album: string;
  imageUrl: string;
  previewUrl: string | null;
  durationMs: number | null;
  isPlayable: boolean;
  streamUrl: string | null;
}
