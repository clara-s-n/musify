# Musify

## Descripción del Proyecto

Musify es una aplicación de demostración que implementa diversas tácticas de arquitectura para satisfacer requerimientos no funcionales relacionados con disponibilidad, rendimiento y seguridad. La aplicación simula un servicio de streaming de música, permitiendo buscar canciones, autenticarse y reproducir pistas.

## Tácticas de Arquitectura Implementadas

Este proyecto implementa la combinación: **"Replicación y re-intentos para disponibilidad y dos tácticas de la categoría resistir a ataques para seguridad"**

### 1. Tácticas para Disponibilidad

#### Replicación

- **Implementación**: Dos instancias replicadas del backend (`backend-app-1` y `backend-app-2`).
- **Componentes clave**:
  - Docker Compose para gestionar múltiples instancias
  - NGINX como balanceador de carga
- **Beneficio**: Alta disponibilidad incluso si una instancia falla.

#### Re-intentos y Circuit Breaker

- **Implementación**: Resilience4j para gestionar fallos del servicio de streaming.
- **Componentes clave**:
  - `@Retry`: Reintenta operaciones fallidas automáticamente
  - `@CircuitBreaker`: Previene cascada de fallos
  - `@TimeLimiter`: Establece tiempos de espera máximos
  - Método de fallback para degradación elegante
- **Beneficio**: Tolerancia a fallos transitorios y protección contra cascadas de fallos.

### 2. Tácticas para Seguridad (Resistir Ataques)

#### Validación de Entrada

- **Implementación**: Validación de datos de usuario.
- **Componentes clave**:
  - Anotaciones `@Email` y `@NotBlank`
  - Validaciones de formato
- **Beneficio**: Prevención de inyecciones y ataques de entrada maliciosa.

#### Rate Limiting

- **Implementación**: Limitación de tasa en endpoint de login.
- **Componentes clave**:
  - `@RateLimiter` de Resilience4j
  - Configuración de límites por periodo de tiempo
- **Beneficio**: Protección contra ataques de fuerza bruta y denegación de servicio.

## Estructura del Proyecto

```
musify/
├── backend/                     # Aplicación Spring Boot
│   ├── src/                     # Código fuente
│   │   └── main/
│   │       ├── java/com/tfu/backend/
│   │       │   ├── auth/        # Autenticación y seguridad
│   │       │   ├── catalog/     # Catálogo de canciones
│   │       │   ├── config/      # Configuración de seguridad
│   │       │   └── playback/    # Reproducción de pistas
│   │       └── resources/       # Configuraciones
│   ├── Dockerfile               # Construcción de imagen backend
│   └── pom.xml                  # Dependencias Maven
├── flaky-service/               # Servicio inestable (simulación)
│   ├── Dockerfile
│   ├── package.json
│   └── server.js                # Simulación de fallos aleatorios
├── ops/
│   └── nginx.conf               # Configuración de balanceo y reintentos
├── scripts/                     # Scripts de demostración
│   ├── demo_health.sh
│   ├── demo_replication.sh
│   ├── demo_retries.sh
│   └── demo_security.sh
└── docker-compose.yaml          # Orquestación de servicios
```

## Requisitos

- Docker y Docker Compose
- Bash

## Cómo Ejecutar

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/clara-s-n/musify.git
   cd musify
   ```

2. Iniciar los servicios:

   ```bash
   docker compose up --build
   ```

3. Esperar a que todos los servicios estén ejecutándose. La aplicación estará disponible en http://localhost:8080

## Demostraciones

### 1. Replicación y Alta Disponibilidad

```bash
./scripts/demo_replication.sh
```

Este script demuestra cómo el sistema sigue funcionando incluso cuando una instancia del backend se cae. NGINX redirecciona automáticamente el tráfico a la instancia disponible.

### 2. Reintentos y Circuit Breaker

```bash
./scripts/demo_retries.sh
```

Este script muestra cómo el sistema maneja los fallos del servicio de streaming inestable, reintentando automáticamente y proporcionando URLs alternativas cuando es necesario.

### 3. Seguridad: Validación y Rate Limiting

```bash
./scripts/demo_security.sh
```

Este script demuestra la validación de entrada (rechazando emails inválidos) y el rate limiting (permitiendo solo 5 intentos de login por minuto).

### 4. Estado de Salud

```bash
./scripts/demo_health.sh
```

Muestra información sobre el estado de salud del sistema y documentación de la API.

## Contribución

Este proyecto fue creado como demostración para la materia "Análisis y Diseño de Aplicaciones II" y no está destinado para uso en producción.
