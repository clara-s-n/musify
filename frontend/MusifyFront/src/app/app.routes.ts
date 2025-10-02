import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',loadComponent() {
            return import('./home.component/home.component').then((m)=>m.HomeComponent);
        },pathMatch: 'full'
        
    },
    {
        path: 'login',loadComponent() {
            return import('./login.component/login.component').then((m)=>m.LoginComponent);
        },pathMatch: 'full'
    }
];
