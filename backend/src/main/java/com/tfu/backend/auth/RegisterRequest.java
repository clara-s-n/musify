package com.tfu.backend.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de usuario.
 */
@Schema(description = "Datos para registro de nuevo usuario")
public record RegisterRequest(
        @Schema(description = "Nombre de usuario para la cuenta", example = "usuario123", required = true, minLength = 4, maxLength = 50) @NotBlank(message = "El nombre de usuario no puede estar vacío") @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres") @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos") String username,

        @Schema(description = "Email del usuario", example = "usuario@example.com", required = true) @NotBlank(message = "El email no puede estar vacío") @Email(message = "Formato de email inválido") @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Formato de email inválido") String email,

        @Schema(description = "Contraseña para la cuenta", example = "password123", required = true, minLength = 6) @NotBlank(message = "La contraseña no puede estar vacía") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}