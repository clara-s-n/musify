import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MusicPlayerComponent } from './components/music-player/music-player.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MusicPlayerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('MusifyFront');
}
