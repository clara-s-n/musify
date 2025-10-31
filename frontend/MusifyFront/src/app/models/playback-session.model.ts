import { CatalogTrack } from './catalog-track.model';

export interface PlaybackSession {
  streamUrl: string;
  currentTrack: CatalogTrack | null;
  previousTrack: CatalogTrack | null;
  nextTrack: CatalogTrack | null;
  recommendations: CatalogTrack[];
  timeToPlayMs: number;
}
