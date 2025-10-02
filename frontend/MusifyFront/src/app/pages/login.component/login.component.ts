import { Component, ViewChild, ElementRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-login.component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  @ViewChild('container') container!: ElementRef;

  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  // Form groups
  loginForm!: FormGroup;
  registerForm!: FormGroup;

  // UI state signals
  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  isLoggedIn = signal(false);
  currentUser = signal<User | null>(null);

  // Return URL after login
  private returnUrl: string = '/';

  ngOnInit(): void {
    // Get return url from route parameters or default to home
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    // Initialize forms
    this.initForms();

    // Subscribe to loading state from auth service
    this.authService.loading$.subscribe(loading => {
      this.loading.set(loading);
    });

    // Check authentication status
    this.updateAuthStatus();
  }

  /**
   * Update the component's authentication status based on the AuthService
   */
  updateAuthStatus(): void {
    const isAuth = this.authService.isAuthenticated();
    this.isLoggedIn.set(isAuth);

    if (isAuth) {
      // Get current user info
      this.currentUser.set(this.authService.getCurrentUser()());

      // If we're on the login page and already authenticated, redirect to home
      if (this.router.url === '/login') {
        this.router.navigateByUrl(this.returnUrl);
      }
    } else {
      this.currentUser.set(null);
    }
  }

  singUpClick() {
    if (this.container) {
      this.container.nativeElement.classList.add("right-panel-active");
    }
  }

  singInClick() {
    if (this.container) {
      this.container.nativeElement.classList.remove("right-panel-active");
    }
  }

  onLogin(): void {
    // Reset error messages
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.loginForm.invalid) {
      this.errorMessage.set('Por favor ingresa un email y contraseña válidos');
      return;
    }

    const { email, password } = this.loginForm.value;

    console.log('Intentando login con email:', email);

    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('Login exitoso:', response);
        this.successMessage.set('Inicio de sesión exitoso');

        // Actualizamos el estado de autenticación
        this.updateAuthStatus();

        // Redirigir al usuario a la página solicitada o al home
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (error) => {
        console.error('Error en login:', error);
        this.errorMessage.set(error.message || 'Error al iniciar sesión');
      }
    });
  }

  onRegister(): void {
    // This would be implemented when backend register endpoint is used
    // For now, just show a message that registration is not available
    this.errorMessage.set('Registration is not available yet');
  }

  /**
   * Handle user logout
   */
  onLogout(): void {
    // Reset messages
    this.errorMessage.set('');
    this.successMessage.set('');

    try {
      // Call logout in AuthService
      this.authService.logout();

      // Update component state
      this.updateAuthStatus();

      // Show success message
      this.successMessage.set('Logged out successfully');

      // Redirect to login page
      this.router.navigateByUrl('/login');
    } catch (error) {
      console.error('Error during logout:', error);
      this.errorMessage.set('Error during logout');
    }
  }

  private initForms(): void {
    // Login form with validation
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Registration form with validation
    this.registerForm = this.fb.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }
}

