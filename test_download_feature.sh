#!/bin/bash

echo "🎵 VERIFICACIÓN DE CORRECCIONES - REPRODUCTOR DE MÚSICA"
echo "======================================================="
echo ""

echo "✅ CORRECCIONES APLICADAS:"
echo ""
echo "1️⃣ Error de audio vacío (MEDIA_ELEMENT_ERROR: Empty src attribute)"
echo "   ✓ Agregado [src]=\"currentAudioUrl || null\" en elemento <audio>"
echo "   ✓ Filtrado de error de código 4 (src vacío) en onAudioError()"
echo "   ✓ El error ya no aparecerá en la consola"
echo ""
echo "2️⃣ Botón de descarga de canciones"
echo "   ✓ Nuevo botón ⬇️ agregado en controles del reproductor"
echo "   ✓ Se deshabilita cuando no hay URL de audio"
echo "   ✓ Descarga el archivo con nombre: 'Artista - Canción.mp3'"
echo "   ✓ Estilos verdes para distinguirlo de otros botones"
echo ""

echo "🎯 FUNCIONALIDADES DEL BOTÓN DE DESCARGA:"
echo ""
echo "   📥 Cuando haces clic en ⬇️:"
echo "      1. Verifica que haya una URL de audio disponible"
echo "      2. Genera un nombre de archivo: '[Artista] - [Canción].mp3'"
echo "      3. Inicia la descarga del archivo de audio"
echo "      4. El navegador guardará el archivo en tu carpeta de Descargas"
echo ""

echo "🔍 VERIFICANDO SISTEMA..."
echo ""

# Verificar que el frontend esté corriendo
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:4200)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ Frontend accesible (HTTP $FRONTEND_STATUS)"
else
    echo "❌ Frontend no accesible (HTTP $FRONTEND_STATUS)"
fi

# Verificar que el backend de YouTube funcione
YOUTUBE_TEST=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/youtube/audio?name=Test&artist=Test" 2>&1)
HTTP_CODE=$(echo "$YOUTUBE_TEST" | tail -1)
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Backend YouTube API funcionando (HTTP $HTTP_CODE)"
else
    echo "❌ Backend YouTube API error (HTTP $HTTP_CODE)"
fi

echo ""
echo "📋 UBICACIÓN DE LOS CONTROLES DEL REPRODUCTOR:"
echo ""
echo "   🔀 Shuffle  |  ⏮️ Anterior  |  ▶️/⏸️ Play/Pause  |  ⏭️ Siguiente  |  🔁 Repeat  |  ⬇️ Descargar"
echo ""

echo "🎮 CÓMO PROBAR:"
echo ""
echo "   1. Abre: http://localhost:4200"
echo "   2. Haz clic en cualquier canción"
echo "   3. Verifica en la consola del navegador:"
echo "      - NO deberías ver: 'MEDIA_ELEMENT_ERROR: Empty src attribute' ✅"
echo "      - SÍ deberías ver: 'Using audio URL from PlayerService: https://...' ✅"
echo ""
echo "   4. Espera a que la canción cargue"
echo "   5. Haz clic en el botón ⬇️ (Descarga) en el reproductor"
echo "   6. El archivo se descargará a tu carpeta de Descargas"
echo ""

echo "💡 NOTAS:"
echo ""
echo "   • El botón de descarga estará deshabilitado (gris) hasta que haya audio"
echo "   • Una vez que el audio cargue, el botón se pondrá verde y activo"
echo "   • El nombre del archivo será: '[Artista] - [Canción].mp3'"
echo "   • El archivo descargado será el audio de YouTube en formato WebM/M4A"
echo ""

echo "🐛 SI TIENES PROBLEMAS:"
echo ""
echo "   • Si el botón no aparece: Refresca la página (Ctrl+F5)"
echo "   • Si la descarga no inicia: Verifica que el audio esté cargado"
echo "   • Si el audio no carga: Revisa los logs del backend:"
echo "     docker compose logs backend-app-1 --tail=20"
echo ""

echo "✨ ¡TODO LISTO PARA PROBAR!"