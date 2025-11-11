-- Seed.sql - Script para cargar datos de prueba en la base de datos Musify
-- Este script inserta datos de ejemplo en todas las tablas principales

-- Usuarios
INSERT INTO usuario (nombre, apellido, email, premium) VALUES
  ('Juan', 'Pérez', 'juan@example.com', true),
  ('María', 'González', 'maria@example.com', false),
  ('Carlos', 'Rodríguez', 'carlos@example.com', true),
  ('Ana', 'Martínez', 'ana@example.com', false),
  ('Pedro', 'López', 'pedro@example.com', true);

-- Artistas
INSERT INTO artista (nombre, apellido) VALUES
  ('Ed', 'Sheeran'),
  ('Taylor', 'Swift'),
  ('Bad', 'Bunny'),
  ('Billie', 'Eilish'),
  ('The', 'Weeknd');

-- Álbumes
INSERT INTO album (tipo, artista_id) VALUES
  (false, 1), -- Álbum de Ed Sheeran
  (false, 2), -- Álbum de Taylor Swift
  (true, 3),  -- Single de Bad Bunny
  (false, 4), -- Álbum de Billie Eilish
  (false, 5); -- Álbum de The Weeknd

-- Canciones
INSERT INTO cancion (nombre, letra) VALUES
  ('Shape of You', 'The club isnt the best place to find a lover...'),
  ('Blank Space', 'Nice to meet you, where youve been?...'),
  ('Dakiti', 'Antes que se acabe el año yo quiero estar contigo...'),
  ('Bad Guy', 'White shirt now red, my bloody nose...'),
  ('Blinding Lights', 'I said, ooh, Im blinded by the lights...'),
  ('Perfect', 'I found a love for me...'),
  ('Cardigan', 'Vintage tee, brand new phone...'),
  ('Callaita', 'Todos le dicen que se pasaba de bellaca...'),
  ('Ocean Eyes', 'Ive been watching you for some time...'),
  ('Save Your Tears', 'I saw you dancing in a crowded room...');

-- Etiquetas
INSERT INTO etiqueta (tipo, nombre, descripcion) VALUES
  ('genero', 'Pop', 'Música popular contemporánea'),
  ('genero', 'Urbano', 'Reggaeton y música urbana latina'),
  ('genero', 'R&B', 'Rhythm and Blues contemporáneo'),
  ('animo', 'Feliz', 'Canciones alegres y optimistas'),
  ('animo', 'Melancólico', 'Canciones emotivas y reflexivas');

-- Playlists
INSERT INTO playlist (descripcion) VALUES
  ('Mis favoritas del 2023'),
  ('Para entrenar'),
  ('Canciones para estudiar'),
  ('Fiesta del fin de semana'),
  ('Música para el viaje');

-- Asignar canciones a álbumes
INSERT INTO album_canciones (album_id, cancion_id) VALUES
  (1, 1), -- Shape of You en álbum de Ed Sheeran
  (1, 6), -- Perfect en álbum de Ed Sheeran
  (2, 2), -- Blank Space en álbum de Taylor Swift
  (2, 7), -- Cardigan en álbum de Taylor Swift
  (3, 3), -- Dakiti en single de Bad Bunny
  (3, 8), -- Callaita en single de Bad Bunny
  (4, 4), -- Bad Guy en álbum de Billie Eilish
  (4, 9), -- Ocean Eyes en álbum de Billie Eilish
  (5, 5), -- Blinding Lights en álbum de The Weeknd
  (5, 10); -- Save Your Tears en álbum de The Weeknd

-- Asignar etiquetas a canciones
INSERT INTO cancion_etiqueta (cancion_id, etiqueta_id) VALUES
  (1, 1), -- Shape of You es Pop
  (1, 4), -- Shape of You es Feliz
  (2, 1), -- Blank Space es Pop
  (3, 2), -- Dakiti es Urbano
  (3, 4), -- Dakiti es Feliz
  (4, 1), -- Bad Guy es Pop
  (5, 3), -- Blinding Lights es R&B
  (6, 1), -- Perfect es Pop
  (6, 5), -- Perfect es Melancólico
  (7, 1), -- Cardigan es Pop
  (7, 5), -- Cardigan es Melancólico
  (8, 2), -- Callaita es Urbano
  (9, 1), -- Ocean Eyes es Pop
  (9, 5), -- Ocean Eyes es Melancólico
  (10, 3); -- Save Your Tears es R&B

-- Asignar etiquetas a álbumes
INSERT INTO album_etiqueta (album_id, etiqueta_id) VALUES
  (1, 1), -- Álbum de Ed Sheeran es Pop
  (2, 1), -- Álbum de Taylor Swift es Pop
  (3, 2), -- Single de Bad Bunny es Urbano
  (4, 1), -- Álbum de Billie Eilish es Pop
  (5, 3); -- Álbum de The Weeknd es R&B

