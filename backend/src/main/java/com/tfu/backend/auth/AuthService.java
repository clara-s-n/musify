package com.tfu.backend.auth;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación de usuarios.
 * Implementa la lógica de negocio para el login y otras operaciones de
 * autenticación.
 */
@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final AppUserRepository userRepository;
  private final AppRoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * Constructor que inyecta los componentes necesarios.
   * 
   * @param userRepository        Repositorio de usuarios
   * @param roleRepository        Repositorio de roles
   * @param passwordEncoder       Codificador de contraseñas
   * @param jwtService            Servicio JWT
   * @param authenticationManager Gestor de autenticación
   */
  public AuthService(
      AppUserRepository userRepository,
      AppRoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  /**
   * Realiza el login del usuario validando sus credenciales contra la base de
   * datos.
   * Aplica rate limiting para prevenir ataques de fuerza bruta.
   * 
   * @param email    Email del usuario
   * @param password Contraseña del usuario
   * @return El token JWT si las credenciales son válidas
   * @throws AuthenticationException si las credenciales no son válidas o el
   *                                 usuario está deshabilitado
   */
  @RateLimiter(name = "loginLimiter")
  public String authenticate(String email, String password) {
    try {
      logger.debug("Intentando autenticar usuario con email: {}", email);

      // Buscar usuario por email
      AppUser user = userRepository.findByEmail(email)
          .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));

      // Verificar si el usuario está habilitado
      if (!user.isEnabled()) {
        logger.warn("Intento de login con usuario deshabilitado: {}", email);
        throw new AuthenticationException("Usuario deshabilitado");
      }

      // Autenticar con Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getUsername(), password));

      // Obtener detalles del usuario autenticado
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      // Generar token JWT
      return jwtService.createToken(userDetails);

    } catch (DisabledException e) {
      logger.warn("Error de autenticación: Usuario deshabilitado - {}", email);
      throw new AuthenticationException("Usuario deshabilitado", e);
    } catch (BadCredentialsException e) {
      logger.warn("Error de autenticación: Credenciales inválidas - {}", email);
      throw new AuthenticationException("Credenciales inválidas", e);
    } catch (Exception e) {
      logger.error("Error inesperado durante autenticación: {}", e.getMessage(), e);
      throw new AuthenticationException("Error de autenticación: " + e.getMessage(), e);
    }
  }

  /**
   * Realiza el login del usuario validando sus credenciales contra la base de
   * datos utilizando el método anterior, pero devolviendo solo el username.
   * 
   * @param email    Email del usuario
   * @param password Contraseña del usuario
   * @return El nombre de usuario si las credenciales son válidas
   * @throws AuthenticationException si las credenciales no son válidas
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
        .orElseThrow(() -> new AuthenticationException("Credenciales inválidas"));
  }

  /**
   * Invalida un token JWT (logout).
   * 
   * @param token Token JWT a invalidar
   */
  public void logout(String token) {
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    try {
      jwtService.invalidateToken(token);
      logger.debug("Token invalidado exitosamente");
    } catch (Exception e) {
      logger.error("Error al invalidar token: {}", e.getMessage(), e);
      throw new AuthenticationException("Error al cerrar sesión", e);
    }
  }

  /**
   * Registra un nuevo usuario en el sistema.
   * 
   * @param username Nombre de usuario
   * @param email Email del usuario
   * @param password Contraseña del usuario
   * @return El usuario creado
   * @throws AuthenticationException si el usuario o email ya existen
   */
  public AppUser register(String username, String email, String password) {
    try {
      logger.debug("Intentando registrar nuevo usuario: {}", email);

      // Verificar si el email ya existe
      if (userRepository.findByEmail(email).isPresent()) {
        logger.warn("Intento de registro con email existente: {}", email);
        throw new AuthenticationException("El email ya está registrado");
      }

      // Verificar si el username ya existe
      if (userRepository.findByUsername(username).isPresent()) {
        logger.warn("Intento de registro con username existente: {}", username);
        throw new AuthenticationException("El nombre de usuario ya está en uso");
      }

      // Crear nuevo usuario
      AppUser newUser = new AppUser();
      newUser.setUsername(username);
      newUser.setEmail(email);
      // Codificar la contraseña con BCrypt
      newUser.setPassword(passwordEncoder.encode(password));
      newUser.setEnabled(true);

      // Guardar en la base de datos
      AppUser savedUser = userRepository.save(newUser);
      logger.info("Usuario registrado exitosamente: {}", email);

      return savedUser;

    } catch (AuthenticationException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Error inesperado durante registro: {}", e.getMessage(), e);
      throw new AuthenticationException("Error al registrar usuario: " + e.getMessage(), e);
    }
  }


}
