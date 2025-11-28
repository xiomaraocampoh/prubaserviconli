# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copiamos el pom.xml y el código fuente desde el directorio actual (.)
COPY pom.xml .
COPY src ./src
# Compilamos (ya no necesitamos -f porque estamos en la raíz)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]