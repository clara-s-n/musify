export interface CatalogTrack {
  id: number;
  title: string;
  artist: string;
  album: string;
  releaseYear: number | null;
  genre: string | null;
  durationSeconds: number | null;
  premium: boolean;
  coverUrl: string | null;
}
