package com.tfu.backend.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio que proporciona detalles de usuario para Spring Security
 * consultando la base de datos.
 */
@Service
public class DbUserDetailsService implements UserDetailsService {

  private final AppUserRepository userRepository;
  private final AppRoleRepository roleRepository;

  /**
   * Constructor que inyecta los repositorios necesarios.
   * 
   * @param userRepository Repositorio de usuarios
   * @param roleRepository Repositorio de roles
   */
  public DbUserDetailsService(AppUserRepository userRepository, AppRoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  /**
   * Carga los detalles de usuario por nombre de usuario.
   * Método requerido por la interfaz UserDetailsService.
   * 
   * @param username Nombre de usuario a buscar
   * @return Detalles del usuario para Spring Security
   * @throws UsernameNotFoundException si el usuario no existe
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Cargar roles del usuario
    List<AppRole> roles = roleRepository.findByUsername(username);
    List<GrantedAuthority> authorities = new ArrayList<>();

    // Convertir roles a autoridades de Spring Security
    for (AppRole role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRole()));
    }

    // Crear objeto UserDetails con la información del usuario y sus roles
    return new User(
        user.getUsername(),
        user.getPassword(),
        user.isEnabled(),
        true, // accountNonExpired
        true, // credentialsNonExpired
        true, // accountNonLocked
        authorities);
  }
}