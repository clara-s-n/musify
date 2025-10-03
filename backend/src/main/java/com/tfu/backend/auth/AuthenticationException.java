package com.tfu.backend.auth;

/**
 * Excepción personalizada para errores de autenticación.
 */
public class AuthenticationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}