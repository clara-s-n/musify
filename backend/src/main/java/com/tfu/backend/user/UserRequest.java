package com.tfu.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitudes de creación o actualización de usuarios.
 */
public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 64, message = "El nombre debe tener entre 2 y 64 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 64, message = "El apellido debe tener entre 2 y 64 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String email;

    @NotNull(message = "El estado premium es obligatorio")
    private Boolean premium;

    // Constructores
    public UserRequest() {
    }

    public UserRequest(String nombre, String apellido, String email, Boolean premium) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.premium = premium;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean IsPremium() {
        return premium != null && premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", premium=" + premium +
                '}';
    }
}
