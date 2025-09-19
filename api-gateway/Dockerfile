# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
LABEL authors="Xiomara"
WORKDIR /app
COPY api-gateway /app/api-gateway
COPY pom.xml /app/pom.xml
RUN mvn -f api-gateway/pom.xml clean package -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]