package com.tfu.backend.auth;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.JwtException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador global para el manejo de excepciones relacionadas con la autenticación.
 * Centraliza la gestión de errores para el módulo de autenticación.
 */
@RestControllerAdvice(value = "com.tfu.backend.auth", basePackageClasses = GlobalExceptionHandler.class)
@org.springframework.core.annotation.Order(1)
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Maneja excepciones de autenticación.
   * 
   * @param ex      La excepción capturada
   * @param request La solicitud HTTP
   * @return Respuesta con el mensaje de error
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleAuthenticationException(
      AuthenticationException ex,
      HttpServletRequest request) {

    logger.warn("Error de autenticación: {} en ruta: {}", ex.getMessage(), request.getRequestURI());

    Map<String, String> error = new HashMap<>();
    error.put("error", "Error de autenticación");
    error.put("message", ex.getMessage());
    error.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Maneja excepciones de JWT.
   * 
   * @param ex      La excepción capturada
   * @param request La solicitud HTTP
   * @return Respuesta con el mensaje de error
   */
  @ExceptionHandler(JwtException.class)
  public ResponseEntity<Map<String, String>> handleJwtException(
      JwtException ex,
      HttpServletRequest request) {

    logger.warn("Error en token JWT: {} en ruta: {}", ex.getMessage(), request.getRequestURI());

    Map<String, String> error = new HashMap<>();
    error.put("error", "Error de token");
    error.put("message", ex.getMessage());
    error.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Maneja excepciones de acceso denegado.
   * 
   * @param ex      La excepción capturada
   * @param request La solicitud HTTP
   * @return Respuesta con el mensaje de error
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request) {

    logger.warn("Acceso denegado: {} en ruta: {}", ex.getMessage(), request.getRequestURI());

    Map<String, String> error = new HashMap<>();
    error.put("error", "Acceso denegado");
    error.put("message", "No tiene permisos suficientes para acceder a este recurso");
    error.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Maneja errores de validación de datos en solicitudes.
   * 
   * @param ex La excepción capturada
   * @return Respuesta con los errores de validación
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Error de validación");
    response.put("details", validationErrors);

    logger.warn("Errores de validación: {}", validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Maneja cualquier otra excepción no capturada específicamente.
   * 
   * @param ex      La excepción capturada
   * @param request La solicitud HTTP
   * @return Respuesta con el mensaje de error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralException(
      Exception ex,
      HttpServletRequest request) {

    logger.error("Error inesperado: {} en ruta: {}", ex.getMessage(), request.getRequestURI(), ex);

    Map<String, String> error = new HashMap<>();
    error.put("error", "Error interno del servidor");
    error.put("message", "Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde.");
    error.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}