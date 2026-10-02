.PHONY: help up down build logs ps restart full-restart clean \
        compile run-server run-client clientes

help:
	@echo "make compile    - compila las fuentes a out/ (local, sin Docker)"
	@echo "make run-server - ejecuta UN servidor en local (puerto 4444)"
	@echo "make up         - levanta los 3 servidores (build si hace falta)"
	@echo "make down       - detiene y elimina los contenedores"
	@echo "make build      - reconstruye la imagen"
	@echo "make logs       - logs del servicio server1"
	@echo "make ps         - estado de los contenedores"
	@echo "make clientes   - lanza N clientes a la vez (N=..., por defecto 3)"
	@echo "make restart    - down + up"
	@echo "make clean      - down + borra artefactos locales (out/)"

compile:
	javac --release 25 -d out $(shell find src -name '*.java')

run-server: compile
	java -cp out Main server 1

up:
	docker compose up --build -d

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f server1

ps:
	docker compose ps -a

restart: down up

clientes: compile
	java -cp out Main clientes $(if $(N),$(N),3)

cliente: compile
	java -cp out Main client $(if $(NOMBRE),$(NOMBRE),Cliente-docker) $(if $(HOST),$(HOST),localhost) $(if $(PUERTO),$(PUERTO),4444)

clean:
	docker compose down -v
	rm -rf out
