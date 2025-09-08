package com.tfu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación.
 * Define las reglas de acceso y autenticación.
 */
@Configuration
public class SecurityConfig {
  /**
   * Configura el filtro de seguridad HTTP.
   * Permite acceso público a endpoints de autenticación, docs y monitoreo.
   * El resto requiere autenticación básica.
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
        .httpBasic(Customizer.withDefaults()); // para no complicar el demo
    return http.build();
  }
}
