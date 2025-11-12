#!/bin/bash

echo "🎵 DIAGNÓSTICO COMPLETO DE REPRODUCCIÓN"
echo "========================================"
echo ""

echo "📊 FLUJO CORRECTO DE REPRODUCCIÓN:"
echo "   1. Usuario hace clic en canción"
echo "   2. PlayerService.play() recibe datos de la canción"
echo "   3. PlayerService actualiza estado con canción (sin audioUrl aún)"
echo "   4. PlayerService llama a backend YouTube API"
echo "   5. PlayerService actualiza estado con audioUrl"
echo "   6. MusicPlayerComponent detecta cambio en estado"
echo "   7. MusicPlayerComponent usa audioUrl del estado"
echo "   8. Elemento <audio> carga y reproduce"
echo ""

echo "🔍 Verificando componentes del sistema..."
echo ""

echo "1️⃣ Backend YouTube API (HTTP a través de NGINX)..."
YOUTUBE_TEST=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/youtube/audio?name=Imagine&artist=John%20Lennon" 2>&1)
HTTP_CODE=$(echo "$YOUTUBE_TEST" | tail -1)
AUDIO_URL=$(echo "$YOUTUBE_TEST" | head -1)

if [ "$HTTP_CODE" = "200" ] && [[ $AUDIO_URL == https://*/videoplayback* ]]; then
    echo "   ✅ API funciona correctamente"
    echo "   📍 URL generada: ${AUDIO_URL:0:100}..."
else
    echo "   ❌ Error en API - HTTP $HTTP_CODE"
    echo "   Respuesta: $AUDIO_URL"
fi

echo ""
echo "2️⃣ Frontend accesible..."
FRONTEND_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:4200)
if [ "$FRONTEND_CODE" = "200" ]; then
    echo "   ✅ Frontend corriendo (HTTP $FRONTEND_CODE)"
else
    echo "   ❌ Frontend no accesible (HTTP $FRONTEND_CODE)"
fi

echo ""
echo "3️⃣ Servicios Docker..."
docker compose ps --format "{{.Name}}: {{.Status}}" | grep -E "(frontend|backend-app|nginx)" | head -4

echo ""
echo "✅ CAMBIOS APLICADOS EN ESTA CORRECCIÓN:"
echo ""
echo "   🔧 PlayerService (player.service.ts):"
echo "      - Usa HTTP:8080 en lugar de HTTPS:8443"
echo "      - Llama al backend YouTube una sola vez"
echo "      - Actualiza el estado con audioUrl obtenido"
echo ""
echo "   🔧 MusicPlayerComponent (music-player.component.ts):"
echo "      - Eliminada llamada duplicada a YouTube API"
echo "      - Ahora solo lee audioUrl del estado del PlayerService"
echo "      - Detecta cambios en el estado correctamente"
echo "      - Mejor manejo de errores de audio"
echo ""
echo "   ✨ Resultado esperado:"
echo "      - Sin errores ERR_CERT_AUTHORITY_INVALID"
echo "      - Sin llamadas HTTP duplicadas"
echo "      - Reproducción automática cuando se obtiene audioUrl"
echo ""
echo "🎯 PRUEBA AHORA:"
echo "   1. Abre: http://localhost:4200"
echo "   2. Haz clic en una canción"
echo "   3. Verifica en la consola del navegador:"
echo "      - 'Using audio URL from PlayerService: https://...' ✅"
echo "      - 'Audio obtained for: [nombre de canción]' ✅"
echo "      - NO debería haber error de certificado ✅"
echo "      - El audio debería comenzar a reproducirse ✅"
echo ""
echo "📝 Si ves 'Waiting for audio URL from PlayerService...' por mucho tiempo,"
echo "    verifica los logs del backend con:"
echo "    docker compose logs backend-app-1 --tail=20"