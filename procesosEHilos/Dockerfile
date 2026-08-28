# --- Etapa 1: compilación --------------------------------------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Solo se copia el código fuente; el resto queda excluido (.dockerignore)
COPY src/ ./src/

# Compila todas las fuentes de una sola vez
RUN mkdir -p out && javac --release 25 -d out $(find src -name '*.java')

# --- Etapa 2: ejecución ----------------------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Carpeta de descargas; se recomienda montar un volumen (ver Makefile)
RUN mkdir -p descargas

# Solo se copian las clases compiladas, no las fuentes
COPY --from=build /app/out/ ./out/

# El programa escribe en ./descargas (relativo al working dir)
ENTRYPOINT ["java", "-cp", "out", "Main"]