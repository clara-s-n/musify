package com.tfu.backend.auth;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  @RateLimiter(name = "loginLimiter")
  public String login(String email, String password) {
    // Para demo: acepta cualquier password si email termina en "@test.com"
    if (!email.endsWith("@test.com")) {
      throw new IllegalArgumentException("Credenciales inválidas");
    }
    return email;
  }
}
