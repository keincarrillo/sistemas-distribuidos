.PHONY: help run package up down build logs ps check clean web-install web web-dev web-lint web-typecheck web-format web-check web-clean

help:
	@echo "make run           - arranca el back en local (puerto 8080)"
	@echo "make package       - genera el jar del back en target/"
	@echo "make web-install   - instala las dependencias del front (bun)"
	@echo "make web           - compila el front hacia back/src/main/resources/static/"
	@echo "make web-dev       - servidor de desarrollo del front (Vite :5173, proxy al back)"
	@echo "make web-lint      - eslint sobre el front"
	@echo "make web-typecheck - comprobacion de tipos (tsc -b)"
	@echo "make web-format    - formatea el front con prettier"
	@echo "make web-check     - format:check + lint + typecheck"
	@echo "make up            - build + arranca el contenedor (puerto 8080)"
	@echo "make down          - detiene y elimina el contenedor"
	@echo "make logs          - logs del contenedor en tiempo real"
	@echo "make ps            - estado del contenedor"
	@echo "make check         - valida el front (web-check)"
	@echo "make clean         - limpia el back (target/) y el front (static/)"

run:
	$(MAKE) -C back run

package:
	$(MAKE) -C back package

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

web-install:
	$(MAKE) -C frontend install

web:
	$(MAKE) -C frontend build

web-dev:
	$(MAKE) -C frontend dev

web-lint:
	$(MAKE) -C frontend lint

web-typecheck:
	$(MAKE) -C frontend typecheck

web-format:
	$(MAKE) -C frontend format

web-check:
	$(MAKE) -C frontend check

check: web-check

clean:
	$(MAKE) -C back clean
	$(MAKE) -C frontend clean
