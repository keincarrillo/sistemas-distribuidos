.PHONY: help up down build logs ps restart full-restart clean \
        compile run-server run-client clientes cliente server-logs

help:
	@echo "make compile    - compila las fuentes a out/ (local, sin Docker)"
	@echo "make run-server - ejecuta el UNICO servidor en local (bloqueante)"
	@echo "make run-client - ejecuta un cliente en local contra localhost"
	@echo "make clientes   - lanza N clientes a la vez (N=..., por defecto 3)"
	@echo "make up         - levanta el contenedor del servidor (build si hace falta)"
	@echo "make down       - detiene y elimina el contenedor"
	@echo "make build      - reconstruye la imagen"
	@echo "make logs       - logs del servicio server"
	@echo "make ps         - estado del contenedor"
	@echo "make restart    - down + run (sin rebuild)"
	@echo "make clean      - down + borra artefactos locales (out/)"

compile:
	javac --release 25 -d out $(shell find src -name '*.java')

run-server: compile
	java -cp out Main server

run-client: compile
	java -cp out Main client

# Lanza N clientes como procesos Java separados contra localhost:4444
# Uso: make clientes N=8   (si no se pasa N, usa 3)
clientes: compile
	java -cp out Main clientes $(if $(N),$(N),3)

# Cliente contra el servidor DENTRO del contenedor (puerto 4444 mapeado al host)
cliente:
	java -cp out Main client localhost 4444 $(strip $(if $(NOMBRE),$(NOMBRE),Cliente-docker))

up:
	docker compose up --build -d

run:
	docker compose up -d

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f server

server-logs: logs

ps:
	docker compose ps -a

restart: down run

full-restart: down up

clean:
	docker compose down -v
	rm -rf out