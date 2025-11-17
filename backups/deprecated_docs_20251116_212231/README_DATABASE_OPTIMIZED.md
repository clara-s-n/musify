# Base de Datos Optimizada - Musify (Aplicación Educacional)

## 📊 Estructura Simplificada

La base de datos ha sido **optimizada para el propósito educacional** de la aplicación, manteniendo únicamente las tablas esenciales para la funcionalidad actual.

### ✅ Tablas Activas (Solo 2)

| Tabla | Propósito | Registros |
|-------|-----------|-----------|
| `app_users` | Usuarios del sistema de autenticación | 6 usuarios esenciales |
| `app_roles` | Roles y permisos | Roles básicos (USER, ADMIN, PREMIUM, EDUCATOR) |

### 🗑️ Tablas Eliminadas (11)

- `usuario` - Perfil de usuarios (reemplazado por autenticación simple)
- `artista` - Catálogo de artistas (ahora usa Spotify API)
- `album` - Catálogo de álbumes (ahora usa Spotify API)
- `cancion` - Catálogo de canciones (ahora usa Spotify API)
- `etiqueta` - Etiquetas y géneros (no se usa)
- `playlist` - Playlists de usuarios (no implementado en frontend)
- `historial` - Historial de búsquedas (no se usa)
- `*_etiqueta` - Tablas de relación (eliminadas con sus entidades)
- `usuario_likes` - Likes de usuarios (no implementado)
- `propietarios_playlist` - Propietarios de playlists (no se usa)
- `playlist_canciones` - Canciones en playlists (no se usa)

## 👥 Usuarios de Prueba

### Usuarios Principales (en 02-seed.sql)
| Username | Password | Email | Roles | Propósito |
|----------|----------|-------|-------|-----------|
| user | password | user@demo.com | USER | Demo básico |
| admin | admin | admin@demo.com | USER, ADMIN | Administración |
| estudiante | estudiante123 | estudiante@musify.com | USER | Contexto educacional |
| profesor | profesor456 | profesor@musify.com | USER, EDUCATOR | Contexto educacional |
| premium | premium789 | premium@musify.com | USER, PREMIUM | Testing funciones premium |
| test | test123 | test@musify.com | USER | Testing general |

### Usuarios Extendidos (en 03-auth-test-data.sql)
| Username | Password | Email | Roles | Propósito |
|----------|----------|-------|-------|-----------|
| soporte | soporte2023 | soporte@musify.com | USER, SUPPORT | Soporte técnico |
| desarrollador | dev2023 | dev@musify.com | USER, ADMIN, DEVELOPER | Desarrollo |
| juan.perez | juanperez | juan.perez@musify.com | USER | Testing individual |
| maria.lopez | marialopez | maria.lopez@musify.com | USER | Testing individual |
| carlos.rodriguez | carlos2023 | carlos.rodriguez@musify.com | USER | Testing individual |
| ana.martinez | ana2023 | ana.martinez@musify.com | USER | Testing individual |

## 🚀 Beneficios de la Optimización

### Rendimiento
- **90% menos tablas** (de 13 a 2)
- **95% menos datos** (de 193 registros a ~12)
- **Consultas más rápidas** (sin JOINs complejos)
- **Menor uso de memoria** en PostgreSQL

### Simplicidad
- **Estructura más clara** para propósitos educacionales
- **Enfoque en autenticación** (el core de la app)
- **Menos complejidad** para estudiantes
- **Debugging más fácil**

### Mantenibilidad
- **Solo 2 entidades JPA** (`AppUser`, `AppRole`)
- **Scripts SQL más legibles**
- **Menos dependencias entre tablas**
- **Backup y restore más rápidos**

## 📂 Archivos de Base de Datos

```
database/
├── 01-init.sql              # Estructura optimizada (solo 2 tablas)
├── 02-seed.sql              # Datos esenciales (6 usuarios básicos)
├── 03-auth-test-data.sql    # Usuarios adicionales (6 usuarios extendidos)
├── 01-init-legacy.sql       # Estructura original (backup)
├── 02-seed-legacy.sql       # Datos originales (backup)
└── README_AUTH_TEST_DATA.md # Esta documentación
```

## 🔐 Uso de Credenciales

### Para Desarrollo Rápido
```bash
# Login básico
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@demo.com","password":"password"}'

# Login educacional  
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"estudiante@musify.com","password":"estudiante123"}'
```

### Para Testing de Roles
- **USER**: Acceso básico a la aplicación
- **ADMIN**: Funciones administrativas (si se implementan)
- **PREMIUM**: Funciones premium (si se implementan)
- **EDUCATOR**: Funciones educacionales (si se implementan)
- **SUPPORT**: Funciones de soporte (si se implementan)
- **DEVELOPER**: Acceso completo para desarrollo

## ⚠️ Notas de Seguridad

- **Solo para desarrollo**: Contraseñas en texto plano con `{noop}`
- **No usar en producción**: Implementar hashing apropiado
- **Rate limiting**: Máximo 5 intentos de login por minuto
- **JWT tokens**: Expiran según configuración en `application.yaml`