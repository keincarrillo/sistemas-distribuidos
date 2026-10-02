FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY src/ ./src/

RUN mkdir -p out && javac --release 25 -d out $(find src -name '*.java')

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/out/ ./out/

EXPOSE 4444
ENTRYPOINT ["java", "-cp", "out", "Main", "server", "1"]
