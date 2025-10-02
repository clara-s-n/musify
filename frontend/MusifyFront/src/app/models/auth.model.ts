/**
 * User interface representing the authenticated user
 */
export interface User {
  id?: string;
  username: string;
  email: string;
  roles?: string[];
}

/**
 * Login request payload
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Authentication response from the backend
 */
export interface AuthResponse {
  accessToken: string;
  message?: string;
  status?: string;
  data?: {
    accessToken: string;
  };
}

/**
 * Error response from the backend
 */
export interface ErrorResponse {
  message: string;
  details?: string;
  status?: string;
}