#!/bin/bash

echo "🎵 PRUEBA DE REPRODUCCIÓN - Diagnóstico Completo"
echo "=============================================="
echo ""

echo "1️⃣ Verificando endpoint de YouTube a través de NGINX (HTTP:8080)..."
RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/youtube/audio?name=Test&artist=Artist" 2>&1)
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
URL=$(echo "$RESPONSE" | head -1)

if [ "$HTTP_CODE" = "200" ] && [[ $URL == https://* ]]; then
    echo "✅ Endpoint funciona correctamente"
    echo "   HTTP Status: $HTTP_CODE"
    echo "   URL obtenida: ${URL:0:100}..."
else
    echo "❌ Error en el endpoint"
    echo "   HTTP Status: $HTTP_CODE"
    echo "   Respuesta: $URL"
fi

echo ""
echo "2️⃣ Verificando servicios Docker..."
docker compose ps --format "table {{.Name}}\t{{.Status}}"

echo ""
echo "3️⃣ Verificando acceso al frontend..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:4200)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ Frontend accesible en http://localhost:4200 (HTTP $FRONTEND_STATUS)"
else
    echo "❌ Frontend no accesible (HTTP $FRONTEND_STATUS)"
fi

echo ""
echo "4️⃣ SOLUCIÓN APLICADA:"
echo "   ✅ PlayerService cambiado de HTTPS:8443 a HTTP:8080"
echo "   ✅ MusicPlayerComponent cambiado de HTTPS:8443 a HTTP:8080"
echo "   ✅ Ahora usa NGINX como proxy (sin problemas de certificado SSL)"
echo ""
echo "🎯 PRUEBA LA REPRODUCCIÓN:"
echo "   1. Abre: http://localhost:4200"
echo "   2. Haz clic en cualquier canción"
echo "   3. El reproductor debería:"
echo "      - Mostrar la información correcta de la canción"
echo "      - Obtener la URL de audio de YouTube"
echo "      - Reproducir el audio automáticamente"
echo ""
echo "📝 Si aún hay errores, verifica la consola del navegador"
echo "   Los errores ERR_CERT_AUTHORITY_INVALID deberían haber desaparecido"