package com.tfu.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de login.
 * Contiene el email y la contraseña del usuario.
 */
public record LoginRequest(
    @Email @NotBlank String email, // Email del usuario (obligatorio y formato válido)
    @NotBlank String password // Contraseña del usuario (obligatoria)
) {
}
