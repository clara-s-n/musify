import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UserMenuComponent } from '../user-menu/user-menu.component';
import { EnhancedSearchComponent } from '../enhanced-search/enhanced-search.component';
import { SearchBarComponent } from '../search-bar.component/search-bar.component';

@Component({
  selector: 'app-logo',
  imports: [CommonModule, UserMenuComponent, EnhancedSearchComponent, SearchBarComponent],
  templateUrl: './logo.component.html',
  styleUrl: './logo.component.css',
})
export class LogoComponent {
  private router = inject(Router);
  showEnhancedSearch = false;

  goHome() {
    this.router.navigate(['']);
  }

  toggleSearchMode() {
    this.showEnhancedSearch = !this.showEnhancedSearch;
  }

  searchHome(results: any) {
    // Handle search results
    console.log('Search results:', results);
  }
}
