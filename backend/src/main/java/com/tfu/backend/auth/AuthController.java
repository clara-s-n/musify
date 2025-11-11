package com.tfu.backend.auth;

import com.tfu.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * Expone los endpoints de login, logout y registro.
 */
@Tag(name = "Autenticación", description = "API para autenticación y registro de usuarios")
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
  @Operation(summary = "Iniciar sesión de usuario", description = "Autentica al usuario y devuelve un token JWT para autorizar solicitudes futuras")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
  })
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
    try {
      logger.debug("Intento de login para: {}", req.email());

      // Para compatibilidad con el código anterior
      var userId = authService.login(req.email(), req.password());

      // Cargamos los detalles completos del usuario
      UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

      // Generamos un token con los detalles completos
      var token = jwtService.createToken(userDetails);

      logger.info("Login exitoso para: {}", req.email());
      return ResponseEntity.ok(ApiResponse.success(new TokenResponse(token), "Login exitoso"));
    } catch (AuthenticationException e) {
      logger.warn("Error de autenticación: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Error de autenticación", e.getMessage()));
    } catch (Exception e) {
      logger.error("Error inesperado durante login: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error interno del servidor", "Error al procesar la solicitud de login"));
    }
  }

  /**
   * Endpoint para logout. Invalida el token JWT.
   * 
   * @param request Solicitud HTTP que contiene el token JWT en el header
   *                Authorization
   * @return Mensaje de confirmación
   */
  @Operation(summary = "Cerrar sesión de usuario", description = "Invalida el token JWT actual del usuario", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token no proporcionado", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json"))
  })
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    try {
      String authHeader = request.getHeader("Authorization");

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        authService.logout(authHeader);
        // Clear security context
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.successMessage("Sesión cerrada correctamente"));
      }

      return ResponseEntity.badRequest().body(ApiResponse.error("Token no proporcionado"));
    } catch (Exception e) {
      logger.error("Error durante logout: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al cerrar sesión", e.getMessage()));
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
