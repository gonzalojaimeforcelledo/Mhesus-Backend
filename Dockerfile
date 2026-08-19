# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: correr con un JRE liviano (no todo el JDK) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mhesus-api.jar app.jar

# Railway/Render inyectan la variable PORT — la app ya la lee (server.port: ${PORT:8080})
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
