package com.tfu.backend.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Extensión personalizada de UserDetails que incluye información adicional del usuario.
 * Además del username, password y authorities, incluye el email del usuario.
 */
public class CustomUserDetails extends User {

  private final String email;
  private final Long userId;

  /**
   * Constructor para crear un CustomUserDetails con toda la información del usuario.
   * 
   * @param username    Nombre de usuario
   * @param password    Contraseña codificada
   * @param enabled     Si el usuario está habilitado
   * @param accountNonExpired Si la cuenta no ha expirado
   * @param credentialsNonExpired Si las credenciales no han expirado
   * @param accountNonLocked Si la cuenta no está bloqueada
   * @param authorities Roles/autoridades del usuario
   * @param email       Email del usuario
   * @param userId      ID del usuario en la base de datos
   */
  public CustomUserDetails(
      String username,
      String password,
      boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities,
      String email,
      Long userId) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    this.email = email;
    this.userId = userId;
  }

  /**
   * Constructor simplificado que asume cuenta no expirada y no bloqueada.
   * 
   * @param username    Nombre de usuario
   * @param password    Contraseña codificada
   * @param enabled     Si el usuario está habilitado
   * @param authorities Roles/autoridades del usuario
   * @param email       Email del usuario
   * @param userId      ID del usuario en la base de datos
   */
  public CustomUserDetails(
      String username,
      String password,
      boolean enabled,
      Collection<? extends GrantedAuthority> authorities,
      String email,
      Long userId) {
    this(username, password, enabled, true, true, true, authorities, email, userId);
  }

  /**
   * Obtiene el email del usuario.
   * 
   * @return Email del usuario
   */
  public String getEmail() {
    return email;
  }

  /**
   * Obtiene el ID del usuario.
   * 
   * @return ID del usuario
   */
  public Long getUserId() {
    return userId;
  }
}
