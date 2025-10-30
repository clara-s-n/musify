
# ====== Etapa 1: build ======
# Imagen base con Maven y JDK 21 para compilar la aplicación
FROM maven:3.9.9-eclipse-temurin-21 AS build
# Directorio de trabajo para la build
WORKDIR /build
# Copia el archivo de dependencias
COPY pom.xml .
# Descarga dependencias y las guarda en caché para acelerar builds futuras
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
# Copia el código fuente de la app
COPY src ./src
# Compila el proyecto y genera el JAR
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ====== Etapa 2: runtime ======
# Imagen base liviana solo con JRE para ejecutar la app
FROM eclipse-temurin:21-jre-alpine
# Directorio de trabajo para la app ya compilada
WORKDIR /app
# Copia el JAR generado en la etapa de build
COPY --from=build /build/target/*-SNAPSHOT.jar /app/app.jar
# Expone el puerto 8080 para el servicio
EXPOSE 8080
# Variable de entorno para el puerto
ENV SERVER_PORT=8080

RUN apk update && apk add yt-dlp
# Comando de entrada para ejecutar la app Spring Boot
ENTRYPOINT ["java","-jar","/app/app.jar"]

