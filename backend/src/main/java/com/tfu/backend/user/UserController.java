package com.tfu.backend.user;
import com.tfu.backend.repositories.UserRepository;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

  private UserRepository userRepository;
  
  @GetMapping("/")
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userRepository.findAll());
  }
   @GetMapping("/{id_user}")
  public ResponseEntity<User> getUserByID(@PathVariable Long id_user) {
    return userRepository.findById(id_user)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }
  @PostMapping
  // Hacer un wrapper desp esto es para flexibilidad para la demo
  public ResponseEntity<?> postUser(@Valid @RequestBody UserRequest request) {
    User user = new User();
    user.setNombre(request.getNombre());
    user.setApellido(request.getApellido());
    user.setEmail(request.getEmail());
    user.setPremium(request.IsPremium());
    try {
      User saved = userRepository.save(user);
      return ResponseEntity.ok(saved);
  } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error al guardar usuario");
  }
    
  }
  @PutMapping("/{id_user}")
  public ResponseEntity<?> putUser(
    @PathVariable Long id_user,
    @Valid @RequestBody UserRequest request) {
      try {
        return userRepository.findById(id_user).map(user -> {
          user.setEmail(request.getEmail());
          user.setApellido(request.getApellido());
          user.setNombre(request.getNombre());
          user.setPremium(request.IsPremium());
          User updated = userRepository.save(user);
          return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
      } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error al editar al usuario");
      }
   
  }
}



