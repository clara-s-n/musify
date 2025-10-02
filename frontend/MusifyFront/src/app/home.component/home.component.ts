import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { SearchBarComponent } from '../components/search-bar.component/search-bar.component';

@Component({
  selector: 'app-home.component',
  imports: [MatCardModule, MatButtonModule, SearchBarComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {}
