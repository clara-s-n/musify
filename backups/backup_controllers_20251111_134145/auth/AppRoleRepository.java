package com.tfu.backend.auth;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para acceder a los roles de usuario en la base de datos.
 */
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

  /**
   * Busca todos los roles asociados a un nombre de usuario.
   * 
   * @param username Nombre de usuario
   * @return Lista de roles del usuario
   */
  List<AppRole> findByUsername(String username);
}