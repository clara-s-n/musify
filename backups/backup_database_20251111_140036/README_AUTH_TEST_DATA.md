# Datos de Prueba para Login en Musify

Este documento proporciona información sobre los datos de prueba disponibles para realizar pruebas de login en la API de Musify.

## Usuarios Disponibles

Todos estos usuarios están disponibles para probar la funcionalidad de autenticación:

| Username | Email | Password | Roles |
|----------|-------|----------|-------|
| user | user@demo.com | password | USER |
| admin | admin@demo.com | admin | USER, ADMIN |
| estudiante | estudiante@musify.com | estudiante123 | USER |
| profesor | profesor@musify.com | profesor456 | USER |
| premium | premium@musify.com | premium789 | USER, PREMIUM |
| soporte | soporte@musify.com | soporte2023 | USER, SUPPORT |
| desarrollador | dev@musify.com | dev2023 | USER, ADMIN, DEVELOPER |
| juan.perez | juan.perez@musify.com | juanperez | USER |
| maria.lopez | maria.lopez@musify.com | marialopez | USER |
| carlos.rodriguez | carlos.rodriguez@musify.com | carlos2023 | USER |
| ana.martinez | ana.martinez@musify.com | ana2023 | USER |
| test | test@musify.com | test123 | USER |

## Cómo utilizar los datos

1. **Para cargar los datos en la base de datos**: 
   - Asegúrate de que el archivo `03-auth-test-data.sql` se ejecute después de inicializar la base de datos.
   - Puedes hacerlo manualmente o incluirlo en tu proceso de inicio de Docker.

2. **Para realizar pruebas con Postman**:
   - Utiliza la colección Postman actualizada que incluye endpoints de login para diferentes tipos de usuarios.
   - Las credenciales ya están predefinidas en las solicitudes.

## Ejemplos de Login

### Usuario estándar
```json
{
    "email": "user@demo.com",
    "password": "password"
}
```

### Usuario administrador
```json
{
    "email": "admin@demo.com",
    "password": "admin"
}
```

### Usuario premium
```json
{
    "email": "premium@musify.com",
    "password": "premium789"
}
```

### Usuario de soporte
```json
{
    "email": "soporte@musify.com",
    "password": "soporte2023"
}
```

## Notas Importantes

- Todos estos usuarios tienen la propiedad `enabled` establecida en `true`.
- Las contraseñas se almacenan con el prefijo `{noop}` para indicar que no están encriptadas (solo para entornos de desarrollo).
- En un entorno de producción, siempre se deberían utilizar contraseñas encriptadas.