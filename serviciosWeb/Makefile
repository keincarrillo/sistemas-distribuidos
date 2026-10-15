.PHONY: help compile run package up down build logs ps clean

help:
	@echo "make compile  - compila las fuentes (local, sin Docker)"
	@echo "make run      - arranca la app en local (puerto 8080)"
	@echo "make package  - genera el jar ejecutable en target/"
	@echo "make up       - levanta el contenedor (build si hace falta)"
	@echo "make down     - detiene y elimina el contenedor"
	@echo "make build    - reconstruye la imagen"
	@echo "make logs     - logs del contenedor en tiempo real"
	@echo "make ps       - estado del contenedor"
	@echo "make clean    - para procesos locales y borra target/"

compile:
	./mvnw -q compile

run: compile
	./mvnw spring-boot:run

package:
	./mvnw -q package -DskipTests

up:
	docker compose up --build -d

down:
	docker compose down

build:
	docker compose build

logs:
	docker compose logs -f

ps:
	docker compose ps -a

clean:
	pkill -f "com.tienda.TiendaApplication" 2>/dev/null || true
	./mvnw -q clean
