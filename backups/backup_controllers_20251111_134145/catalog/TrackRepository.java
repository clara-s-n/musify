package com.tfu.backend.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de acceso a datos de pistas musicales.
 */
@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

  /**
   * Busca pistas por título que contengan el texto especificado.
   * La búsqueda es case-insensitive.
   * 
   * @param title Título a buscar
   * @return Lista de pistas que coinciden con el título
   */
  List<Track> findByTitleContainingIgnoreCase(String title);

  /**
   * Busca pistas por artista que contengan el texto especificado.
   * La búsqueda es case-insensitive.
   * 
   * @param artist Artista a buscar
   * @return Lista de pistas que coinciden con el artista
   */
  List<Track> findByArtistContainingIgnoreCase(String artist);

  /**
   * Busca pistas por álbum que contengan el texto especificado.
   * La búsqueda es case-insensitive.
   * 
   * @param album Álbum a buscar
   * @return Lista de pistas que coinciden con el álbum
   */
  List<Track> findByAlbumContainingIgnoreCase(String album);

  /**
   * Busca pistas por género.
   * La búsqueda es case-insensitive.
   * 
   * @param genre Género a buscar
   * @return Lista de pistas del género especificado
   */
  List<Track> findByGenreIgnoreCase(String genre);

  /**
   * Busca pistas por título, artista o álbum que contengan el texto especificado.
   * La búsqueda es case-insensitive.
   * 
   * @param query Texto a buscar
   * @return Lista de pistas que coinciden con la consulta
   */
  @Query("SELECT t FROM Track t WHERE " +
      "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(t.artist) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(t.album) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<Track> search(@Param("query") String query);

  /**
   * Busca pistas por año de lanzamiento.
   * 
   * @param year Año de lanzamiento a buscar
   * @return Lista de pistas del año especificado
   */
  List<Track> findByReleaseYear(Integer year);

  /**
   * Obtiene todas las pistas premium.
   * 
   * @return Lista de pistas premium
   */
  List<Track> findByPremiumTrue();

  /**
   * Obtiene todas las pistas que no son premium (gratuitas).
   * 
   * @return Lista de pistas gratuitas
   */
  List<Track> findByPremiumFalse();
}