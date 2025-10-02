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

          // Extract the token from the response structure based on API format
          // The backend returns { data: { accessToken: '...' }, status: '...', message: '...' }
          const token = response.data?.accessToken;

          if (!token) {
            console.error('No token found in response', response);
            throw new Error('Authentication failed: No token received');
          }

          // Store token in sessionStorage (more secure than localStorage)
          sessionStorage.setItem(this.TOKEN_KEY, token);

          // Parse user info from JWT and update the user signal
          this.setUserFromToken(token);
        }),
        map(response => response.data),
        catchError(this.handleError),
        tap(() => this.loadingSubject.next(false))
      );
  }

  /**
   * Logout user and clear stored data
   */
  logout(): void {
    // Clear token from storage
    sessionStorage.removeItem(this.TOKEN_KEY);

    // Reset user state
    this.currentUserSignal.set(null);

    // Optional: Call backend logout endpoint if needed
    // this.http.post(`${this.API_URL}/logout`, {}).subscribe();
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
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.status === 401) {
        errorMessage = 'Invalid email or password';
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Error Code: ${error.status}, Message: ${error.message}`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}