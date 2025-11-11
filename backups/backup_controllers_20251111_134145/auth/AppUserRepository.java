package com.tfu.backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para acceder a los usuarios de la aplicación en la base de datos.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

  /**
   * Busca un usuario por su nombre de usuario.
   * 
   * @param username Nombre de usuario a buscar
   * @return Usuario encontrado o empty si no existe
   */
  Optional<AppUser> findByUsername(String username);

  /**
   * Busca un usuario por su dirección de email.
   * 
   * @param email Email a buscar
   * @return Usuario encontrado o empty si no existe
   */
  Optional<AppUser> findByEmail(String email);
}