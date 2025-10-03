package com.tfu.backend.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de login.
 * Contiene el email y la contraseña del usuario.
 */
@Schema(description = "Datos para autenticación de usuario")
public record LoginRequest(
                @Schema(description = "Email del usuario", example = "usuario@example.com", required = true) @NotBlank(message = "Email no puede estar vacío") @Email(message = "Formato de email inválido") @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Formato de email inválido") String email,

                @Schema(description = "Contraseña del usuario", example = "password123", required = true, minLength = 6) @NotBlank(message = "Contraseña no puede estar vacía") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}
