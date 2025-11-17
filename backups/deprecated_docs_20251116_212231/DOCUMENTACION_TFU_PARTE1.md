# Documentación TFU Unidad 4 - Parte 1

## Patrones de Arquitectura: Modelos UML, Justificaciones y Tácticas

---

## Tabla de Contenidos

1. [Introducción](#introducción)
2. [Patrones de Disponibilidad](#patrones-de-disponibilidad)
   - 2.1 [Retry](#21-retry)
   - 2.2 [Circuit Breaker](#22-circuit-breaker)
   - 2.3 [Rate Limiting](#23-rate-limiting)
   - 2.4 [Health Endpoint Monitoring](#24-health-endpoint-monitoring)
3. [Patrones de Rendimiento](#patrones-de-rendimiento)
   - 3.1 [Cache-Aside](#31-cache-aside)
   - 3.2 [Asynchronous Request-Reply](#32-asynchronous-request-reply)
4. [Patrones de Seguridad](#patrones-de-seguridad)
   - 4.1 [Gatekeeper](#41-gatekeeper)
   - 4.2 [Gateway Offloading](#42-gateway-offloading)
   - 4.3 [Federated Identity](#43-federated-identity)
5. [Patrones de Modificabilidad y Despliegue](#patrones-de-modificabilidad-y-despliegue)
   - 5.1 [External Configuration Store](#51-external-configuration-store)
   - 5.2 [Blue/Green Deployment](#52-bluegreen-deployment)
6. [Resumen de Tácticas](#resumen-de-tácticas)

---

## Introducción

Este documento presenta los **11 patrones de arquitectura** implementados en la aplicación Musify, cumpliendo con los requisitos del Trabajo Final de Unidad 4. Para cada patrón se incluye:

- **Diagrama UML** (secuencia, clases, componentes o despliegue según corresponda)
- **Justificación** de por qué se eligió el patrón
- **Relación con tácticas de arquitectura**

---

## Patrones de Disponibilidad

### 2.1. Retry

#### Diagrama de Secuencia UML

Ver archivo: [`diagramas/retry-sequence.pu`](diagramas/retry-sequence.pu)

![Diagrama Retry](diagramas/retry-sequence.png)

#### Justificación

**¿Por qué se implementó Retry?**

El patrón Retry se implementó para manejar fallos transitorios en servicios externos, específicamente:

1. **Flaky Service**: Simula un servicio de streaming inestable que falla el 60% del tiempo (40% timeout, 20% error 500)
2. **Spotify API**: API externa que puede experimentar fallos temporales de red o rate limiting
3. **Fallos de Red**: Problemas temporales de conectividad que se resuelven automáticamente

**Beneficios en Musify**:

- Mejora la experiencia del usuario al recuperarse automáticamente de fallos temporales
- Reduce errores percibidos por el usuario en un 80-90% en servicios inestables
- Backoff exponencial evita sobrecargar servicios que están experimentando problemas

#### Relación con Tácticas de Arquitectura

**Táctica**: Detección y Recuperación de Fallos → **Recuperación - Reintroducción**

**¿Por qué?**

- El patrón Retry implementa la táctica de **reintroducción** al reintentar automáticamente operaciones fallidas
- Usa **backoff exponencial** para espaciar los reintentos y dar tiempo al servicio a recuperarse
- Se combina con **Circuit Breaker** para evitar reintentos infinitos cuando el servicio está completamente caído

**Configuración**:

```yaml
maxAttempts: 3
waitDuration: 200ms
enableExponentialBackoff: true
```

---

### 2.2. Circuit Breaker

#### Diagrama de Secuencia UML

Ver archivo: [`diagramas/circuit-breaker-sequence.pu`](diagramas/circuit-breaker-sequence.pu)

![Diagrama Circuit Breaker](diagramas/circuit-breaker-sequence.png)

#### Diagrama de Estados del Circuito

Ver archivo: [`diagramas/circuit-breaker-states.pu`](diagramas/circuit-breaker-states.pu)

![Estados Circuit Breaker](diagramas/circuit-breaker-states.png)

#### Justificación

**¿Por qué se implementó Circuit Breaker?**

El Circuit Breaker es crítico para prevenir cascadas de fallos y proteger el sistema:

1. **Prevención de Cascadas**: Si el flaky-service está completamente caído, evita que cada request espere el timeout
2. **Respuesta Rápida**: Cuando el circuito está abierto, responde inmediatamente con fallback sin esperar
3. **Protección del Backend**: Evita que el backend se sature con requests a servicios caídos
4. **Recuperación Automática**: Después de un período, intenta cerrar el circuito automáticamente

**Beneficios en Musify**:

- Reduce el tiempo de respuesta de ~2 segundos (timeout) a ~50ms (fallback inmediato)
- Protege los recursos del sistema (threads, conexiones)
- Proporciona degradación elegante con URLs alternativas

#### Relación con Tácticas de Arquitectura

**Táctica**: Detección y Recuperación de Fallos → **Prevención - Eliminación de Fuente de Fallos**

**¿Por qué?**

- **Previene cascadas** al abrir el circuito cuando detecta alta tasa de fallos (50%)
- **Elimina temporalmente la fuente de fallos** al dejar de llamar al servicio problemático
- Implementa **self-healing** al intentar cerrar automáticamente después de `waitDurationInOpenState`
- Proporciona **graceful degradation** mediante métodos de fallback

**Estados del circuito**:

- **CLOSED**: Funcionamiento normal, monitorea fallos
- **OPEN**: Circuito abierto, usa fallback inmediatamente
- **HALF_OPEN**: Permite requests de prueba para verificar recuperación

---

### 2.3. Rate Limiting

#### Diagrama de Secuencia UML

Ver archivo: [`diagramas/rate-limiting-sequence.pu`](diagramas/rate-limiting-sequence.pu)

![Diagrama Rate Limiting](diagramas/rate-limiting-sequence.png)

#### Justificación

**¿Por qué se implementó Rate Limiting?**

El Rate Limiting en el endpoint de login es esencial para seguridad:

1. **Prevención de Ataques de Fuerza Bruta**: Limita intentos de login a 5 por minuto
2. **Protección contra DoS**: Evita que un atacante sature el sistema con requests
3. **Protección de Recursos**: Reduce carga en la base de datos y sistema de autenticación
4. **Cumplimiento de Buenas Prácticas**: Estándar en APIs de autenticación

**Beneficios en Musify**:

- Detecta y bloquea intentos de fuerza bruta en tiempo real
- No afecta usuarios legítimos (5 intentos son suficientes)
- Implementación declarativa con `@RateLimiter`

#### Relación con Tácticas de Arquitectura

**Táctica**: Seguridad → **Resistir Ataques - Limitar Acceso**

**¿Por qué?**

- **Limita acceso** al endpoint de login a 5 intentos por minuto
- **Detecta ataques** automáticamente cuando se excede el límite
- Implementa **throttling** para proteger recursos críticos
- Se combina con **validación de entrada** para defensa en profundidad

**Configuración**:

```yaml
limitForPeriod: 5 # 5 intentos
limitRefreshPeriod: 1m # por minuto
```

---

### 2.4. Health Endpoint Monitoring

#### Diagrama de Componentes UML

Ver archivo: [`diagramas/health-monitoring-component.pu`](diagramas/health-monitoring-component.pu)

![Diagrama Health Monitoring](diagramas/health-monitoring-component.png)

#### Justificación

**¿Por qué se implementó Health Endpoint Monitoring?**

El monitoreo de salud es fundamental para operaciones y mantenimiento:

1. **Visibilidad del Sistema**: Permite saber el estado del sistema en tiempo real
2. **Detección Temprana**: Identifica problemas antes de que afecten a usuarios
3. **Integración con Load Balancers**: NGINX puede usar health checks para routing
4. **Debugging y Troubleshooting**: Facilita diagnóstico de problemas en producción

**Beneficios en Musify**:

- Endpoint `/actuator/health` accesible públicamente
- Información sobre estado de base de datos, disco, conectividad
- Integrable con herramientas de monitoreo externas

#### Relación con Tácticas de Arquitectura

**Táctica**: Detección y Recuperación de Fallos → **Detección - Monitor**

**¿Por qué?**

- **Monitorea continuamente** el estado de componentes críticos
- **Detecta fallos** en base de datos, servicios externos, recursos del sistema
- Proporciona **observabilidad** para operadores y herramientas de monitoreo
- Facilita **diagnóstico rápido** de problemas en producción

**Componentes monitoreados**:

- Estado de PostgreSQL
- Espacio en disco
- Estado de la aplicación
- Métricas de rendimiento

---

## Patrones de Rendimiento

### 3.1. Cache-Aside

#### Diagrama de Secuencia UML

Ver archivo: [`diagramas/cache-aside-sequence.pu`](diagramas/cache-aside-sequence.pu)

![Diagrama Cache-Aside](diagramas/cache-aside-sequence.png)

#### Diagrama de Clases UML

Ver archivo: [`diagramas/cache-aside-class.pu`](diagramas/cache-aside-class.pu)

![Diagrama Clases Cache](diagramas/cache-aside-class.png)

#### Justificación

**¿Por qué se implementó Cache-Aside?**

El cacheo de resultados de Spotify API es crítico para rendimiento:

1. **Reducción de Latencia**: Spotify API puede tardar 300-500ms, el caché responde en <10ms
2. **Reducción de Costos**: Menos llamadas a API externa (posible rate limiting o costos)
3. **Mejor Experiencia de Usuario**: Búsquedas repetidas son instantáneas
4. **Resiliencia**: Si Spotify API falla, datos en caché siguen disponibles

**Beneficios en Musify**:

- Mejora de ~50x en tiempo de respuesta para búsquedas cacheadas
- Reduce tráfico a Spotify API en ~70-80% (búsquedas repetidas)
- Evicción automática cada 10 minutos para mantener datos frescos

#### Relación con Tácticas de Arquitectura

**Táctica**: Rendimiento → **Gestión de Recursos - Introducir Concurrencia**

**¿Por qué?**

- **Reduce contención** al evitar llamadas repetidas a APIs externas
- **Mejora throughput** permitiendo más requests simultáneas
- Implementa **lazy loading** (cache-aside): solo cachea lo que se usa
- **Gestión de memoria** con evicción programada para evitar crecimiento infinito

**Estrategia**:

- **Cache Key**: Basado en parámetros de búsqueda (`query + limit`, `trackId`)
- **Eviction**: Scheduled cada 10 minutos (balance entre freshness y performance)
- **Miss Strategy**: Load from source (Spotify API)

---

### 3.2. Asynchronous Request-Reply

#### Diagrama de Secuencia UML

Ver archivo: [`diagramas/async-request-reply-sequence.pu`](diagramas/async-request-reply-sequence.pu)

![Diagrama Async Request-Reply](diagramas/async-request-reply-sequence.png)

#### Diagrama de Componentes UML

Ver archivo: [`diagramas/async-components.pu`](diagramas/async-components.pu)

![Diagrama Componentes Async](diagramas/async-components.png)

#### Justificación

**¿Por qué se implementó Asynchronous Request-Reply?**

El procesamiento asíncrono es esencial para escalabilidad:

1. **No Bloquea Threads**: Thread del controller retorna inmediatamente, libera recursos
2. **Mejor Throughput**: Puede manejar más requests concurrentes
3. **Operaciones Largas**: Llamadas a flaky-service pueden tardar (timeouts, retries)
4. **Escalabilidad**: Pool de threads dedicado para operaciones asíncronas

**Beneficios en Musify**:

- Puede manejar 100+ requests concurrentes con pool de 10 threads
- Threads del Tomcat no se bloquean esperando I/O
- Cola de 100 tareas permite burst de tráfico

#### Relación con Tácticas de Arquitectura

**Táctica**: Rendimiento → **Gestión de Recursos - Introducir Concurrencia**

**¿Por qué?**

- **Maximiza utilización de recursos** al no bloquear threads esperando I/O
- **Aumenta throughput** permitiendo procesar múltiples requests simultáneamente
- Implementa **non-blocking I/O** mediante `CompletableFuture`
- **Pool de threads** optimizado para balance entre recursos y capacidad

**Configuración del Thread Pool**:

```java
corePoolSize: 5      // Threads siempre activos
maxPoolSize: 10      // Máximo threads concurrentes
queueCapacity: 100   // Requests en espera
```

---

## Patrones de Seguridad

### 4.1. Gatekeeper

#### Diagrama de Despliegue UML

Ver archivo: [`diagramas/gatekeeper-deployment.pu`](diagramas/gatekeeper-deployment.pu)

![Diagrama Gatekeeper](diagramas/gatekeeper-deployment.png)

#### Justificación

**¿Por qué se implementó Gatekeeper?**

NGINX como gateway es fundamental para seguridad y arquitectura:

1. **Punto Único de Entrada**: Toda comunicación externa pasa por NGINX
2. **Ocultación de Arquitectura**: Clientes no conocen la topología interna
3. **Terminación TLS**: NGINX maneja SSL/TLS, backend puede usar HTTP interno
4. **Control Centralizado**: Políticas de seguridad en un solo lugar

**Beneficios en Musify**:

- Backend no expuesto directamente a Internet
- Load balancing transparente para clientes
- Simplifica gestión de certificados SSL

#### Relación con Tácticas de Arquitectura

**Táctica**: Seguridad → **Resistir Ataques - Limitar Acceso**

**¿Por qué?**

- **Controla acceso** siendo el único punto de entrada público
- **Oculta detalles internos** de implementación y topología
- **Centraliza seguridad**: firewall, rate limiting, autenticación pueden agregarse aquí
- Implementa **defense in depth**: primera capa de seguridad

**Configuración NGINX**:

```nginx
upstream app_backend {
  server backend-app-1:8443;
  server backend-app-2:8443;
}
server {
  listen 80;  # Solo puerto expuesto públicamente
}
```

---

### 4.2. Gateway Offloading

#### Diagrama de Componentes UML

Ver archivo: [`diagramas/gateway-offloading-component.pu`](diagramas/gateway-offloading-component.pu)

![Diagrama Gateway Offloading](diagramas/gateway-offloading-component.png)

#### Justificación

**¿Por qué se implementó Gateway Offloading?**

Offloading funcionalidades a NGINX mejora eficiencia y separación de responsabilidades:

1. **Reintentos en Gateway**: NGINX reintenta automáticamente sin involucrar lógica de negocio
2. **Health Checks**: NGINX detecta backends caídos sin polling desde aplicación
3. **Compresión**: NGINX comprime respuestas, liberando CPU del backend
4. **Connection Pooling**: NGINX reutiliza conexiones HTTP, reduce overhead

**Beneficios en Musify**:

- Backend se enfoca en lógica de negocio, no en infraestructura
- Reintentos automáticos sin código en aplicación
- Mejor performance por compresión y connection pooling

#### Relación con Tácticas de Arquitectura

**Táctica**: Modificabilidad → **Localizar Modificaciones - Separación de Responsabilidades**

**¿Por qué?**

- **Separa responsabilidades**: infraestructura (NGINX) vs lógica de negocio (backend)
- **Reduce acoplamiento**: cambios en políticas de retry no requieren cambios en código
- Implementa **cross-cutting concerns** (logging, compresión) en un solo lugar
- Facilita **scaling independiente**: NGINX puede escalar separado del backend

**Funcionalidades offloaded**:

- Reintentos automáticos
- Health checks pasivos
- Compresión gzip
- Connection pooling
- TLS termination

---

### 4.3. Federated Identity

#### Diagrama de Secuencia UML - JWT

Ver archivo: [`diagramas/federated-identity-jwt-sequence.pu`](diagramas/federated-identity-jwt-sequence.pu)

![Diagrama JWT](diagramas/federated-identity-jwt-sequence.png)

#### Diagrama de Secuencia UML - OAuth2

Ver archivo: [`diagramas/federated-identity-oauth2-sequence.pu`](diagramas/federated-identity-oauth2-sequence.pu)

![Diagrama OAuth2](diagramas/federated-identity-oauth2-sequence.png)

#### Justificación

**¿Por qué se implementó Federated Identity?**

La identidad federada permite autenticación distribuida y flexible:

1. **JWT para Usuarios**: Autenticación stateless, no requiere sesiones en servidor
2. **OAuth2 para Spotify**: Integración segura con API externa sin exponer credenciales
3. **Escalabilidad**: JWT permite horizontal scaling sin shared session store
4. **Integración Futura**: Facilita agregar otros proveedores (Google, Facebook, etc.)

**Beneficios en Musify**:

- No requiere almacenamiento de sesiones (stateless)
- Token refresh automático para Spotify API
- Fácil integración con otros servicios externos

#### Relación con Tácticas de Arquitectura

**Táctica**: Seguridad → **Resistir Ataques - Autenticar Actores**

**¿Por qué?**

- **Autentica usuarios** mediante JWT firmado con secret key
- **Valida identidad** en cada request sin consultar base de datos
- Implementa **OAuth2 client credentials** para autenticación servicio-a-servicio
- **Tokens con expiración** limitan ventana de compromiso

**Componentes**:

- `JwtTokenProvider`: Genera y valida tokens
- `JwtAuthFilter`: Intercepta requests y valida JWT
- `SpotifyService`: Maneja OAuth2 con Spotify

---

## Patrones de Modificabilidad y Despliegue

### 5.1. External Configuration Store

#### Diagrama de Componentes UML

Ver archivo: [`diagramas/external-config-component.pu`](diagramas/external-config-component.pu)

![Diagrama External Config](diagramas/external-config-component.png)

#### Justificación

**¿Por qué se implementó External Configuration Store?**

La externalización de configuración es crítica para mantenibilidad:

1. **Separación Código-Config**: Cambios de configuración sin recompilar
2. **Múltiples Ambientes**: Misma imagen Docker con diferente configuración (dev, staging, prod)
3. **Secrets Management**: Credenciales no versionadas en Git
4. **Despliegue Simplificado**: Cambiar configuración sin rebuild de imagen

**Beneficios en Musify**:

- Misma imagen Docker funciona en cualquier ambiente
- Secrets seguros mediante variables de entorno
- Fácil cambio de configuración en producción

#### Relación con Tácticas de Arquitectura

**Táctica**: Modificabilidad → **Localizar Modificaciones - Configuración Externa**

**¿Por qué?**

- **Localiza cambios**: configuración en un solo lugar (variables de entorno)
- **Reduce impacto**: cambios de config no requieren rebuild ni redeploy de código
- Implementa **12-factor app** principio de externalización
- Facilita **configuración por ambiente** sin duplicar código

**Flujo**:

1. Variables definidas en `docker-compose.yaml` o `.env`
2. Spring Boot lee mediante `${VAR:default}` en `application.yaml`
3. Cambios en config no requieren recompilación

---

### 5.2. Blue/Green Deployment

#### Diagrama de Despliegue UML

Ver archivo: [`diagramas/blue-green-deployment.pu`](diagramas/blue-green-deployment.pu)

![Diagrama Blue-Green](diagramas/blue-green-deployment.png)

#### Diagrama de Secuencia UML - Actualización

Ver archivo: [`diagramas/blue-green-update-sequence.pu`](diagramas/blue-green-update-sequence.pu)

![Secuencia Blue-Green](diagramas/blue-green-update-sequence.png)

#### Justificación

**¿Por qué se implementó Blue/Green Deployment?**

El despliegue sin downtime es esencial para aplicaciones en producción:

1. **Zero Downtime**: Sistema siempre disponible durante actualizaciones
2. **Rollback Rápido**: Si algo falla, simplemente se redirige tráfico a versión anterior
3. **Testing en Producción**: Nueva versión puede probarse con tráfico real gradualmente
4. **Reducción de Riesgo**: Actualizaciones incrementales por réplica

**Beneficios en Musify**:

- Actualizaciones sin interrumpir servicio
- NGINX redirige automáticamente a instancias saludables
- Proceso de actualización simple y predecible

#### Relación con Tácticas de Arquitectura

**Táctica**: Modificabilidad → **Defer Binding - Deployment Time**

**¿Por qué?**

- **Difiere binding** de qué versión servir hasta deployment time
- Implementa **canary deployment** actualizando una réplica primero
- **Facilita rollback**: simplemente redirigir tráfico a versión anterior
- **Reduce riesgo** de deployments mediante actualización gradual

**Proceso de actualización**:

1. Detener `backend-app-1`
2. NGINX redirige todo a `backend-app-2`
3. Actualizar y reiniciar `app-1`
4. Verificar funcionamiento
5. Repetir con `app-2`

---

## Resumen de Tácticas

### Tabla de Relación: Patrones → Tácticas

| Patrón                  | Táctica de Arquitectura                       | Categoría       |
| ----------------------- | --------------------------------------------- | --------------- |
| **Retry**               | Recuperación - Reintroducción                 | Disponibilidad  |
| **Circuit Breaker**     | Prevención - Eliminación de Fuente            | Disponibilidad  |
| **Rate Limiting**       | Limitar Acceso                                | Seguridad       |
| **Health Monitoring**   | Detección - Monitor                           | Disponibilidad  |
| **Cache-Aside**         | Gestión de Recursos - Introducir Concurrencia | Rendimiento     |
| **Async Request-Reply** | Gestión de Recursos - Introducir Concurrencia | Rendimiento     |
| **Gatekeeper**          | Limitar Acceso                                | Seguridad       |
| **Gateway Offloading**  | Separación de Responsabilidades               | Modificabilidad |
| **Federated Identity**  | Autenticar Actores                            | Seguridad       |
| **External Config**     | Configuración Externa                         | Modificabilidad |
| **Blue/Green**          | Defer Binding - Deployment Time               | Modificabilidad |

### Mapa de Tácticas por Categoría

#### Disponibilidad: Detección y Recuperación de Fallos

- **Detección**: Health Endpoint Monitoring
- **Prevención**: Circuit Breaker
- **Recuperación**: Retry

#### Rendimiento: Gestión de Recursos

- **Reducir Demanda**: Cache-Aside (evita llamadas repetidas)
- **Introducir Concurrencia**: Async Request-Reply (threads no bloqueantes)

#### Seguridad: Resistir Ataques

- **Limitar Acceso**: Gatekeeper, Rate Limiting
- **Autenticar Actores**: Federated Identity (JWT + OAuth2)

#### Modificabilidad: Localizar Modificaciones

- **Separación de Responsabilidades**: Gateway Offloading
- **Configuración Externa**: External Configuration Store
- **Defer Binding**: Blue/Green Deployment

---

## Conclusión

Los 11 patrones implementados forman un sistema cohesivo donde:

✅ **Múltiples patrones trabajan juntos**: Retry + Circuit Breaker + Async para disponibilidad robusta  
✅ **Cada patrón mapea a tácticas específicas**: Relación clara entre implementación y teoría  
✅ **Cobertura completa de atributos de calidad**: Disponibilidad, Rendimiento, Seguridad, Modificabilidad  
✅ **Implementación verificable**: Diagramas UML muestran estructura y comportamiento real

Los diagramas PlantUML proporcionan visualizaciones detalladas de cómo cada patrón opera dentro del sistema Musify.
