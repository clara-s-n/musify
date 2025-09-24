package com.tfu.backend.auth;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación de usuarios.
 * Implementa la lógica de negocio para el login y otras operaciones de
 * autenticación.
 */
@Service
public class AuthService {

  private final AppUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Constructor que inyecta los componentes necesarios.
   * 
   * @param userRepository  Repositorio de usuarios
   * @param passwordEncoder Codificador de contraseñas
   */
  public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Realiza el login del usuario validando sus credenciales contra la base de
   * datos.
   * Aplica rate limiting para prevenir ataques de fuerza bruta.
   * 
   * @param email    Email del usuario
   * @param password Contraseña del usuario
   * @return El nombre de usuario si las credenciales son válidas
   * @throws IllegalArgumentException si las credenciales no son válidas
   */
  @RateLimiter(name = "loginLimiter")
  public String login(String email, String password) {
    // Buscar usuario por email en la base de datos
    return userRepository.findByEmail(email)
        .filter(user -> {
          // Verificar si la contraseña coincide
          // Para passwords con formato {noop}, extraemos la parte después de {noop}
          String storedPassword = user.getPassword();
          if (storedPassword.startsWith("{noop}")) {
            // Para el demo, si la contraseña está almacenada con {noop}, hacemos
            // comparación directa
            return password.equals(storedPassword.substring(6));
          } else {
            // Si no, usamos el codificador de contraseñas
            return passwordEncoder.matches(password, storedPassword);
          }
        })
        .map(AppUser::getUsername)
        .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
  }
}
