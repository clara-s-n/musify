package com.tfu.backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para manejar la funcionalidad de logout directamente
 * sin pasar por el proxy de NGINX.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Endpoint simple para probar la API.
     * 
     * @return Mensaje de prueba
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("¡API funcionando correctamente!");
    }
}