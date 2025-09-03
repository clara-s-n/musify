# syntax=docker/dockerfile:1
############################################
# Etapa de build (usa Maven + JDK 21)
############################################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom para cachear dependencias
COPY pom.xml ./

# Pre-descarga de dependencias (mejora tiempos de build)
# Usa BuildKit para cache (--mount) si está disponible; de lo contrario igual funciona.
RUN mvn -B -ntp -DskipTests dependency:go-offline

# Copiamos el código y construimos el fat jar
COPY src ./src
RUN mvn -B -ntp -DskipTests package

############################################
# Etapa de runtime (JRE liviano)
############################################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copiamos el jar generado (target/*.jar)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

# Puedes pasar JAVA_OPTS desde compose (memoria, GC, etc.)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
