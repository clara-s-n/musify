package com.tfu.backend.config;

import com.tfu.backend.auth.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad para la aplicación.
 * Define las reglas de acceso y autenticación.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthFilter;

  /**
   * Constructor que inyecta el servicio de detalles de usuario y el filtro JWT.
   * 
   * @param userDetailsService Servicio que proporciona detalles de usuario
   * @param jwtAuthFilter      Filtro para autenticación JWT
   */
  public SecurityConfig(
      UserDetailsService userDetailsService,
      JwtAuthenticationFilter jwtAuthFilter) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthFilter = jwtAuthFilter;
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
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/api/auth/**", "/tracks/**", "/api/tracks/**",
                "/playback/**", "/api/playback/**",
                "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**", "/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html")
            .permitAll()
            .requestMatchers("/music/spotify/**").permitAll() // Hacer públicos los endpoints de Spotify
            .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .userDetailsService(userDetailsService) // Usar directamente UserDetailsService
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Configura el administrador de autenticación.
   * 
   * @param config Configuración de autenticación
   * @return Administrador de autenticación
   * @throws Exception en caso de error de configuración
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
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

  /**
   * Configura CORS para permitir solicitudes desde el frontend Angular.
   * 
   * @return Configuración CORS
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Permitir solicitudes desde cualquier IP (para desarrollo/demo)
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:*", // Cualquier puerto localhost
        "http://*:4200", // Angular dev server desde cualquier IP
        "http://*:8080", // Angular app servida desde cualquier IP
        "http://*" // Permitir cualquier IP (para desarrollo/demo)
    ));

    // Permitir todos los métodos HTTP comunes
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

    // Permitir todos los headers comunes
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers",
        "X-XSRF-TOKEN"));

    // Exponer headers que Angular necesita acceder
    configuration.setExposedHeaders(Arrays.asList(
        "Authorization",
        "Content-Disposition"));

    // Permitir credenciales (cookies, encabezados de autenticación)
    configuration.setAllowCredentials(true);

    // Tiempo de caché para respuestas preflight
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
