package com.tfu.backend.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * Expone el endpoint de login.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
  /** Servicio de autenticación */
  private final AuthService authService;
  /** Servicio para generación de JWT */
  private final JwtService jwtService;

  /**
   * Constructor que inyecta los servicios de autenticación y JWT.
   * 
   * @param a Servicio de autenticación
   * @param j Servicio de JWT
   */
  public AuthController(AuthService a, JwtService j) {
    this.authService = a;
    this.jwtService = j;
  }

  /**
   * Endpoint para login de usuario. Devuelve un JWT si es exitoso.
   * 
   * @param req Datos de login (email y password)
   * @return Token JWT en un DTO
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    var userId = authService.login(req.email(), req.password()); // rate limited
    var token = jwtService.createToken(userId);
    return ResponseEntity.ok(new TokenResponse(token));
  }
}

/**
 * DTO para la respuesta de login, contiene el token de acceso.
 */
record TokenResponse(String accessToken) {
}
