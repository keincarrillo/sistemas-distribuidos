.PHONY: help compile run package up down build logs ps clean

help:
	@echo "make compile  - compila las fuentes (local, sin Docker)"
	@echo "make run      - arranca el back en local (puerto 8080)"
	@echo "make package  - genera el jar ejecutable en target/"
	@echo "make up       - levanta el contenedor (delega en pwa/)"
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
	$(MAKE) -C .. up

down:
	$(MAKE) -C .. down

build:
	$(MAKE) -C .. build

logs:
	$(MAKE) -C .. logs

ps:
	$(MAKE) -C .. ps

clean:
	pkill -f "com.tienda.TiendaApplication" 2>/dev/null || true
	./mvnw -q clean
