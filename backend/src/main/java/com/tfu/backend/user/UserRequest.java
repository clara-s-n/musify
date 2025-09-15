package com.tfu.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 64, message="El nombre puede tener un máximo de 64 y un mínimo de 2 carácteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 64, message="El apellido puede tener un máximo de 64 y un mínimo de 2 caracteres")
    private String apellido;

    @Email(message = "Email inválido")
    private String email;

    @NotBlank
    private boolean premium;


    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean IsPremium() {return premium; }
    public void setPremium(boolean premium) {this.premium = premium;}


}
