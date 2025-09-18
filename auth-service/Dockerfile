# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build
LABEL authors="Xiomara"
WORKDIR /app
COPY . .
RUN mvn -f auth-service/pom.xml clean package -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
