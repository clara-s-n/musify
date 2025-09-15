package com.tfu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación.
 * Define las reglas de acceso y autenticación.
 */
@Configuration
public class SecurityConfig {

  private final UserDetailsService userDetailsService;

  /**
   * Constructor que inyecta el servicio de detalles de usuario.
   * 
   * @param userDetailsService Servicio que proporciona detalles de usuario
   */
  public SecurityConfig(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  /**
   * Configura el filtro de seguridad HTTP.
   * Permite acceso público a endpoints de autenticación, docs y monitoreo.
   * El resto requiere autenticación.
   * 
   * @param http Configuración de seguridad HTTP
   * @return Cadena de filtros de seguridad
   * @throws Exception en caso de error de configuración
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .userDetailsService(userDetailsService); // Usamos nuestro servicio de detalles de usuario
    return http.build();
  }

  /**
   * Proporciona un codificador de contraseñas.
   * Usa el codificador por defecto de Spring Security que soporta varios
   * formatos.
   * 
   * @return Codificador de contraseñas
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
