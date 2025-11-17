# 🎵 Musify - Aplicación Educacional de Streaming Musical

> **Proyecto Educacional** - Implementación de patrones arquitectónicos para disponibilidad, rendimiento y seguridad usando Spring Boot + Angular + Spotify API.

[![Arquitectura](https://img.shields.io/badge/Arquitectura-Por%20Capas-blue)](docs/arquitectura/)
[![API](https://img.shields.io/badge/API-REST%20+%20JWT-green)](docs/api/)
[![Base de Datos](https://img.shields.io/badge/DB-PostgreSQL-orange)](docs/database/)
[![Frontend](https://img.shields.io/badge/Frontend-Angular-red)](frontend/)
[![Demo](https://img.shields.io/badge/Demo-Scripts-purple)](docs/demos/)

## 🎯 Descripción del Proyecto

**Musify** es una aplicación de demostración que implementa **patrones arquitectónicos clave** para satisfacer requerimientos no funcionales. La aplicación simula un servicio de streaming musical educacional, enfocado en demostrar:

- ✅ **Autenticación JWT segura**
- ✅ **Integración con Spotify API**
- ✅ **Patrones de disponibilidad** (Circuit Breaker, Retry, Replicación)
- ✅ **Patrones de rendimiento** (Cache-Aside, Async Processing)
- ✅ **Patrones de seguridad** (Rate Limiting, Gatekeeper)
- ✅ **Monitoreo y health checks**

## 🏗️ Arquitectura Optimizada

### **Stack Tecnológico**
```
📱 Frontend:     Angular 20.3.0 + Material Design
🔧 Backend:      Spring Boot 3.5.5 + Java 17
🔐 Autenticación: JWT (jjwt 0.11.5) + Spring Security
🛡️ Resiliencia:  Resilience4j 2.3.0 (Circuit Breaker, Retry, Rate Limiting)
🌐 APIs:         REST + SOAP (Spring WS) + OpenAPI 3 (Swagger)
🗄️ Database:     PostgreSQL 16 (2 tablas optimizadas)
🎵 Music API:    Spotify Web API (oficial) + YouTube API
🐳 Deploy:       Docker Compose + NGINX (Load Balancer)
📊 Monitor:      Spring Actuator + Health Checks
💾 Cache:        Spring Cache (Cache-Aside Pattern)
```

### **Arquitectura por Capas**
```
musify/
├── 📄 README.md                 # Este archivo
├── 🐳 docker-compose.yaml       # Orquestación de servicios
├── 📁 backend/                  # 🏗️ API Spring Boot (Arquitectura por Capas)
│   └── src/main/java/com/tfu/backend/
│       ├── 🎮 auth/            # Capa de Controladores (Autenticación)
│       ├── 🎵 spotify/         # Capa de Controladores (Música)
│       ├── 👤 artist/          # Capa de Controladores (Artistas)
│       ├── 🔍 search/          # Capa de Controladores (Búsqueda)
│       ├── ▶️ player/          # Capa de Controladores (Reproductor)
│       ├── 📡 soap/            # Capa de Web Services (SOAP)
│       ├── 🎬 youtube/         # Capa de Servicios (YouTube)
│       ├── ⚙️ config/          # Capa de Configuración
│       └── 🔧 common/          # Utilidades Compartidas
├── 📁 frontend/MusifyFront/     # 🖥️ Aplicación Angular 20.3.0
├── 📁 database/                 # 🗄️ Scripts SQL optimizados
├── 📁 scripts/                  # 🧪 Scripts de demostración
├── 📁 flaky-service/           # 🔄 Servicio simulado (tolerancia a fallos)
└── 📁 docs/                    # 📚 Documentación consolidada
    ├── 📁 api/                 # 📖 API_GUIDE.md + Postman Collections
    ├── 📁 diagramas/           # 📊 Diagramas PlantUML de patrones
    ├── 📄 ARCHITECTURE_PATTERNS.md  # 🏗️ Patrones implementados
    ├── 📄 DEMO_GUIDE.md        # 🎯 Guía completa de demos
    ├── 📄 DEPLOYMENT_GUIDE.md  # 🚀 Despliegue y acceso externo
    └── 📄 SOAP_GUIDE.md        # 📡 API SOAP/XML completa
```

## 🚀 Inicio Rápido (3 pasos)

### 1️⃣ **Clonar y Configurar**
```bash
git clone https://github.com/clara-s-n/musify.git
cd musify
cp .env.example .env  # Configurar variables de entorno
```

### 2️⃣ **Iniciar Servicios**
```bash
docker compose up --build
```

### 3️⃣ **Acceder a la Aplicación**
- 🌐 **Frontend**: http://localhost:4200
- 🔧 **API Backend**: http://localhost:8080  
- 📊 **Swagger UI**: http://localhost:8080/swagger-ui.html
- 🧼 **SOAP WSDL (Auth)**: http://localhost:8080/ws/auth.wsdl
- 🧼 **SOAP WSDL (Music)**: http://localhost:8080/ws/music.wsdl
- ❤️ **Health Check**: http://localhost:8080/actuator/health

## 🔐 Credenciales de Prueba

| Email | Password | Roles | Propósito |
|-------|----------|-------|-----------|
| `user@demo.com` | `password` | USER | Demo básico |
| `admin@demo.com` | `admin` | USER, ADMIN | Administración |
| `estudiante@musify.com` | `estudiante123` | USER | Contexto educacional |
| `profesor@musify.com` | `profesor456` | USER, EDUCATOR | Contexto educacional |
| `premium@musify.com` | `premium789` | USER, PREMIUM | Testing premium |

> 📖 **Más usuarios disponibles en**: [`docs/database/README_DATABASE_OPTIMIZED.md`](docs/database/README_DATABASE_OPTIMIZED.md)

## 🧼 API SOAP (Web Services)

La aplicación incluye **endpoints SOAP** además de REST para demostrar integración con servicios web tradicionales:

### **Endpoints Disponibles**

#### 🔐 **Auth Service** (`/ws/auth.wsdl`)
```xml
<!-- Login Request -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:web="http://tfu.com/backend/webservice">
   <soapenv:Header/>
   <soapenv:Body>
      <web:loginRequest>
         <web:email>user@demo.com</web:email>
         <web:password>password</web:password>
      </web:loginRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

#### 🎵 **Music Service** (`/ws/music.wsdl`)
```xml
<!-- Search Tracks Request -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:web="http://tfu.com/backend/webservice">
   <soapenv:Header/>
   <soapenv:Body>
      <web:searchTracksRequest>
         <web:query>Lana Del Rey</web:query>
         <web:limit>5</web:limit>
      </web:searchTracksRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### **Testing SOAP**

```bash
# Ejecutar script de demo completo
./scripts/demo_soap_complete.sh

# Probar con curl
curl -X POST http://localhost:8080/ws \
  -H "Content-Type: text/xml" \
  -d @request.xml
```

> 📖 **Guías detalladas**:
> - [`docs/api/SOAP_Usage_Guide.md`](docs/api/SOAP_Usage_Guide.md) - Uso completo de SOAP
> - [`docs/api/SOAP_XML_API_Guide.md`](docs/api/SOAP_XML_API_Guide.md) - Ejemplos XML
> - [`SOAP_QUICK_START.md`](SOAP_QUICK_START.md) - Inicio rápido
> - Colección Postman: [`docs/api/Musify_Complete_API_Collection.postman_collection.json`](docs/api/Musify_Complete_API_Collection.postman_collection.json)

## 🛠️ Patrones Arquitectónicos Implementados

### 🔄 **Disponibilidad**
- **Replicación**: 2 instancias backend + NGINX load balancer
- **Circuit Breaker**: Resilience4j para tolerancia a fallos
- **Retry Pattern**: Reintentos automáticos con backoff exponencial  
- **Health Monitoring**: Endpoints de salud con Spring Actuator

### ⚡ **Rendimiento**
- **Cache-Aside**: Spring Cache para resultados de Spotify API
- **Async Processing**: CompletableFuture para operaciones no bloqueantes

### 🔒 **Seguridad**
- **Rate Limiting**: 5 intentos de login por minuto (Resilience4j)
- **JWT Authentication**: Tokens seguros con expiración configurable
- **Gatekeeper**: NGINX como proxy reverso con TLS

### 📊 **Monitoreo**
- **Health Checks**: `/actuator/health`, `/actuator/metrics`
- **API Documentation**: OpenAPI 3 + Swagger UI
- **Logging**: Structured logging with SLF4J

## 📖 Manual de Usuario

### 🎯 **Primeros Pasos**

#### 1️⃣ **Iniciar la Aplicación**
```bash
# Clonar repositorio
git clone https://github.com/clara-s-n/musify.git
cd musify

# Configurar variables de entorno
cp .env.example .env

# Iniciar todos los servicios
docker compose up --build
```

#### 2️⃣ **Acceder al Frontend**
1. Abrir navegador en: **http://localhost:4200**
2. Usar credenciales de prueba:
   - **Email**: `user@demo.com`
   - **Password**: `password`
3. ¡Listo! Ya puedes explorar la aplicación

### 🎵 **Funcionalidades Principales**

#### **🔐 Autenticación y Registro**
- **Login**: Iniciar sesión con JWT
- **Registro**: Crear nueva cuenta
- **Roles**: USER, ADMIN, PREMIUM, EDUCATOR
- **Seguridad**: Rate limiting (5 intentos/minuto)

#### **🎶 Exploración Musical**
- **Búsqueda**: Buscar canciones, artistas y álbumes
- **Spotify Integration**: Resultados reales de Spotify API
- **Categorías**: Pop, Rock, Hip-Hop, Electronic, Jazz, Classical
- **Pistas Aleatorias**: Descubrir nueva música

#### **🎧 Reproductor (Simulado)**
- **Play/Pause**: Control básico de reproducción
- **Información**: Mostrar datos de la canción actual
- **Estado**: Simulación de streaming (URLs de muestra)

> ⚠️ **Nota**: La reproducción actual es simulada. Para audio real, se requiere implementar reproductor HTML5 en el frontend.

### 🛠️ **Para Desarrolladores**

#### **🔧 APIs Disponibles**

**REST API** (http://localhost:8080):
- **Swagger UI**: `/swagger-ui.html`
- **Endpoints**: Auth, Spotify, Artists, Player, Search
- **Formato**: JSON con JWT Authentication

**SOAP API** (http://localhost:8080/ws):
- **Auth WSDL**: `/ws/auth.wsdl`
- **Music WSDL**: `/ws/music.wsdl`
- **Formato**: XML tradicional

#### **🧪 Testing y Demostraciones**
```bash
# Probar todos los patrones arquitectónicos
./scripts/run_all_demos.sh

# Demos específicos
./scripts/demo_retries.sh        # Tolerancia a fallos
./scripts/demo_security.sh       # JWT + Rate limiting
./scripts/demo_performance.sh    # Cache + Async
./scripts/demo_soap_complete.sh  # Web Services SOAP
```

#### **📊 Monitoreo**
- **Health**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/metrics
- **Info**: http://localhost:8080/actuator/info

### 🎯 **Casos de Uso Educacionales**

#### **Para Estudiantes**
1. **Explorar patrones**: Revisar implementación de Circuit Breaker, Retry, Cache
2. **Probar APIs**: Usar Postman con colección incluida
3. **Analizar código**: Estructura por capas en `/backend/src/main/java/`
4. **Ejecutar demos**: Scripts automatizados en `/scripts/`

#### **Para Profesores**
1. **Demostrar resilencia**: Usar `./scripts/demo_retries.sh`
2. **Mostrar escalabilidad**: `./scripts/demo_replication.sh` (2 replicas + NGINX)
3. **Explicar seguridad**: `./scripts/demo_security.sh` (JWT + Rate limiting)
4. **Analizar rendimiento**: `./scripts/demo_performance.sh` (Cache + Async)

### 🚨 **Solución de Problemas**

#### **La aplicación no inicia**
```bash
# Verificar Docker
docker --version
docker compose --version

# Limpiar contenedores
docker compose down -v
docker system prune -f

# Reiniciar
docker compose up --build
```

#### **Error de autenticación**
- Verificar credenciales en sección "🔐 Credenciales de Prueba"
- Revisar que el token JWT no haya expirado
- Probar con `user@demo.com` / `password`

#### **No aparecen resultados de música**
- Verificar variables de entorno Spotify en `.env`
- Comprobar conectividad a internet
- Revisar logs: `docker compose logs backend-app-1`

#### **Puertos ocupados**
```bash
# Verificar puertos en uso
sudo netstat -tlnp | grep :4200
sudo netstat -tlnp | grep :8080

# Cambiar puertos en docker-compose.yaml si es necesario
```

### 📚 **Recursos Adicionales**
- **📖 Documentación API**: [`docs/api/API_GUIDE.md`](docs/api/API_GUIDE.md)
- **🏗️ Patrones Implementados**: [`docs/ARCHITECTURE_PATTERNS.md`](docs/ARCHITECTURE_PATTERNS.md)
- **🎯 Guía de Demos**: [`docs/DEMO_GUIDE.md`](docs/DEMO_GUIDE.md)
- **🚀 Despliegue**: [`docs/DEPLOYMENT_GUIDE.md`](docs/DEPLOYMENT_GUIDE.md)
- **📡 SOAP**: [`docs/SOAP_GUIDE.md`](docs/SOAP_GUIDE.md)

## 🎮 Demostraciones Disponibles

```bash
# Ejecutar todas las demos de patrones
./scripts/run_all_demos.sh

# Demos individuales
./scripts/demo_retries.sh        # Circuit Breaker + Retry
./scripts/demo_replication.sh    # Load Balancing + Replicación  
./scripts/demo_security.sh       # Rate Limiting + JWT
./scripts/demo_performance.sh    # Cache + Async Processing
./scripts/demo_health.sh         # Health Monitoring
./scripts/demo_soap_complete.sh  # Endpoints SOAP (Auth + Music)
```

> 📖 **Guía completa**: [`docs/DEMO_GUIDE.md`](docs/DEMO_GUIDE.md)

## 🏫 Propósito Educacional

Este proyecto es ideal para aprender:

- ✅ **Arquitectura por Capas** con Spring Boot (Controller → Service → Repository)
- ✅ **APIs REST y SOAP** (integración de múltiples protocolos)
- ✅ **Patrones de Resiliencia** (Circuit Breaker, Retry, Bulkhead)
- ✅ **Seguridad en APIs** (JWT, Rate Limiting, CORS)
- ✅ **Integración con APIs externas** (Spotify Web API)
- ✅ **Containerización** con Docker y Docker Compose
- ✅ **Frontend-Backend separation** con Angular + REST API
- ✅ **Web Services SOAP** con Spring WS y WSDL
- ✅ **Base de datos optimizada** (PostgreSQL con solo lo esencial)
- ✅ **Documentación técnica** estructurada

## 📚 Documentación

### **🔍 Para Desarrolladores**
- [`docs/api/Musify_API_Testing_Guide.md`](docs/api/Musify_API_Testing_Guide.md) - Testing con Postman/curl
- [`docs/api/SOAP_Usage_Guide.md`](docs/api/SOAP_Usage_Guide.md) - Guía de uso SOAP
- [`docs/api/SOAP_XML_API_Guide.md`](docs/api/SOAP_XML_API_Guide.md) - Ejemplos XML SOAP
- [`docs/database/README_DATABASE_OPTIMIZED.md`](docs/database/README_DATABASE_OPTIMIZED.md) - Estructura de BD
- [`docs/spotify/Spotify_API_Integration_Guide.md`](docs/spotify/Spotify_API_Integration_Guide.md) - Integración Spotify

### **🏗️ Para Arquitectos**
- [`docs/patrones/PATRONES_IMPLEMENTADOS.md`](docs/patrones/PATRONES_IMPLEMENTADOS.md) - Patrones detallados
- [`docs/arquitectura/CAMBIOS_DIAGRAMAS.md`](docs/arquitectura/CAMBIOS_DIAGRAMAS.md) - Diagramas UML

### **🚀 Para DevOps**
- [`docs/deployment/EXTERNAL_ACCESS_SUMMARY.md`](docs/deployment/EXTERNAL_ACCESS_SUMMARY.md) - Configuración de red
- [`docs/scripts/SCRIPTS_IMPLEMENTACION.md`](docs/scripts/SCRIPTS_IMPLEMENTACION.md) - Scripts de demo

## 📊 Métricas del Proyecto

### **Backend por Capas**
- **Arquitectura**: Por Capas (Controller → Service → Repository)
- **Endpoints REST**: 15+ distribuidos en 7 controladores
- **Endpoints SOAP**: 2 Web Services (Auth + Music Search)
- **Capas**: Controladores (8), Servicios (5), Configuración (3)
- **Controladores**: AuthController, SpotifyTrackController, ArtistController, SearchController, PlayerController, YoutubeService, SoapMusicController
- **Patrones**: 11 patrones arquitectónicos implementados
- **Tecnologías**: Spring Boot 3.5.5 + Java 17 + Resilience4j 2.3.0

### **Base de Datos Simplificada**
- **Tablas**: 2 (eliminadas 11 innecesarias)
- **Registros**: ~12 usuarios de prueba
- **Reducción**: 94% menos datos, 90% menos tablas

### **Frontend Angular 20.3.0**
- **Arquitectura**: Componentes standalone + Signals
- **UI**: Angular Material Design
- **Servicios**: AuthService + SpotifyService (integración optimizada)
- **Características**: Responsive, PWA-ready, TypeScript

## 🤝 Contribución

Este es un proyecto educacional. Para contribuir:

1. Fork del repositorio
2. Crear branch para feature (`git checkout -b feature/mejora`)
3. Commit cambios (`git commit -am 'Agregar mejora'`)
4. Push al branch (`git push origin feature/mejora`)
5. Crear Pull Request

---

**🎓 Desarrollado como parte del programa académico de Análisis y Diseño de Aplicaciones II**

> Para más información sobre patrones específicos, consulta la documentación en [`docs/`](docs/)