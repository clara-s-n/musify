FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --omit=dev || npm install --omit=dev
COPY server.js ./
EXPOSE 9090
CMD ["npm","start"]
