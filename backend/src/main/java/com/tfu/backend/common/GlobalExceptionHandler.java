package com.tfu.backend.common;

import com.tfu.backend.auth.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * Proporciona un manejo centralizado y consistente de errores.
 */
@RestControllerAdvice(value = "com.tfu.backend.common", basePackageClasses = GlobalExceptionHandler.class)
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Maneja excepciones de autenticación.
   * 
   * @param ex Excepción de autenticación.
   * @return Respuesta de error con estado 401.
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
    logger.error("Error de autenticación: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Error de autenticación", ex.getMessage()));
  }

  /**
   * Maneja errores de validación de argumentos.
   * 
   * @param ex Excepción de validación.
   * @return Respuesta con detalles de errores de validación.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    logger.warn("Error de validación: {}", errors);
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("Error de validación", "Hay errores en los datos enviados"));
  }

  /**
   * Maneja errores de parámetros faltantes en la petición.
   * 
   * @param ex Excepción de parámetro faltante.
   * @return Respuesta de error.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Object>> handleMissingParams(MissingServletRequestParameterException ex) {
    logger.warn("Parámetro faltante: {}", ex.getParameterName());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("Parámetro requerido no encontrado",
            "El parámetro '" + ex.getParameterName() + "' es obligatorio"));
  }

  /**
   * Maneja errores de tipo de parámetros incorrectos.
   * 
   * @param ex Excepción de tipo incorrecto.
   * @return Respuesta de error.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    logger.warn("Tipo de parámetro incorrecto: {}", ex.getName());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("Tipo de parámetro incorrecto",
            "El parámetro '" + ex.getName() + "' debe ser de tipo " +
                ex.getRequiredType().getSimpleName()));
  }

  /**
   * Maneja errores de cuerpo de petición inválido.
   * 
   * @param ex Excepción de lectura HTTP.
   * @return Respuesta de error.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
    logger.warn("Cuerpo de petición inválido: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("Formato de datos inválido",
            "El cuerpo de la petición no tiene un formato JSON válido"));
  }

  /**
   * Maneja errores de integridad de datos (unicidad, restricciones, etc.)
   * 
   * @param ex Excepción de integridad de datos.
   * @return Respuesta de error.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
    logger.error("Error de integridad de datos: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("Error de integridad de datos",
            "La operación viola una restricción de la base de datos"));
  }

  /**
   * Maneja cualquier otra excepción no contemplada.
   * 
   * @param ex Excepción general.
   * @return Respuesta de error.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiResponse<Object>> handleAllUncaughtException(Exception ex) {
    logger.error("Error interno del servidor: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("Error interno del servidor",
            "Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde."));
  }
}