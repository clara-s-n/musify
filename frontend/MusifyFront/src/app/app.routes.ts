import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent() {
      return import('./pages/home.component/home.component').then((m) => m.HomeComponent);
    },
    pathMatch: 'full',
    canActivate: [authGuard] // Protect home route
  },
  {
    path: 'login',
    loadComponent() {
      return import('./pages/login.component/login.component').then((m) => m.LoginComponent);
    },
    pathMatch: 'full',
  },
  {
    path: 'result',
    loadComponent() {
      return import('./pages/results.component/results.component').then((m) => m.ResultsComponent);
    },
    pathMatch: 'full',
    canActivate: [authGuard] // Protect results route
  },
  // Add a catch-all route to redirect to home
  {
    path: '**',
    redirectTo: ''
  }
];
