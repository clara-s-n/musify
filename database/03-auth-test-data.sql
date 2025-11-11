-- ============================================================================
-- MUSIFY - DATOS DE PRUEBA ADICIONALES (Usuarios extendidos para desarrollo)
-- ============================================================================
-- Este archivo contiene usuarios adicionales que NO están en el seed principal
-- Útil para pruebas extendidas, desarrollo y casos especiales
-- 
-- NOTA: Los usuarios básicos (estudiante, profesor, premium, test) ya están
--       en 02-seed.sql, aquí solo agregamos usuarios especializados
-- ============================================================================

-- Usuarios adicionales para casos especiales de testing
INSERT INTO app_users (username, password, email, enabled) VALUES
  ('soporte', '{noop}soporte2023', 'soporte@musify.com', true),
  ('desarrollador', '{noop}dev2023', 'dev@musify.com', true),
  ('juan.perez', '{noop}juanperez', 'juan.perez@musify.com', true),
  ('maria.lopez', '{noop}marialopez', 'maria.lopez@musify.com', true),
  ('carlos.rodriguez', '{noop}carlos2023', 'carlos.rodriguez@musify.com', true),
  ('ana.martinez', '{noop}ana2023', 'ana.martinez@musify.com', true);

-- Roles para usuarios especializados
INSERT INTO app_roles (username, role) VALUES
  ('soporte', 'USER'),
  ('soporte', 'SUPPORT'),
  ('desarrollador', 'USER'),
  ('desarrollador', 'ADMIN'),
  ('desarrollador', 'DEVELOPER'),
  ('juan.perez', 'USER'),
  ('maria.lopez', 'USER'),
  ('carlos.rodriguez', 'USER'),
  ('ana.martinez', 'USER');

-- ============================================================================
-- USUARIOS ADICIONALES:
-- 
-- Soporte técnico:
--   - soporte@musify.com / soporte2023 (USER, SUPPORT)
-- 
-- Desarrollo:
--   - dev@musify.com / dev2023 (USER, ADMIN, DEVELOPER)
-- 
-- Usuarios individuales para testing:
--   - juan.perez@musify.com / juanperez (USER)
--   - maria.lopez@musify.com / marialopez (USER)  
--   - carlos.rodriguez@musify.com / carlos2023 (USER)
--   - ana.martinez@musify.com / ana2023 (USER)
-- ============================================================================