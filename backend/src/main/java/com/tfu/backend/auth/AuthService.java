package com.tfu.backend.auth;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación de usuarios.
 */
@Service
public class AuthService {
  /**
   * Realiza el login del usuario. Aplica rate limiting.
   * Para demo: acepta cualquier password si el email termina en "@test.com".
   * 
   * @param email    Email del usuario
   * @param password Contraseña del usuario
   * @return El email si es válido
   * @throws IllegalArgumentException si las credenciales no son válidas
   */
  @RateLimiter(name = "loginLimiter")
  public String login(String email, String password) {
    // Para demo: acepta cualquier password si email termina en "@test.com"
    if (!email.endsWith("@test.com")) {
      throw new IllegalArgumentException("Credenciales inválidas");
    }
    return email;
  }
}
