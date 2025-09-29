package com.tfu.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * Expone los endpoints de login, logout y registro.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  /** Servicio de autenticación */
  private final AuthService authService;
  /** Servicio para generación de JWT */
  private final JwtService jwtService;
  /** Servicio para cargar detalles de usuario */
  private final UserDetailsService userDetailsService;

  /**
   * Constructor que inyecta los servicios necesarios.
   * 
   * @param authService        Servicio de autenticación
   * @param jwtService         Servicio de JWT
   * @param userDetailsService Servicio de detalles de usuario
   */
  public AuthController(
      AuthService authService,
      JwtService jwtService,
      UserDetailsService userDetailsService) {
    this.authService = authService;
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Endpoint para login de usuario. Devuelve un JWT si es exitoso.
   * 
   * @param req Datos de login (email y password)
   * @return Token JWT en un DTO
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    try {
      logger.debug("Intento de login para: {}", req.email());

      // Para compatibilidad con el código anterior
      var userId = authService.login(req.email(), req.password());

      // Cargamos los detalles completos del usuario
      UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

      // Generamos un token con los detalles completos
      var token = jwtService.createToken(userDetails);

      logger.info("Login exitoso para: {}", req.email());
      return ResponseEntity.ok(new TokenResponse(token));
    } catch (AuthenticationException e) {
      logger.warn("Error de autenticación: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    } catch (Exception e) {
      logger.error("Error inesperado durante login: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  /**
   * Endpoint para logout. Invalida el token JWT.
   * 
   * @param request Solicitud HTTP que contiene el token JWT en el header
   *                Authorization
   * @return Mensaje de confirmación
   */
  @PostMapping("/logout")
  public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
    try {
      String authHeader = request.getHeader("Authorization");

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        authService.logout(authHeader);
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada correctamente"));
      }

      return ResponseEntity.badRequest().body(new MessageResponse("Token no proporcionado"));
    } catch (Exception e) {
      logger.error("Error durante logout: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("Error al cerrar sesión: " + e.getMessage()));
    }
  }

  /**
   * Endpoint para registro de nuevos usuarios.
   * 
   * @param req Datos de registro (username, email, password)
   * @return Información del usuario registrado
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    try {
      logger.debug("Intento de registro para: {}", req.email());

      AppUser user = authService.register(req.username(), req.email(), req.password());

      RegisterResponse response = new RegisterResponse(
          user.getUsername(),
          user.getEmail(),
          "Usuario registrado correctamente");

      logger.info("Registro exitoso para: {}", req.email());
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (AuthenticationException e) {
      logger.warn("Error de registro: {}", e.getMessage());
      return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
      logger.error("Error inesperado durante registro: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("Error al registrar usuario: " + e.getMessage()));
    }
  }
}

/**
 * DTO para la respuesta de login, contiene el token de acceso.
 */
record TokenResponse(String accessToken) {
}

/**
 * DTO para respuestas simples con mensaje.
 */
record MessageResponse(String message) {
}
