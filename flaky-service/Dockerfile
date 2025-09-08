
# Imagen base de Node.js 20 en Alpine Linux (liviana)
FROM node:20-alpine
# Directorio de trabajo dentro del contenedor
WORKDIR /app
# Copia los archivos de dependencias
COPY package.json package-lock.json* ./
# Instala solo dependencias de producción (omite devDependencies)
RUN npm install --omit=dev || npm install --omit=dev
# Copia el código fuente del servidor
COPY server.js ./
# Expone el puerto 9090 para el servicio HTTP
EXPOSE 9090
# Comando de inicio del servicio
CMD ["npm","start"]
