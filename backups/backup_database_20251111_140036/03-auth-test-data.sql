-- Archivo 03-auth-test-data.sql
-- Datos adicionales de prueba para autenticación en Musify

-- Insertar usuarios adicionales para pruebas de login
INSERT INTO app_users (username, password, email, enabled) VALUES
  ('estudiante', '{noop}estudiante123', 'estudiante@musify.com', true),
  ('profesor', '{noop}profesor456', 'profesor@musify.com', true),
  ('premium', '{noop}premium789', 'premium@musify.com', true),
  ('soporte', '{noop}soporte2023', 'soporte@musify.com', true),
  ('desarrollador', '{noop}dev2023', 'dev@musify.com', true),
  ('juan.perez', '{noop}juanperez', 'juan.perez@musify.com', true),
  ('maria.lopez', '{noop}marialopez', 'maria.lopez@musify.com', true),
  ('carlos.rodriguez', '{noop}carlos2023', 'carlos.rodriguez@musify.com', true),
  ('ana.martinez', '{noop}ana2023', 'ana.martinez@musify.com', true),
  ('test', '{noop}test123', 'test@musify.com', true);

-- Asignar roles a los nuevos usuarios
INSERT INTO app_roles (username, role) VALUES
  ('estudiante', 'USER'),
  ('profesor', 'USER'),
  ('premium', 'USER'),
  ('premium', 'PREMIUM'),
  ('soporte', 'USER'),
  ('soporte', 'SUPPORT'),
  ('desarrollador', 'USER'),
  ('desarrollador', 'ADMIN'),
  ('desarrollador', 'DEVELOPER'),
  ('juan.perez', 'USER'),
  ('maria.lopez', 'USER'),
  ('carlos.rodriguez', 'USER'),
  ('ana.martinez', 'USER'),
  ('test', 'USER');