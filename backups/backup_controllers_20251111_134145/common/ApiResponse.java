package com.tfu.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Clase genérica para respuestas de API.
 * Proporciona una estructura consistente para todas las respuestas.
 * 
 * @param <T> Tipo de datos contenidos en la respuesta.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private final boolean success;
  private final String message;
  private final T data;
  private final LocalDateTime timestamp;
  private final String error;

  private ApiResponse(boolean success, String message, T data, String error) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
    this.error = error;
  }

  /**
   * Crea una respuesta de éxito con datos.
   * 
   * @param <T>     Tipo de datos.
   * @param data    Datos de la respuesta.
   * @param message Mensaje descriptivo opcional.
   * @return ApiResponse con éxito.
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, message, data, null);
  }

  /**
   * Crea una respuesta de éxito con datos sin mensaje.
   * 
   * @param <T>  Tipo de datos.
   * @param data Datos de la respuesta.
   * @return ApiResponse con éxito.
   */
  public static <T> ApiResponse<T> success(T data) {
    return success(data, null);
  }

  /**
   * Crea una respuesta de éxito solo con mensaje.
   * 
   * @param <T>     Tipo de datos.
   * @param message Mensaje descriptivo.
   * @return ApiResponse con éxito.
   */
  public static <T> ApiResponse<T> successMessage(String message) {
    return success(null, message);
  }

  /**
   * Crea una respuesta de error.
   * 
   * @param <T>          Tipo de datos.
   * @param message      Mensaje de error.
   * @param errorDetails Detalles adicionales del error.
   * @return ApiResponse con error.
   */
  public static <T> ApiResponse<T> error(String message, String errorDetails) {
    return new ApiResponse<>(false, message, null, errorDetails);
  }

  /**
   * Crea una respuesta de error simple.
   * 
   * @param <T>     Tipo de datos.
   * @param message Mensaje de error.
   * @return ApiResponse con error.
   */
  public static <T> ApiResponse<T> error(String message) {
    return error(message, null);
  }

  // Getters
  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getError() {
    return error;
  }
}