-- Propietarios de playlists
INSERT INTO propietarios_playlist (playlist_id, usuario_id) VALUES
  (1, 1), -- Juan es propietario de "Mis favoritas del 2023"
  (2, 2), -- María es propietaria de "Para entrenar"
  (3, 3), -- Carlos es propietario de "Canciones para estudiar"
  (4, 4), -- Ana es propietaria de "Fiesta del fin de semana"
  (5, 5); -- Pedro es propietario de "Música para el viaje"

-- Canciones en playlists
INSERT INTO playlist_canciones (playlist_id, cancion_id) VALUES
  (1, 1), -- Shape of You en "Mis favoritas del 2023"
  (1, 3), -- Dakiti en "Mis favoritas del 2023"
  (1, 5), -- Blinding Lights en "Mis favoritas del 2023"
  (2, 1), -- Shape of You en "Para entrenar"
  (2, 3), -- Dakiti en "Para entrenar"
  (2, 5), -- Blinding Lights en "Para entrenar"
  (3, 6), -- Perfect en "Canciones para estudiar"
  (3, 7), -- Cardigan en "Canciones para estudiar"
  (3, 9), -- Ocean Eyes en "Canciones para estudiar"
  (4, 1), -- Shape of You en "Fiesta del fin de semana"
  (4, 3), -- Dakiti en "Fiesta del fin de semana"
  (4, 8), -- Callaita en "Fiesta del fin de semana"
  (5, 2), -- Blank Space en "Música para el viaje"
  (5, 5), -- Blinding Lights en "Música para el viaje"
  (5, 10); -- Save Your Tears en "Música para el viaje"

-- Usuarios les gusta canciones
INSERT INTO usuario_likes (usuario_id, cancion_id) VALUES
  (1, 1), -- A Juan le gusta Shape of You
  (1, 3), -- A Juan le gusta Dakiti
  (1, 5), -- A Juan le gusta Blinding Lights
  (2, 1), -- A María le gusta Shape of You
  (2, 6), -- A María le gusta Perfect
  (2, 9), -- A María le gusta Ocean Eyes
  (3, 2), -- A Carlos le gusta Blank Space
  (3, 7), -- A Carlos le gusta Cardigan
  (3, 10), -- A Carlos le gusta Save Your Tears
  (4, 3), -- A Ana le gusta Dakiti
  (4, 4), -- A Ana le gusta Bad Guy
  (4, 8), -- A Ana le gusta Callaita
  (5, 5), -- A Pedro le gusta Blinding Lights
  (5, 10); -- A Pedro le gusta Save Your Tears

-- Usuarios tienen etiquetas preferidas
INSERT INTO usuario_etiqueta (usuario_id, etiqueta_id) VALUES
  (1, 1), -- Juan prefiere Pop
  (1, 4), -- Juan prefiere canciones Felices
  (2, 1), -- María prefiere Pop
  (2, 5), -- María prefiere canciones Melancólicas
  (3, 1), -- Carlos prefiere Pop
  (3, 5), -- Carlos prefiere canciones Melancólicas
  (4, 2), -- Ana prefiere Urbano
  (4, 4), -- Ana prefiere canciones Felices
  (5, 3); -- Pedro prefiere R&B

-- Historial de búsquedas
INSERT INTO historial (busqueda, usuario_id) VALUES
  ('pop', 1),
  ('canciones felices', 1),
  ('taylor swift', 2),
  ('música para estudiar', 2),
  ('reggaeton', 3),
  ('bad bunny', 3),
  ('billie eilish', 4),
  ('canciones nuevas', 4),
  ('the weeknd', 5),
  ('r&b', 5);

-- Añadir usuarios con credenciales para pruebas de autenticación
-- Nota: En una aplicación real, las contraseñas serían hasheadas
-- Esta tabla es ficticia para simular la autenticación
CREATE TABLE IF NOT EXISTS app_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE
);

-- Insertar usuarios para pruebas de autenticación
INSERT INTO app_users (username, password, email, enabled) VALUES
  ('user', '{noop}password', 'user@demo.com', true),
  ('admin', '{noop}admin123', 'admin@demo.com', true);

-- Tabla de roles para manejo de permisos básicos
CREATE TABLE IF NOT EXISTS app_roles (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT unique_username_role UNIQUE (username, role)
);

-- Asignar roles a usuarios
INSERT INTO app_roles (username, role) VALUES
  ('user', 'USER'),
  ('admin', 'USER'),
  ('admin', 'ADMIN');
