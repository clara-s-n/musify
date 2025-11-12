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
        this.successMessage.set('¡Inicio de sesión exitoso! Redirigiendo...');

        // Actualizamos el estado de autenticación
        this.updateAuthStatus();

        // Esperamos un momento para que el usuario vea el mensaje de éxito
        setTimeout(() => {
          this.successMessage.set('');
          this.router.navigateByUrl(this.returnUrl);
        }, 800);
      },
      error: (error) => {
        console.error('Error en login:', error);

        // Mensaje de error más específico
        let errorMsg = 'Error al iniciar sesión';
        if (error.status === 401) {
          errorMsg = 'Credenciales inválidas. Por favor verifica tu email y contraseña.';
        } else if (error.status === 0) {
          errorMsg = 'No se pudo conectar con el servidor. Por favor verifica tu conexión.';
        } else if (error.message) {
          errorMsg = error.message;
        }

        this.errorMessage.set(errorMsg);

        // Mostrar el mensaje de error durante 2.5 segundos y luego recargar la página
        setTimeout(() => {
          this.errorMessage.set('');
          // Limpiar el formulario antes de recargar
          this.loginForm.reset();
          // Recargar la página para restablecer el estado
          window.location.reload();
        }, 2500);
      }
    });
  }

  onRegister(): void {
    // Reset messages
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.registerForm.invalid) {
      this.errorMessage.set('Por favor completa todos los campos correctamente');
      return;
    }

    // TODO: Implement registration when backend is ready
    this.errorMessage.set('El registro no está disponible en este momento. Por favor contacta al administrador.');

    // Limpiar el mensaje después de 5 segundos
    setTimeout(() => {
      this.errorMessage.set('');
    }, 5000);
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
      this.successMessage.set('Sesión cerrada exitosamente');

      // Limpiar formulario
      this.loginForm.reset();

      // Limpiar el mensaje después de 3 segundos
      setTimeout(() => {
        this.successMessage.set('');
      }, 3000);

    } catch (error) {
      console.error('Error during logout:', error);
      this.errorMessage.set('Error al cerrar sesión');
    }
  }

  /**
   * Helper methods for template validation
   */
  isLoginButtonDisabled(): boolean {
    return this.loginForm.invalid || this.loading();
  }

  isRegisterButtonDisabled(): boolean {
    return this.registerForm.invalid || this.loading();
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

