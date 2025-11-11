-- ============================================================================
-- MUSIFY - ESTRUCTURA DE BASE DE DATOS OPTIMIZADA (Solo lo esencial)
-- ============================================================================
-- Este script contiene únicamente las tablas necesarias para la funcionalidad
-- actual de la aplicación: autenticación de usuarios
-- 
-- Fecha de optimización: 2025-11-11
-- Tablas eliminadas: usuario, artista, album, cancion, etiqueta, playlist,
--                    historial y todas las tablas de relación
-- ============================================================================

-- Tabla para usuarios del sistema de autenticación
-- Esta tabla gestiona los usuarios que pueden hacer login
CREATE TABLE app_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,  -- Passwords con prefijo {noop} para simplicidad
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para roles del sistema de autorización
-- Maneja los permisos básicos (USER, ADMIN, PREMIUM, etc.)
CREATE TABLE app_roles (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_username FOREIGN KEY (username) REFERENCES app_users(username) ON DELETE CASCADE,
    CONSTRAINT unique_username_role UNIQUE (username, role)
);

-- Índices para optimizar consultas de autenticación
CREATE INDEX idx_app_users_username ON app_users(username);
CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_app_roles_username ON app_roles(username);

-- ============================================================================
-- NOTAS:
-- - La aplicación ahora usa únicamente Spotify API para música
-- - No se necesitan tablas de catálogo interno (canciones, artistas, álbumes)
-- - No se necesitan tablas de reproducción (historial, playlists)
-- - Solo mantenemos autenticación y autorización básica
-- ============================================================================