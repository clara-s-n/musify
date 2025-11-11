package com.tfu.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot para Musify.
 * Inicia el backend con soporte para:
 * - Cache-Aside pattern (@EnableCaching)
 * - Async Request-Reply pattern (@EnableAsync)
 * - Scheduled tasks (@EnableScheduling)
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class BackendApplication {
	/**
	 * Método main que arranca la aplicación Spring Boot.
	 * 
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
