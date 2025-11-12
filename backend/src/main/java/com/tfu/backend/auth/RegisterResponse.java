package com.tfu.backend.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para la respuesta de registro.
 * Contiene la información del usuario registrado y un token JWT.
 */
@Schema(description = "Respuesta después de registrar un usuario exitosamente")
public record RegisterResponse(
                @Schema(description = "ID del usuario creado", example = "1") 
                Long id,

                @Schema(description = "Nombre de usuario", example = "usuario123") 
                String username,

                @Schema(description = "Email del usuario", example = "usuario@example.com") 
                String email,

                @Schema(description = "Token JWT de acceso") 
                String accessToken) {
}
