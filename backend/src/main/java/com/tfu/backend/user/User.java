package com.tfu.backend.user;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremental en PostgreSQL
    private Long id;

    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;    

    @Column(nullable = false)
    private boolean premium;

    @Column(unique = true ,nullable = false)
    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public boolean isPremium() {return premium; }
    public void setPremium(boolean premium) {this.premium = premium; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
