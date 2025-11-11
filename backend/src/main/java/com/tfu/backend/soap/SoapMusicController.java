package com.tfu.backend.soap;

import com.tfu.backend.spotify.SpotifyService;
import com.tfu.backend.spotify.SpotifyTrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador SOAP para servicios de música.
 * Expone endpoints SOAP/XML para búsqueda y descubrimiento de música.
 * 
 * Este controlador maneja requests y responses en formato XML,
 * proporcionando una interfaz SOAP complementaria a la API REST.
 */
@RestController
@RequestMapping("/soap/music")
public class SoapMusicController {
    
    private static final Logger logger = LoggerFactory.getLogger(SoapMusicController.class);
    
    private final SpotifyService spotifyService;
    
    public SoapMusicController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }
    
    /**
     * Endpoint SOAP para buscar música por términos de búsqueda.
     * Acepta y retorna XML.
     * 
     * @param searchRequest XML request con parámetros de búsqueda
     * @return XML response con lista de canciones encontradas
     */
    @PostMapping(value = "/search", 
                consumes = MediaType.APPLICATION_XML_VALUE, 
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> searchMusic(@RequestBody String searchRequest) {
        logger.info("SOAP Request - Búsqueda de música recibida: {}", searchRequest);
        
        try {
            // Parsear parámetros básicos del XML (implementación simple)
            String query = extractXmlValue(searchRequest, "query");
            String limitStr = extractXmlValue(searchRequest, "limit");
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(buildErrorResponse("El parámetro 'query' es obligatorio"));
            }
            
            int limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest().body(buildErrorResponse("El límite debe estar entre 1 y 50"));
            }
            
            // Realizar búsqueda usando el servicio de Spotify
            List<SpotifyTrackDto> tracks = spotifyService.searchTracks(query.trim(), limit);
            
            // Construir respuesta XML
            String xmlResponse = buildSearchResponse(tracks, query);
            
            logger.info("SOAP Response - Búsqueda exitosa: {} canciones encontradas", tracks.size());
            return ResponseEntity.ok(xmlResponse);
            
        } catch (Exception e) {
            logger.error("Error en búsqueda SOAP de música: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(buildErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint SOAP para obtener canciones aleatorias.
     * Acepta y retorna XML.
     * 
     * @param randomRequest XML request con parámetros opcionales
     * @return XML response con lista de canciones aleatorias
     */
    @PostMapping(value = "/random", 
                consumes = MediaType.APPLICATION_XML_VALUE, 
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getRandomMusic(@RequestBody String randomRequest) {
        logger.info("SOAP Request - Música aleatoria recibida: {}", randomRequest);
        
        try {
            // Parsear parámetros del XML
            String limitStr = extractXmlValue(randomRequest, "limit");
            
            int limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest().body(buildErrorResponse("El límite debe estar entre 1 y 50"));
            }
            
            // Obtener música aleatoria usando el servicio de Spotify
            List<SpotifyTrackDto> tracks = spotifyService.getRandomTracks(limit);
            
            // Construir respuesta XML
            String xmlResponse = buildRandomResponse(tracks);
            
            logger.info("SOAP Response - Música aleatoria exitosa: {} canciones obtenidas", tracks.size());
            return ResponseEntity.ok(xmlResponse);
            
        } catch (Exception e) {
            logger.error("Error en obtención SOAP de música aleatoria: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(buildErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }
    
    /**
     * Extrae el valor de un elemento XML simple.
     * Implementación básica para parsear XML sin librerías adicionales.
     */
    private String extractXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        
        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) return null;
        
        startIndex += startTag.length();
        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) return null;
        
        return xml.substring(startIndex, endIndex).trim();
    }
    
    /**
     * Construye una respuesta XML para búsqueda de música.
     */
    private String buildSearchResponse(List<SpotifyTrackDto> tracks, String query) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<searchMusicResponse xmlns=\"http://tfu.com/backend/soap/music\">\n");
        xml.append("  <success>true</success>\n");
        xml.append("  <message>Búsqueda completada exitosamente para '").append(escapeXml(query)).append("'</message>\n");
        xml.append("  <totalResults>").append(tracks.size()).append("</totalResults>\n");
        xml.append("  <tracks>\n");
        
        for (SpotifyTrackDto track : tracks) {
            xml.append("    <track>\n");
            xml.append("      <id>").append(escapeXml(track.getId())).append("</id>\n");
            xml.append("      <name>").append(escapeXml(track.getName())).append("</name>\n");
            xml.append("      <artist>").append(escapeXml(track.getArtists())).append("</artist>\n");
            if (track.getAlbum() != null) {
                xml.append("      <album>").append(escapeXml(track.getAlbum())).append("</album>\n");
            }
            if (track.getPreviewUrl() != null) {
                xml.append("      <previewUrl>").append(escapeXml(track.getPreviewUrl())).append("</previewUrl>\n");
            }
            if (track.getImageUrl() != null) {
                xml.append("      <imageUrl>").append(escapeXml(track.getImageUrl())).append("</imageUrl>\n");
            }
            xml.append("    </track>\n");
        }
        
        xml.append("  </tracks>\n");
        xml.append("</searchMusicResponse>");
        
        return xml.toString();
    }
    
    /**
     * Construye una respuesta XML para música aleatoria.
     */
    private String buildRandomResponse(List<SpotifyTrackDto> tracks) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<getRandomMusicResponse xmlns=\"http://tfu.com/backend/soap/music\">\n");
        xml.append("  <success>true</success>\n");
        xml.append("  <message>Obtenidas ").append(tracks.size()).append(" canciones aleatorias exitosamente</message>\n");
        xml.append("  <totalResults>").append(tracks.size()).append("</totalResults>\n");
        xml.append("  <tracks>\n");
        
        for (SpotifyTrackDto track : tracks) {
            xml.append("    <track>\n");
            xml.append("      <id>").append(escapeXml(track.getId())).append("</id>\n");
            xml.append("      <name>").append(escapeXml(track.getName())).append("</name>\n");
            xml.append("      <artist>").append(escapeXml(track.getArtists())).append("</artist>\n");
            if (track.getAlbum() != null) {
                xml.append("      <album>").append(escapeXml(track.getAlbum())).append("</album>\n");
            }
            if (track.getPreviewUrl() != null) {
                xml.append("      <previewUrl>").append(escapeXml(track.getPreviewUrl())).append("</previewUrl>\n");
            }
            if (track.getImageUrl() != null) {
                xml.append("      <imageUrl>").append(escapeXml(track.getImageUrl())).append("</imageUrl>\n");
            }
            xml.append("    </track>\n");
        }
        
        xml.append("  </tracks>\n");
        xml.append("</getRandomMusicResponse>");
        
        return xml.toString();
    }
    
    /**
     * Construye una respuesta XML de error.
     */
    private String buildErrorResponse(String errorMessage) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<errorResponse xmlns=\"http://tfu.com/backend/soap/music\">\n");
        xml.append("  <success>false</success>\n");
        xml.append("  <message>").append(escapeXml(errorMessage)).append("</message>\n");
        xml.append("  <totalResults>0</totalResults>\n");
        xml.append("</errorResponse>");
        
        return xml.toString();
    }
    
    /**
     * Escapa caracteres especiales para XML.
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}