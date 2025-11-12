import { Injectable, Signal, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, of, tap, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, User } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/auth`;

  // User state signal for Angular's reactivity
  private currentUserSignal = signal<User | null>(null);

  // Loading state
  private loadingSubject = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean> = this.loadingSubject.asObservable();

  // JWT token key in sessionStorage
  private readonly TOKEN_KEY = 'auth_token';

  constructor() {
    // Check if a token exists in sessionStorage on service initialization
    this.checkTokenOnStartup();
  }

  /**
   * Login user with email and password
   * @param email User email
   * @param password User password
   * @returns Observable of the auth response containing the JWT token
   */
  login(email: string, password: string): Observable<AuthResponse> {
    const loginRequest: LoginRequest = { email, password };
    this.loadingSubject.next(true);

    console.log('Attempting login to:', `${this.API_URL}/login`);

    return this.http.post<any>(`${this.API_URL}/login`, loginRequest)
      .pipe(
        tap(response => {
          console.log('Login response:', response);

          // La estructura de respuesta del backend es:
          // {
          //   success: boolean,
          //   message: string,
          //   data: { accessToken: string },
          //   timestamp: string,
          //   error: string | null
          // }

          // Primero, verificamos si la respuesta fue exitosa
          if (!response || response.error) {
            console.error('Error en respuesta de login:', response);
            throw new Error(response?.message || response?.error || 'Error de autenticación');
          }

          // Intentamos obtener el token de diferentes maneras para ser flexibles
          let token = null;

          if (response.data?.accessToken) {
            // Estructura completa
            token = response.data.accessToken;
          } else if (response.accessToken) {
            // En caso de que venga directamente en la respuesta
            token = response.accessToken;
          } else if (typeof response === 'string') {
            // En caso de que devuelva directamente el token como string
            token = response;
          }

          if (!token) {
            console.error('No se encontró token en la respuesta', response);
            throw new Error('Error de autenticación: No se recibió token');
          }

          console.log('Token extraído correctamente');

          // Store token in sessionStorage (more secure than localStorage)
          sessionStorage.setItem(this.TOKEN_KEY, token);

          // Parse user info from JWT and update the user signal
          this.setUserFromToken(token);
        }),
        // Solo extraemos el contenido útil para el componente
        map(response => {
          if (response.data) return response.data;
          return response; // Si no tiene estructura esperada, devolvemos todo
        }),
        catchError(this.handleError),
        tap(() => this.loadingSubject.next(false))
      );
  }

  /**
   * Register a new user
   * @param username User's username
   * @param email User email
   * @param password User password
   * @returns Observable of the auth response containing user data and JWT token
   */
  register(username: string, email: string, password: string): Observable<any> {
    const registerRequest = { username, email, password };
    this.loadingSubject.next(true);

    console.log('Attempting registration to:', `${this.API_URL}/register`);

    return this.http.post<any>(`${this.API_URL}/register`, registerRequest)
      .pipe(
        tap(response => {
          console.log('Register response:', response);

          // Verificar si la respuesta fue exitosa
          if (!response || response.error) {
            console.error('Error en respuesta de registro:', response);
            throw new Error(response?.message || response?.error || 'Error en el registro');
          }

          // Extraer el token de la respuesta
          let token = null;

          if (response.data?.accessToken) {
            token = response.data.accessToken;
          } else if (response.accessToken) {
            token = response.accessToken;
          }

          if (!token) {
            console.error('No se encontró token en la respuesta de registro', response);
            throw new Error('Error en el registro: No se recibió token');
          }

          console.log('Usuario registrado, token extraído correctamente');

          // Guardar token en sessionStorage
          sessionStorage.setItem(this.TOKEN_KEY, token);

          // Actualizar información del usuario
          this.setUserFromToken(token);
        }),
        map(response => {
          if (response.data) return response.data;
          return response;
        }),
        catchError(this.handleError),
        tap(() => this.loadingSubject.next(false))
      );
  }

  /**
   * Logout user and clear stored data
   */
  logout(): Observable<any> {
    const token = this.getToken();

    // Clear local state immediately
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.currentUserSignal.set(null);
    this.loadingSubject.next(false);

    // Call backend logout endpoint if token exists
    if (token) {
      return this.http.post(`${this.API_URL}/logout`, {}, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).pipe(
        catchError((error) => {
          console.warn('Error during logout:', error);
          // Even if backend logout fails, we already cleared local state
          return of({ success: true, message: 'Logout completed locally' });
        })
      );
    }

    // Return observable for consistency
    return of({ success: true, message: 'Logout completed' });
  }

  /**
   * Quick logout without backend call (for compatibility)
   */
  logoutLocal(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.currentUserSignal.set(null);
    this.loadingSubject.next(false);
  }

  /**
   * Check if user is authenticated with a valid token
   */
  isAuthenticated(): boolean {
    const token = this.getToken();

    // If no token exists, user is not authenticated
    if (!token) {
      return false;
    }

    try {
      // Check if token is expired using our custom decoder
      const decodedToken = this.parseJwt(token);
      const isExpired = decodedToken.exp * 1000 < Date.now();

      // If token is expired, clear it
      if (isExpired) {
        this.logout();
        return false;
      }

      return true;
    } catch (error) {
      // If token is invalid, clear it
      this.logout();
      return false;
    }
  }

  /**
   * Get the current user data as a readonly signal
   */
  getCurrentUser(): Signal<User | null> {
    return this.currentUserSignal.asReadonly();
  }

  /**
   * Get the stored JWT token
   */
  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check for existing token on startup and initialize user if token exists
   */
  private checkTokenOnStartup(): void {
    const token = this.getToken();
    if (token && this.isTokenValid(token)) {
      this.setUserFromToken(token);
    } else if (token) {
      // Clear invalid token
      this.logout();
    }
  }

  /**
   * Parse the JWT token and extract user information
   */
  private setUserFromToken(token: string): void {
    try {
      const decodedToken = this.parseJwt(token);

      const user: User = {
        id: decodedToken.sub,
        username: decodedToken.username || decodedToken.preferred_username || '',
        email: decodedToken.email || '',
        roles: decodedToken.roles || decodedToken.authorities || []
      };

      this.currentUserSignal.set(user);
    } catch (error) {
      console.error('Error decoding token', error);
    }
  }

  /**
   * Check if a token is valid and not expired
   */
  private isTokenValid(token: string): boolean {
    try {
      const decodedToken = this.parseJwt(token);
      return decodedToken.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  /**
   * Parse a JWT token without using external libraries
   * @param token JWT token string
   * @returns Decoded token payload
   */
  private parseJwt(token: string): any {
    try {
      // Split the token into header, payload, and signature
      const base64Url = token.split('.')[1];
      // Replace characters that are not valid for base64 URL encoding
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      // Decode the base64 string
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      // Parse the JSON payload
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error parsing JWT token', error);
      throw error;
    }
  }

  /**
   * Handle HTTP errors from the API
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('Error en la solicitud HTTP:', error);

    let errorMessage = 'Ocurrió un error desconocido';

    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente
      errorMessage = `Error: ${error.error.message}`;
      console.error('Error del lado del cliente:', error.error.message);
    } else {
      // Error del lado del servidor
      console.error(
        `Código de error ${error.status}, ` +
        `Cuerpo: ${JSON.stringify(error.error)}`
      );

      // Intentamos extraer el mensaje de error del formato específico de la API
      if (error.error && typeof error.error === 'object') {
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        } else if (error.error.data && error.error.data.message) {
          errorMessage = error.error.data.message;
        }
      }

      // Mensajes específicos según el código de estado
      if (error.status === 401) {
        errorMessage = 'Email o contraseña inválidos';
      } else if (error.status === 403) {
        errorMessage = 'No tiene permisos para acceder a este recurso';
      } else if (error.status === 404) {
        errorMessage = 'El recurso solicitado no existe';
      } else if (error.status === 0) {
        errorMessage = 'No se puede conectar con el servidor. Compruebe su conexión a Internet.';
      } else if (error.status >= 500) {
        errorMessage = 'Error en el servidor. Inténtelo de nuevo más tarde.';
      }
    }

    this.loadingSubject.next(false);
    return throwError(() => new Error(errorMessage));
  }
}