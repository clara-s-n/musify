CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    premium BOOLEAN DEFAULT FALSE
);

CREATE TABLE artista (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL

);

CREATE TABLE album (
    id SERIAL PRIMARY KEY,
    tipo BOOLEAN, -- TRUE = single, FALSE = álbum normal
    artista_id INTEGER NOT NULL REFERENCES artista(id)
);

CREATE TABLE cancion (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    letra TEXT
);

CREATE TABLE etiqueta (
    id SERIAL PRIMARY KEY,
    tipo TEXT CHECK (tipo IN ('genero','animo')),
    nombre TEXT NOT NULL,
    descripcion TEXT
);

CREATE TABLE playlist (
    id SERIAL PRIMARY KEY,
    descripcion TEXT
);

CREATE TABLE historial (
    id SERIAL PRIMARY KEY,
    busqueda TEXT NOT NULL,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id)
);


CREATE TABLE cancion_etiqueta (
    id SERIAL PRIMARY KEY,
    cancion_id INTEGER NOT NULL REFERENCES cancion(id),
    etiqueta_id INTEGER NOT NULL REFERENCES etiqueta(id)
);

CREATE TABLE album_etiqueta (
    id SERIAL PRIMARY KEY,
    album_id INTEGER NOT NULL REFERENCES album(id),
    etiqueta_id INTEGER NOT NULL REFERENCES etiqueta(id)
);

CREATE TABLE playlist_etiqueta (
    id SERIAL PRIMARY KEY,
    playlist_id INTEGER NOT NULL REFERENCES playlist(id),
    etiqueta_id INTEGER NOT NULL REFERENCES etiqueta(id)
);

CREATE TABLE usuario_etiqueta (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id),
    etiqueta_id INTEGER NOT NULL REFERENCES etiqueta(id)
);

CREATE TABLE usuario_likes (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id),
    cancion_id INTEGER NOT NULL REFERENCES cancion(id)
);


CREATE TABLE propietarios_playlist (
    id SERIAL PRIMARY KEY,
    playlist_id INTEGER NOT NULL REFERENCES playlist(id),
    usuario_id INTEGER NOT NULL REFERENCES usuario(id)
);

CREATE TABLE playlist_canciones (
    id SERIAL PRIMARY KEY,
    playlist_id INTEGER NOT NULL REFERENCES playlist(id),
    cancion_id INTEGER NOT NULL REFERENCES cancion(id)
);

CREATE TABLE album_canciones (
    id SERIAL PRIMARY KEY,
    album_id INTEGER NOT NULL REFERENCES album(id),
    cancion_id INTEGER NOT NULL REFERENCES cancion(id)
);
