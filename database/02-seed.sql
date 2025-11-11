-- ============================================================================
-- MUSIFY - DATOS DE PRUEBA OPTIMIZADOS (Solo autenticación)
-- ============================================================================
-- Este script contiene únicamente los usuarios necesarios para probar
-- la funcionalidad de autenticación de la aplicación educacional
-- 
-- Fecha de optimización: 2025-11-11
-- Usuarios simplificados: Solo perfiles esenciales para demos y pruebas
-- ============================================================================

-- Usuarios esenciales para pruebas
INSERT INTO app_users (username, password, email, enabled) VALUES
  -- Usuarios básicos para demos
  ('user', '{noop}password', 'user@demo.com', true),
  ('admin', '{noop}admin', 'admin@demo.com', true),
  
  -- Usuario educacional (coincide con el contexto académico)
  ('estudiante', '{noop}estudiante123', 'estudiante@musify.com', true),
  ('profesor', '{noop}profesor456', 'profesor@musify.com', true),
  
  -- Usuario para pruebas de funciones premium
  ('premium', '{noop}premium789', 'premium@musify.com', true),
  
  -- Usuario genérico para testing
  ('test', '{noop}test123', 'test@musify.com', true);

-- Asignación de roles optimizada
INSERT INTO app_roles (username, role) VALUES
  -- Roles básicos
  ('user', 'USER'),
  ('admin', 'USER'),
  ('admin', 'ADMIN'),
  
  -- Roles educacionales
  ('estudiante', 'USER'),
  ('profesor', 'USER'),
  ('profesor', 'EDUCATOR'),
  
  -- Roles premium para testing
  ('premium', 'USER'),
  ('premium', 'PREMIUM'),
  
  -- Usuario de testing básico
  ('test', 'USER');

-- ============================================================================
-- CREDENCIALES DE ACCESO RÁPIDO:
-- 
-- Demo básico:
--   - user@demo.com / password (USER)
--   - admin@demo.com / admin (USER, ADMIN)
-- 
-- Contexto educacional:
--   - estudiante@musify.com / estudiante123 (USER)  
--   - profesor@musify.com / profesor456 (USER, EDUCATOR)
-- 
-- Testing premium:
--   - premium@musify.com / premium789 (USER, PREMIUM)
-- 
-- Testing general:
--   - test@musify.com / test123 (USER)
-- ============================================================================