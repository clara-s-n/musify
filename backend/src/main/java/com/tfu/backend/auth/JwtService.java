package com.tfu.backend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio para la generación de tokens JWT.
 */
@Service
public class JwtService {
  /** Secreto para firmar el JWT (inyectado desde configuración) */
  @Value("${jwt.secret}")
  private String secret;
  /**
   * Tiempo de expiración del token en segundos (inyectado desde configuración)
   */
  @Value("${jwt.expiration}")
  private long expiration;

  /**
   * Genera un token JWT para el usuario indicado.
   * 
   * @param subject Identificador del usuario
   * @return Token JWT firmado
   */
  public String createToken(String subject) {
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(key)
        .compact();
  }
}
