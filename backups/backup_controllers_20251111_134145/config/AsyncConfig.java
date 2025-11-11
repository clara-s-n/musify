package com.tfu.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para el patrón Asynchronous Request-Reply.
 * Define un pool de hilos personalizado para manejar operaciones asíncronas
 * de manera eficiente.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  /**
   * Configura el executor para operaciones asíncronas.
   * Pool de hilos optimizado para peticiones REST asíncronas.
   * 
   * @return Executor configurado con pool de hilos
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5); // Tamaño mínimo del pool
    executor.setMaxPoolSize(10); // Tamaño máximo del pool
    executor.setQueueCapacity(100); // Capacidad de la cola de espera
    executor.setThreadNamePrefix("async-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }
}
