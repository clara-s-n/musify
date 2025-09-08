package com.tfu.backend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;
  @Value("${jwt.expiration}")
  private long expiration;

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
