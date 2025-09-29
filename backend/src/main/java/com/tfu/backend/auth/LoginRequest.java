package com.tfu.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de login.
 * Contiene el email y la contraseña del usuario.
 */
public record LoginRequest(
        @NotBlank(message = "Email no puede estar vacío") @Email(message = "Formato de email inválido") @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Formato de email inválido") String email,
        @NotBlank(message = "Contraseña no puede estar vacía") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}
