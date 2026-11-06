FROM oven/bun:1 AS web

WORKDIR /app

COPY frontend/package.json frontend/bun.lock* ./frontend/
RUN cd frontend && bun install --frozen-lockfile

COPY frontend ./frontend
RUN cd frontend && bun run build

FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY back/pom.xml .
RUN mvn -q dependency:go-offline

COPY back/src ./src
COPY --from=web /app/back/src/main/resources/static ./src/main/resources/static
RUN mvn -q package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/servicios-web-*.jar app.jar

# Render inyecta PORT al arrancar; el puerto por defecto es 8080
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
