# 🎵 Musify - Aplicación Educacional de Streaming Musical

> **Proyecto Educacional** - Implementación de patrones arquitectónicos para disponibilidad, rendimiento y seguridad usando Spring Boot + Angular + Spotify API.

[![Arquitectura](https://img.shields.io/badge/Arquitectura-Microservicios-blue)](docs/arquitectura/)
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
📱 Frontend: Angular 17 + Material Design
🔧 Backend:  Spring Boot 3 + JWT + Resilience4j
🌐 APIs:     REST + SOAP (Spring Boot Starter Web Services)
🗄️ Database: PostgreSQL (2 tablas optimizadas)
🎵 Music API: Spotify Web API (oficial)
🐳 Deploy:   Docker Compose + NGINX
📊 Monitor:  Spring Actuator + Health Checks
```

### **Estructura del Proyecto**
```
musify/
├── 📄 README.md                 # Este archivo
├── 🐳 docker-compose.yaml       # Orquestación de servicios
├── 📁 backend/                  # API Spring Boot
├── 📁 frontend/MusifyFront/     # Aplicación Angular
├── 📁 database/                 # Scripts SQL optimizados
├── 📁 scripts/                  # Scripts de demostración
├── 📁 flaky-service/           # Servicio simulado (tolerancia a fallos)
├── 📁 diagramas/               # Diagramas PlantUML
└── 📁 docs/                    # 📚 Documentación organizada
    ├── 📁 api/                 # Guías de API y testing
    ├── 📁 arquitectura/        # Patrones y diagramas
    ├── 📁 database/            # Documentación de BD
    ├── 📁 demos/               # Guías de demostración
    ├── 📁 deployment/          # Configuración y despliegue
    ├── 📁 patrones/            # Documentación de patrones
    ├── 📁 scripts/             # Documentación de scripts
    └── 📁 spotify/             # Integración Spotify API
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

> 📖 **Guía completa**: [`docs/demos/GUIA_RAPIDA_DEMOS.md`](docs/demos/GUIA_RAPIDA_DEMOS.md)

## 🏫 Propósito Educacional

Este proyecto es ideal para aprender:

- ✅ **Arquitectura de Microservicios** con Spring Boot
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

### **Backend Optimizado**
- **Endpoints REST**: 11 (eliminados 24 huérfanos)
- **Endpoints SOAP**: 2 (Auth + Music Search)
- **Controladores**: 2 (AuthController + SpotifyController)
- **Web Services**: 2 (AuthService + MusicService)
- **Entidades JPA**: 2 (AppUser + AppRole)
- **Patrones**: 8+ patrones arquitectónicos implementados

### **Base de Datos Simplificada**
- **Tablas**: 2 (eliminadas 11 innecesarias)
- **Registros**: ~12 usuarios de prueba
- **Reducción**: 94% menos datos, 90% menos tablas

### **Estructura del Frontend**
- **Angular 20**: Componentes standalone + Signals
- **Material Design**: UI consistente y moderna
- **Servicios**: AuthService + SpotifyService (optimizados)

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