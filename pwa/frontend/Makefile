.PHONY: help install build dev lint typecheck format check clean

help:
	@echo "make install     - instala las dependencias con bun"
	@echo "make build       - compila (tsc + vite) hacia ../back/src/main/resources/static/"
	@echo "make dev         - servidor de desarrollo (Vite :5173, proxy al back :8080)"
	@echo "make lint        - eslint sobre el codigo"
	@echo "make typecheck   - comprobacion de tipos (tsc -b)"
	@echo "make format      - formatea con prettier"
	@echo "make check       - format:check + lint + typecheck"
	@echo "make clean       - borra static/ generado"

install:
	bun install

build:
	bun run build

dev:
	bun run dev

lint:
	bun run lint

typecheck:
	bun run typecheck

format:
	bun run format

check:
	bun run format:check && bun run lint && bun run typecheck

clean:
	rm -rf ../back/src/main/resources/static
