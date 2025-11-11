import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategorizedSearchService, CategorizedSearchResponse } from '../../services/categorized-search.service';
import { CategorizedSearchResultsComponent } from '../categorized-search-results/categorized-search-results.component';

@Component({
  selector: 'app-enhanced-search',
  standalone: true,
  imports: [CommonModule, FormsModule, CategorizedSearchResultsComponent],
  template: `
    <div class="search-container">
      
      <!-- Search Bar -->
      <div class="search-bar-container">
        <div class="search-input-wrapper">
          <input 
            type="text" 
            [(ngModel)]="searchQuery"
            (keyup.enter)="performSearch()"
            placeholder="Buscar canciones, artistas, álbumes..."
            class="search-input"
            [disabled]="isLoading"
          >
          <button 
            (click)="performSearch()" 
            class="search-btn"
            [disabled]="isLoading || !searchQuery.trim()"
          >
            <span *ngIf="!isLoading">🔍</span>
            <span *ngIf="isLoading" class="loading-spinner">⏳</span>
          </button>
        </div>
        
        <!-- Search Filters -->
        <div class="search-filters" *ngIf="showFilters">
          <label class="filter-option">
            <input type="checkbox" [(ngModel)]="filters.songs" (change)="onFilterChange()">
            <span>Canciones</span>
          </label>
          <label class="filter-option">
            <input type="checkbox" [(ngModel)]="filters.albums" (change)="onFilterChange()">
            <span>Álbumes</span>
          </label>
          <label class="filter-option">
            <input type="checkbox" [(ngModel)]="filters.artists" (change)="onFilterChange()">
            <span>Artistas</span>
          </label>
          <label class="filter-option">
            <input type="checkbox" [(ngModel)]="filters.concerts" (change)="onFilterChange()">
            <span>Conciertos</span>
          </label>
        </div>
        
        <button 
          (click)="toggleFilters()" 
          class="filter-toggle-btn"
        >
          {{ showFilters ? 'Ocultar filtros' : 'Mostrar filtros' }}
        </button>
      </div>

      <!-- Search Stats -->
      <div class="search-stats" *ngIf="searchResults && !isLoading">
        <p>
          <strong>{{ getTotalResults() }}</strong> resultados para 
          <em>"{{ lastSearchQuery }}"</em>
          <span *ngIf="searchTime"> en {{ searchTime }}ms</span>
        </p>
      </div>

      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <div class="loading-spinner-large">⏳</div>
        <p>Buscando música...</p>
      </div>

      <!-- Error State -->
      <div class="error-container" *ngIf="error && !isLoading">
        <p class="error-message">❌ {{ error }}</p>
        <button (click)="performSearch()" class="retry-btn">🔄 Reintentar</button>
      </div>

      <!-- Search Results -->
      <div class="results-container" *ngIf="searchResults && !isLoading && !error">
        <app-categorized-search-results [results]="filteredResults"></app-categorized-search-results>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="!searchResults && !isLoading && !error">
        <div class="empty-state-content">
          <h2>🎵 Descubre música increíble</h2>
          <p>Busca por canciones, artistas, álbumes o conciertos</p>
          <div class="suggested-searches">
            <p>Búsquedas sugeridas:</p>
            <button 
              *ngFor="let suggestion of suggestedSearches" 
              (click)="searchSuggestion(suggestion)"
              class="suggestion-btn"
            >
              {{ suggestion }}
            </button>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .search-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    /* Search Bar */
    .search-bar-container {
      background: white;
      border-radius: 15px;
      padding: 25px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.1);
      margin-bottom: 30px;
    }

    .search-input-wrapper {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .search-input {
      flex: 1;
      padding: 15px 20px;
      border: 2px solid #e0e0e0;
      border-radius: 25px;
      font-size: 1.1em;
      outline: none;
      transition: all 0.3s ease;
    }

    .search-input:focus {
      border-color: #4CAF50;
      box-shadow: 0 0 10px rgba(76, 175, 80, 0.2);
    }

    .search-input:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .search-btn {
      padding: 15px 25px;
      background: #4CAF50;
      color: white;
      border: none;
      border-radius: 25px;
      font-size: 1.2em;
      cursor: pointer;
      transition: all 0.3s ease;
      min-width: 60px;
    }

    .search-btn:hover:not(:disabled) {
      background: #45a049;
      transform: translateY(-2px);
    }

    .search-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }

    .loading-spinner {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    /* Search Filters */
    .search-filters {
      display: flex;
      gap: 20px;
      flex-wrap: wrap;
      margin-bottom: 15px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 10px;
    }

    .filter-option {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-size: 1em;
      user-select: none;
    }

    .filter-option input[type="checkbox"] {
      width: 18px;
      height: 18px;
      cursor: pointer;
    }

    .filter-toggle-btn {
      background: #007bff;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 20px;
      cursor: pointer;
      font-size: 0.9em;
      transition: background 0.3s ease;
    }

    .filter-toggle-btn:hover {
      background: #0056b3;
    }

    /* Search Stats */
    .search-stats {
      background: #f8f9fa;
      padding: 15px 20px;
      border-radius: 10px;
      margin-bottom: 20px;
      border-left: 4px solid #4CAF50;
    }

    .search-stats p {
      margin: 0;
      color: #666;
    }

    /* Loading State */
    .loading-container {
      text-align: center;
      padding: 60px 20px;
    }

    .loading-spinner-large {
      font-size: 3em;
      animation: spin 1s linear infinite;
      margin-bottom: 20px;
    }

    /* Error State */
    .error-container {
      text-align: center;
      padding: 40px 20px;
      background: #ffebee;
      border-radius: 10px;
      margin-bottom: 20px;
    }

    .error-message {
      color: #c62828;
      font-size: 1.1em;
      margin-bottom: 15px;
    }

    .retry-btn {
      background: #f44336;
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 25px;
      cursor: pointer;
      font-size: 1em;
      transition: background 0.3s ease;
    }

    .retry-btn:hover {
      background: #d32f2f;
    }

    /* Empty State */
    .empty-state {
      text-align: center;
      padding: 80px 20px;
    }

    .empty-state-content h2 {
      color: #333;
      margin-bottom: 15px;
      font-size: 2em;
    }

    .empty-state-content p {
      color: #666;
      font-size: 1.2em;
      margin-bottom: 30px;
    }

    .suggested-searches {
      background: #f8f9fa;
      padding: 25px;
      border-radius: 15px;
      margin-top: 30px;
    }

    .suggested-searches p {
      margin-bottom: 15px;
      font-weight: bold;
      color: #333;
    }

    .suggestion-btn {
      background: #e3f2fd;
      color: #1976d2;
      border: 1px solid #bbdefb;
      padding: 8px 16px;
      margin: 5px;
      border-radius: 20px;
      cursor: pointer;
      font-size: 0.9em;
      transition: all 0.3s ease;
    }

    .suggestion-btn:hover {
      background: #2196f3;
      color: white;
      transform: translateY(-2px);
    }

    /* Results Container */
    .results-container {
      animation: fadeIn 0.5s ease-in;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .search-container {
        padding: 15px;
      }

      .search-bar-container {
        padding: 20px;
      }

      .search-input-wrapper {
        flex-direction: column;
      }

      .search-filters {
        flex-direction: column;
        gap: 10px;
      }

      .suggested-searches {
        padding: 20px;
      }

      .suggestion-btn {
        display: block;
        width: 100%;
        margin: 8px 0;
      }
    }
  `]
})
export class EnhancedSearchComponent implements OnInit {
  searchQuery: string = '';
  lastSearchQuery: string = '';
  searchResults: CategorizedSearchResponse | null = null;
  filteredResults: CategorizedSearchResponse | null = null;
  isLoading: boolean = false;
  error: string | null = null;
  searchTime: number | null = null;
  showFilters: boolean = false;

