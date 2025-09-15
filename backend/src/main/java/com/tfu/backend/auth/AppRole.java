package com.tfu.backend.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un rol de usuario en la aplicación.
 * Se mapea con la tabla app_roles en la base de datos.
 */
@Entity
@Table(name = "app_roles")
public class AppRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String role;

  // Constructores
  public AppRole() {
  }

  public AppRole(Long id, String username, String role) {
    this.id = id;
    this.username = username;
    this.role = role;
  }

  // Getters y setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}