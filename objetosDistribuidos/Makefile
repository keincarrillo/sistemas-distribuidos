.PHONY: help up down build logs ps restart full-restart clean \
        compile run-server servers stop-servers clientes cliente

help:
	@echo "make compile      - compila las fuentes a out/ (local, sin Docker)"
	@echo "make run-server   - ejecuta UN servidor RMI en local (puerto 4444)"
	@echo "make servers      - lanza los 3 servidores RMI en local (background)"
	@echo "make stop-servers - detiene los servidores locales lanzados con 'make servers'"
	@echo "make clientes     - lanza N clientes a la vez (N=..., por defecto 3)"
	@echo "make up           - levanta los 3 servidores con Docker (build si hace falta)"
	@echo "make down         - detiene y elimina los contenedores"
	@echo "make build        - reconstruye la imagen"
	@echo "make logs         - logs del servicio server1"
	@echo "make ps           - estado de los contenedores"
	@echo "make restart      - down + up"
	@echo "make clean        - down + borra artefactos locales (out/) y PIDs"

compile:
	javac --release 25 -d out $(shell find src -name '*.java')

run-server: compile
	java -cp out Main server 1

servers: compile
	@rm -f .servers.pid
	@for i in 1 2 3; do \
		java -cp out Main server $$i > logs-server-$$i.txt 2>&1 & \
		echo $$! >> .servers.pid; \
	done
	@echo "3 servidores RMI lanzados (PIDs en .servers.pid, logs en logs-server-*.txt)"

stop-servers:
	@if [ -f .servers.pid ]; then \
		xargs kill < .servers.pid 2>/dev/null; \
		rm -f .servers.pid; \
		echo "Servidores locales detenidos"; \
	else \
		echo "No hay servidores locales activos (.servers.pid no existe)"; \
	fi

clientes: compile
	java -cp out Main clientes $(if $(N),$(N),3)

cliente: compile
	java -cp out Main client $(if $(NOMBRE),$(NOMBRE),Cliente-docker) $(if $(HOST),$(HOST),localhost) $(if $(PUERTO),$(PUERTO),4444)

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

clean:
	docker compose down -v
	rm -rf out logs-server-*.txt .servers.pid