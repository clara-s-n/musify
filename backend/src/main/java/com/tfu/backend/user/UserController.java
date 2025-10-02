package com.tfu.backend.user;

import com.tfu.backend.common.ApiResponse;
import com.tfu.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de usuarios.
 * Expone endpoints para crear, consultar y actualizar información de usuarios.
 */
@Tag(name = "Usuarios", description = "API para gestión de usuarios del sistema")
@RestController
@RequestMapping("/user")
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserRepository userRepository;

  /**
   * Constructor que inyecta el repositorio de usuarios.
   * 
   * @param userRepository Repositorio para acceso a datos de usuario
   */
  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Obtiene todos los usuarios del sistema.
   * 
   * @return Lista de todos los usuarios
   */
  @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista de todos los usuarios registrados en el sistema", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> users = userRepository.findAll();
    logger.debug("Recuperados {} usuarios", users.size());
    return ResponseEntity.ok(ApiResponse.success(users,
        "Se encontraron " + users.size() + " usuarios"));
  }

  /**
   * Obtiene un usuario por su ID.
   * 
   * @param userId ID del usuario a buscar
   * @return Datos del usuario encontrado o error 404 si no existe
   */
  @Operation(summary = "Obtener usuario por ID", description = "Busca y devuelve un usuario específico por su ID", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json"))
  })
  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<User>> getUserById(
      @Parameter(description = "ID del usuario a buscar") @PathVariable("userId") Long userId) {
    logger.debug("Buscando usuario con ID: {}", userId);
    return userRepository.findById(userId)
        .map(user -> ResponseEntity.ok(ApiResponse.success(user, "Usuario encontrado")))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Usuario no encontrado", "No existe un usuario con ID: " + userId)));
  }

  /**
   * Obtiene un usuario por su email.
   * 
   * @param email Email del usuario a buscar
   * @return Datos del usuario encontrado o error 404 si no existe
   */
  @GetMapping("/by-email")
  public ResponseEntity<ApiResponse<User>> getUserByEmail(@RequestParam String email) {
    logger.debug("Buscando usuario con email: {}", email);
    return userRepository.findByEmail(email)
        .map(user -> ResponseEntity.ok(ApiResponse.success(user, "Usuario encontrado")))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Usuario no encontrado", "No existe un usuario con email: " + email)));
  }

  /**
   * Busca usuarios por nombre o apellido.
   * 
   * @param q Texto a buscar en nombre o apellido
   * @return Lista de usuarios que coinciden con la búsqueda
   */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String q) {
    logger.debug("Buscando usuarios que coincidan con: {}", q);
    List<User> users = userRepository.findByNombreContainingOrApellidoContaining(q);
    return ResponseEntity.ok(ApiResponse.success(users,
        "Se encontraron " + users.size() + " usuarios que coinciden con la búsqueda"));
  }

  /**
   * Obtiene todos los usuarios premium o no premium.
   * 
   * @param premium Filtro para usuarios premium (true) o no premium (false)
   * @return Lista de usuarios según el filtro
   */
  @GetMapping("/premium")
  public ResponseEntity<ApiResponse<List<User>>> getUsersByPremium(@RequestParam boolean premium) {
    logger.debug("Buscando usuarios con premium={}", premium);
    List<User> users = premium ? userRepository.findByPremiumTrue() : userRepository.findByPremiumFalse();

    return ResponseEntity.ok(ApiResponse.success(users,
        "Se encontraron " + users.size() + " usuarios " + (premium ? "premium" : "no premium")));
  }

  /**
   * Crea un nuevo usuario.
   * 
   * @param request Datos del usuario a crear
   * @return Usuario creado
   */
  @Operation(summary = "Crear nuevo usuario", description = "Registra un nuevo usuario en el sistema", security = {
      @SecurityRequirement(name = "Bearer Authentication") })
  @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = "application/json"))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserRequest request) {
    try {
      logger.debug("Creando nuevo usuario con email: {}", request.getEmail());

      // Verificamos si ya existe un usuario con ese email
      if (userRepository.existsByEmail(request.getEmail())) {
        logger.warn("Intento de crear usuario con email ya existente: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("Email ya registrado",
                "Ya existe un usuario con el email: " + request.getEmail()));
      }

      User user = new User();
      user.setNombre(request.getNombre());
      user.setApellido(request.getApellido());
      user.setEmail(request.getEmail());
      user.setPremium(request.IsPremium());

      User saved = userRepository.save(user);
      logger.info("Usuario creado correctamente con ID: {}", saved.getId());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success(saved, "Usuario creado correctamente"));
    } catch (Exception e) {
      logger.error("Error al crear usuario: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Error al crear usuario", e.getMessage()));
    }
  }

  /**
   * Actualiza un usuario existente.
   * 
   * @param userId  ID del usuario a actualizar
   * @param request Nuevos datos del usuario
   * @return Usuario actualizado o error si no se encuentra
   */
  @PutMapping("/{userId}")
  public ResponseEntity<ApiResponse<User>> updateUser(
      @PathVariable("userId") Long userId,
      @Valid @RequestBody UserRequest request) {
    try {
      logger.debug("Actualizando usuario con ID: {}", userId);

      // Verificamos si el email ya está en uso por otro usuario
      userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
        if (!existingUser.getId().equals(userId)) {
          logger.warn("Email ya registrado por otro usuario: {}", request.getEmail());
          throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }
      });

      return userRepository.findById(userId)
          .map(user -> {
            user.setNombre(request.getNombre());
            user.setApellido(request.getApellido());
            user.setEmail(request.getEmail());
            user.setPremium(request.IsPremium());

            User updated = userRepository.save(user);
            logger.info("Usuario actualizado correctamente con ID: {}", updated.getId());

            return ResponseEntity.ok(ApiResponse.success(updated, "Usuario actualizado correctamente"));
          })
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Usuario no encontrado", "No existe un usuario con ID: " + userId)));
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validación en actualización: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ApiResponse.error("Error de validación", e.getMessage()));
    } catch (Exception e) {
      logger.error("Error al actualizar usuario: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Error al actualizar usuario", e.getMessage()));
    }
  }

  /**
   * Cambia el estado premium de un usuario.
   * 
   * @param userId  ID del usuario a modificar
   * @param premium Nuevo estado premium
   * @return Usuario actualizado
   */
  @PatchMapping("/{userId}/premium")
  public ResponseEntity<ApiResponse<User>> updatePremiumStatus(
      @PathVariable("userId") Long userId,
      @RequestParam boolean premium) {
    try {
      logger.debug("Actualizando estado premium={} para usuario con ID: {}", premium, userId);

      return userRepository.findById(userId)
          .map(user -> {
            user.setPremium(premium);
            User updated = userRepository.save(user);
            logger.info("Estado premium actualizado correctamente para usuario con ID: {}", userId);

            return ResponseEntity.ok(ApiResponse.success(updated,
                "Estado premium actualizado correctamente a " + (premium ? "activo" : "inactivo")));
          })
          .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Usuario no encontrado", "No existe un usuario con ID: " + userId)));
    } catch (Exception e) {
      logger.error("Error al actualizar estado premium: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al actualizar estado premium", e.getMessage()));
    }
  }

  /**
   * Elimina un usuario existente.
   * 
   * @param userId ID del usuario a eliminar
   * @return Respuesta de éxito o error si no se encuentra
   */
  @DeleteMapping("/{userId}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("userId") Long userId) {
    try {
      logger.debug("Eliminando usuario con ID: {}", userId);

      if (userRepository.existsById(userId)) {
        userRepository.deleteById(userId);
        logger.info("Usuario eliminado correctamente con ID: {}", userId);
        return ResponseEntity.ok(ApiResponse.successMessage("Usuario eliminado correctamente"));
      } else {
        logger.warn("Intento de eliminar usuario inexistente con ID: {}", userId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Usuario no encontrado", "No existe un usuario con ID: " + userId));
      }
    } catch (Exception e) {
      logger.error("Error al eliminar usuario: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Error al eliminar usuario", e.getMessage()));
    }
  }
}
