# Etapa de compilacion: JDK
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p out && javac --release 25 -d out $(find src -name '*.java')

# Etapa de ejecucion: JRE
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/out/ ./out/
EXPOSE 4444 4445 4446
# El modo "server" y el id se pasan como argumentos
# (docker compose los sobrescribe por servicio con "command")
ENTRYPOINT ["java", "-cp", "out", "Main"]
CMD ["server", "1"]