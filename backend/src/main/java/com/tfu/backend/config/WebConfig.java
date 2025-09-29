package com.tfu.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS para la aplicación.
 * Esta configuración complementa la de SecurityConfig y se aplica a todos los
 * controladores.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Configura las reglas CORS para todas las rutas.
   * Esta configuración asegura que los controladores respondan adecuadamente a
   * las solicitudes preflight OPTIONS
   * y manejen correctamente los encabezados CORS, incluso antes de que Spring
   * Security intervenga.
   *
   * @param registry Registro de CORS para configurar
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:4200", // Angular dev server
            "http://localhost:8080", // Angular app servida desde Spring Boot
            "http://localhost" // Otros puertos locales
        )
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
        .allowedHeaders(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers")
        .exposedHeaders("Authorization", "Content-Disposition")
        .allowCredentials(true)
        .maxAge(3600);
  }
}