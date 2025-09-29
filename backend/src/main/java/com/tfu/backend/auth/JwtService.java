package com.tfu.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Servicio para la generación y validación de tokens JWT.
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
   * Almacena los tokens invalidados (ej: por cierre de sesión)
   */
  private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

  /**
   * Genera un token JWT para el usuario indicado con claims adicionales.
   * 
   * @param userDetails Detalles del usuario autenticado
   * @return Token JWT firmado
   */
  public String createToken(UserDetails userDetails) {
    return createToken(new HashMap<>(), userDetails);
  }

  /**
   * Genera un token JWT para el usuario indicado con claims adicionales.
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
        .setId(UUID.randomUUID().toString())
        .signWith(key)
        .compact();
  }

  /**
   * Genera un token JWT con claims personalizados adicionales.
   * 
   * @param extraClaims Claims adicionales a incluir en el token
   * @param userDetails Detalles del usuario autenticado
   * @return Token JWT firmado
   */
  public String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    List<String> roles = userDetails.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .toList();

    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .claim("roles", roles)
        .setIssuedAt(new Date())
        .setId(UUID.randomUUID().toString()) // Identificador único del token (jti)
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Extrae el nombre de usuario del token JWT.
   * 
   * @param token Token JWT
   * @return Nombre de usuario
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrae la fecha de expiración del token JWT.
   * 
   * @param token Token JWT
   * @return Fecha de expiración
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrae un claim específico del token JWT.
   * 
   * @param token          Token JWT
   * @param claimsResolver Función para extraer el claim deseado
   * @return El claim extraído
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extrae todos los claims del token JWT.
   * 
   * @param token Token JWT
   * @return Todos los claims
   */
  private Claims extractAllClaims(String token) {
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Comprueba si un token JWT es válido para el usuario especificado.
   * 
   * @param token       Token JWT a validar
   * @param userDetails Detalles del usuario
   * @return true si el token es válido, false en caso contrario
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && !isTokenBlacklisted(token));
  }

  /**
   * Verifica si un token está en la lista negra (invalidado).
   * 
   * @param token Token JWT
   * @return true si el token está en la lista negra, false en caso contrario
   */
  public boolean isTokenBlacklisted(String token) {
    String tokenId = extractClaim(token, Claims::getId);
    return blacklistedTokens.containsKey(tokenId);
  }

  /**
   * Invalida un token añadiéndolo a la lista negra.
   * 
   * @param token Token JWT a invalidar
   */
  public void invalidateToken(String token) {
    String tokenId = extractClaim(token, Claims::getId);
    Date expiration = extractExpiration(token);
    blacklistedTokens.put(tokenId, expiration);

    // Limpiar tokens expirados de la lista negra
    purgeExpiredTokens();
  }

  /**
   * Elimina tokens expirados de la lista negra para optimizar memoria.
   */
  private void purgeExpiredTokens() {
    Date now = new Date();
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
  }

  /**
   * Comprueba si un token está expirado.
   * 
   * @param token Token JWT
   * @return true si el token está expirado, false en caso contrario
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Valida un token JWT y devuelve sus claims si es válido.
   * 
   * @param token Token JWT a validar
   * @return Claims del token si es válido
   * @throws JwtException si el token no es válido
   */
  public Jws<Claims> validateToken(String token) {
    try {
      var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

      // Verificar si el token está en la lista negra
      if (isTokenBlacklisted(token)) {
        throw new JwtException("Token ha sido invalidado");
      }

      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
    } catch (SignatureException e) {
      throw new JwtException("Firma JWT inválida");
    } catch (MalformedJwtException e) {
      throw new JwtException("JWT malformado");
    } catch (ExpiredJwtException e) {
      throw new JwtException("JWT expirado");
    } catch (UnsupportedJwtException e) {
      throw new JwtException("JWT no soportado");
    } catch (IllegalArgumentException e) {
      throw new JwtException("JWT claims string está vacío");
    }
  }
}