  filters = {
    songs: true,
    albums: true,
    artists: true,
    concerts: true
  };

  suggestedSearches = [
    'Bad Bunny',
    'Taylor Swift',
    'Rock en español',
    'Música latina',
    'Pop internacional',
    'Jazz clásico'
  ];

  constructor(private categorizedSearchService: CategorizedSearchService) { }

  ngOnInit(): void {
    // Component initialization
  }

  /**
   * Realiza la búsqueda categorizada
   */
  performSearch(): void {
    if (!this.searchQuery.trim()) {
      return;
    }

    this.isLoading = true;
    this.error = null;
    this.lastSearchQuery = this.searchQuery.trim();

    const startTime = performance.now();

    this.categorizedSearchService.searchCategorized(this.lastSearchQuery).subscribe({
      next: (results: CategorizedSearchResponse) => {
        this.searchResults = results;
        this.applyFilters();
        this.searchTime = Math.round(performance.now() - startTime);
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Search error:', error);
        this.error = 'Error al realizar la búsqueda. Por favor, inténtalo de nuevo.';
        this.isLoading = false;
        this.searchTime = null;
      }
    });
  }

  /**
   * Busca una sugerencia
   */
  searchSuggestion(suggestion: string): void {
    this.searchQuery = suggestion;
    this.performSearch();
  }

  /**
   * Alterna la visibilidad de los filtros
   */
  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  /**
   * Maneja cambios en los filtros
   */
  onFilterChange(): void {
    this.applyFilters();
  }

  /**
   * Aplica los filtros a los resultados
   */
  private applyFilters(): void {
    if (!this.searchResults) {
      this.filteredResults = null;
      return;
    }

    this.filteredResults = {
      songs: this.filters.songs ? this.searchResults.songs : [],
      albums: this.filters.albums ? this.searchResults.albums : [],
      artists: this.filters.artists ? this.searchResults.artists : [],
      concerts: this.filters.concerts ? this.searchResults.concerts : []
    };
  }

  /**
   * Calcula el total de resultados
   */
  getTotalResults(): number {
    if (!this.filteredResults) return 0;

    return (
      (this.filteredResults.songs?.length || 0) +
      (this.filteredResults.albums?.length || 0) +
      (this.filteredResults.artists?.length || 0) +
      (this.filteredResults.concerts?.length || 0)
    );
  }
}