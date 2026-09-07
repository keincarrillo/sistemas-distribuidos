# --- Etapa 1: compilacion --------------------------------------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Solo se copia el codigo fuente; el resto queda excluido (.dockerignore)
COPY src/ ./src/

# Compila todas las fuentes de una sola vez
RUN mkdir -p out && javac --release 25 -d out $(find src -name '*.java')

# --- Etapa 2: ejecucion ----------------------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Solo se copian las clases compiladas, no las fuentes
COPY --from=build /app/out/ ./out/

# El contenedor ejecuta el SERVIDOR (escucha conexiones TCP de clientes)
EXPOSE 4444
ENTRYPOINT ["java", "-cp", "out", "Main", "server"]