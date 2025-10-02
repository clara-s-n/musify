package com.tfu.backend.auth;

/**
 * DTO para la respuesta de registro.
 */
public record RegisterResponse(
    String username,
    String email,
    String message) {
}