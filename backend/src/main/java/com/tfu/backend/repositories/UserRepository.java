package com.tfu.backend.repositories;

import com.tfu.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de acceso a datos de usuarios.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Busca un usuario por su dirección de correo electrónico.
   * 
   * @param email Correo electrónico a buscar
   * @return Usuario encontrado o vacío si no existe
   */
  Optional<User> findByEmail(String email);

  /**
   * Verifica si existe un usuario con el correo electrónico especificado.
   * 
   * @param email Correo electrónico a verificar
   * @return true si existe, false en caso contrario
   */
  boolean existsByEmail(String email);

  /**
   * Busca usuarios por nombre o apellido que contengan el texto especificado.
   * La búsqueda es case-insensitive.
   * 
   * @param texto Texto a buscar en nombre o apellido
   * @return Lista de usuarios que coinciden con el criterio
   */
  @Query("SELECT u FROM User u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))")
  List<User> findByNombreContainingOrApellidoContaining(@Param("texto") String texto);

  /**
   * Obtiene todos los usuarios premium.
   * 
   * @return Lista de usuarios premium
   */
  List<User> findByPremiumTrue();

  /**
   * Obtiene todos los usuarios no premium.
   * 
   * @return Lista de usuarios no premium
   */
  List<User> findByPremiumFalse();
}