import { Component, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="user-menu" *ngIf="currentUser">
      <div class="user-info" (click)="toggleDropdown()">
        <div class="user-avatar">
           <span class="avatar-icon">👤</span>
        </div>
        <span class="user-name">{{ currentUser.username || currentUser.email }}</span>
        <span class="dropdown-arrow" [class.rotated]="isDropdownOpen">▼</span>
      </div>

      <div class="dropdown-menu" [class.open]="isDropdownOpen">
        <div class="dropdown-item user-details">
          <strong>{{ currentUser.username || 'Usuario' }}</strong>
          <small>{{ currentUser.email }}</small>
        </div>
        <hr class="dropdown-divider">
        <button class="dropdown-item action-item" (click)="viewProfile()">
          👤 Mi Perfil
        </button>
        <button class="dropdown-item action-item" (click)="viewSettings()">
          ⚙️ Configuración
        </button>
        <hr class="dropdown-divider">
        <button 
          class="dropdown-item action-item logout-item" 
          (click)="logout()"
          [disabled]="isLoggingOut"
        >
          <span *ngIf="!isLoggingOut">🚪 Cerrar Sesión</span>
          <span *ngIf="isLoggingOut" class="logout-loading">⏳ Cerrando...</span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div class="loading-placeholder" *ngIf="!currentUser">
      <div class="loading-avatar"></div>
    </div>

    <!-- Click Outside Handler -->
    <div 
      class="dropdown-backdrop" 
      *ngIf="isDropdownOpen" 
      (click)="closeDropdown()"
    ></div>
  `,
  styles: [`
    .user-menu {
      position: relative;
      z-index: 100;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 25px;
      cursor: pointer;
      transition: all 0.3s ease;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);
    }

    .user-info:hover {
      background: rgba(255, 255, 255, 0.15);
      transform: translateY(-1px);
    }

    .user-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #4CAF50, #2196F3);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 14px;
    }

    .user-name {
      color: white;
      font-weight: 500;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dropdown-arrow {
      color: #ccc;
      font-size: 12px;
      transition: transform 0.3s ease;
    }

    .dropdown-arrow.rotated {
      transform: rotate(180deg);
    }

    .dropdown-menu {
      position: absolute;
      top: 100%;
      right: 0;
      margin-top: 8px;
      background: linear-gradient(135deg, rgba(20, 20, 30, 0.95), rgba(10, 10, 20, 0.95));
      backdrop-filter: blur(15px);
      border-radius: 15px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
      min-width: 220px;
      opacity: 0;
      visibility: hidden;
      transform: translateY(-10px);
      transition: all 0.3s ease;
    }

    .dropdown-menu.open {
      opacity: 1;
      visibility: visible;
      transform: translateY(0);
    }

    .dropdown-item {
      display: block;
      width: 100%;
      padding: 12px 18px;
      background: transparent;
      border: none;
      color: white;
      text-align: left;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 14px;
    }

    .dropdown-item.user-details {
      padding: 15px 18px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      cursor: default;
    }

    .dropdown-item.user-details strong {
      display: block;
      margin-bottom: 4px;
      color: white;
    }

    .dropdown-item.user-details small {
      color: #ccc;
      font-size: 12px;
    }

    .dropdown-item.action-item:hover:not(:disabled) {
      background: rgba(255, 255, 255, 0.1);
      color: #4CAF50;
    }

    .dropdown-item.logout-item {
      color: #ff6b6b;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }

    .dropdown-item.logout-item:hover:not(:disabled) {
      background: rgba(255, 107, 107, 0.1);
      color: #ff5252;
    }

    .dropdown-item:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .dropdown-divider {
      border: none;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      margin: 8px 0;
    }

    .logout-loading {
      animation: pulse 1.5s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }

    .loading-placeholder {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 16px;
    }

    .loading-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(90deg, #333, #555, #333);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    @keyframes shimmer {
      0% { background-position: -200% 0; }
      100% { background-position: 200% 0; }
    }

    .dropdown-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 99;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .user-name {
        display: none;
      }

      .dropdown-menu {
        right: -10px;
        min-width: 200px;
      }

      .user-info {
        padding: 8px 12px;
      }
    }
  `]
})
export class UserMenuComponent implements OnInit {
  currentUser: User | null = null;
  isDropdownOpen: boolean = false;
  isLoggingOut: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Use effect to reactively update when user changes
    effect(() => {
      const userSignal = this.authService.getCurrentUser();
      this.currentUser = userSignal();
    });
  }

  ngOnInit(): void {
    // Initial load is handled by effect in constructor
  }

  /**
   * Get user initials for avatar
   */
  getUserInitials(): string {
    if (!this.currentUser) return '?';

    const name = this.currentUser.username || this.currentUser.email;
    const parts = name.split(' ');

    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    } else {
      return name.substring(0, 2).toUpperCase();
    }
  }

  /**
   * Toggle dropdown menu
   */
  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  /**
   * Close dropdown menu
   */
  closeDropdown(): void {
    this.isDropdownOpen = false;
  }

  /**
   * Navigate to user profile
   */
  viewProfile(): void {
    this.closeDropdown();
    // TODO: Implement profile page
    console.log('Navigate to profile');
  }

  /**
   * Navigate to settings
   */
  viewSettings(): void {
    this.closeDropdown();
    // TODO: Implement settings page
    console.log('Navigate to settings');
  }

  /**
   * Logout user
   */
  logout(): void {
    if (this.isLoggingOut) return;

    this.isLoggingOut = true;
    this.closeDropdown();

    this.authService.logout().subscribe({
      next: (response: any) => {
        console.log('Logout successful:', response);
        this.router.navigate(['/login']);
      },
      error: (error: any) => {
        console.error('Logout error:', error);
        // Even if logout fails on backend, redirect to login
        this.router.navigate(['/login']);
      },
      complete: () => {
        this.isLoggingOut = false;
      }
    });
  }
}