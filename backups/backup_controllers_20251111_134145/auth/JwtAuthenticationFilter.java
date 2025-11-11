package com.tfu.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que valida el token en cada solicitud.
 * Extiende OncePerRequestFilter para garantizar que se ejecuta una vez por
 * solicitud.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Constructor que inyecta los servicios necesarios.
   *
   * @param jwtService         Servicio para manipular tokens JWT
   * @param userDetailsService Servicio para cargar detalles del usuario
   */
  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Método principal que se ejecuta en cada solicitud.
   * Extrae y valida el token JWT, y si es válido, autentica al usuario.
   *
   * @param request     Solicitud HTTP
   * @param response    Respuesta HTTP
   * @param filterChain Cadena de filtros
   * @throws ServletException Si ocurre un error en el servlet
   * @throws IOException      Si ocurre un error de E/S
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // Si no hay header de Authorization o no es Bearer, continua la cadena de
    // filtros
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extrae el token (elimina "Bearer " del principio)
    jwt = authHeader.substring(7);

    try {
      // Extrae el username del token
      username = jwtService.extractUsername(jwt);

      // Si hay un username y no hay autenticación en el contexto de seguridad
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        // Valida el token para este usuario
        if (jwtService.isTokenValid(jwt, userDetails)) {
          // Crea una autenticación y la establece en el contexto de seguridad
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

          // Establece detalles de la solicitud web
          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      logger.error("Error validando el token JWT: " + e.getMessage());
      // No establecer autenticación si hay un error
    }

    // Continúa la cadena de filtros
    filterChain.doFilter(request, response);
  }
}