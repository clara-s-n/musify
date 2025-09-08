package com.tfu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
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
