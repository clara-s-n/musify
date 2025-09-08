# ====== Etapa 1: build ======
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
# Descarga dependencias en caché
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline
# Copia el código y compila
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ====== Etapa 2: runtime ======
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia el jar “reempacado” de Spring Boot (el que NO tiene 'original' en el nombre)
COPY --from=build /build/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
