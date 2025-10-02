export interface SpotifyTrack {
  id: string;
  name: string;
  artists: string;
  album: string;
  imageUrl: string;
  previewUrl: string | null;
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
