.PHONY: help up down build logs ps restart full-restart clean \
        app-logs app-shell compile run-local

help:
	@echo "make up        - levanta el contenedor (build si hace falta) en segundo plano"
	@echo "make down      - detiene y elimina el contenedor"
	@echo "make build     - reconstruye la imagen"
	@echo "make logs      - logs del servicio en vivo"
	@echo "make ps        - estado del contenedor"
	@echo "make restart   - down + run (sin rebuild)"
	@echo "make clean     - down + borra artefactos locales de compilación (out/classes)"
	@echo "make app-logs  - logs del servicio app"
	@echo "make app-shell - shell dentro del contenedor app"

up:
	docker compose up --build -d

run:
	docker compose up -d

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f

ps:
	docker compose ps -a

restart: down run

full-restart: down up

clean:
	docker compose down -v
	rm -rf out/classes

app-logs:
	docker compose logs -f app

app-shell:
	docker compose exec app sh
