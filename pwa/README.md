# pwa — Tienda en linea como PWA

Practica de **Sistemas Distribuidos**: la tienda en linea de las practicas anteriores
(expuesta como servicio web REST en [`serviciosWeb`](../serviciosWeb/README.md)), ahora
convertida en una **PWA instalable** (Progressive Web App).

Este proyecto es **independiente de `serviciosWeb/`**: `pwa/back/` es una copia
autonoma del backend y `pwa/frontend/` es el frontend nuevo. La practica original
`serviciosWeb/` no se ha tocado.

## De que trata

- **`back/`**: el mismo backend REST de Spring Boot de `serviciosWeb`
  (`POST /api/pedidos`, `GET /api/estado`, `GET/health`, nucleo concurrente con
  cola, workers, almacén y futures). Ver su [README](back/README.md) para la API.
- **`frontend/`**: interfaz web hecha con **React + TypeScript + Vite + Bun**:
  - formulario para enviar pedidos (con el catalogo real y los errores 400/503/504),
  - panel de estado en vivo (`GET /api/estado` + `GET/health`),
  - **PWA**: manifest + service worker (`vite-plugin-pwa`), app shell cacheado,
    la app abre sin conexion y avisa con un banner cuando la API no responde,
    boton "Instalar app".
- **Calidad**: ESLint (flat config), Prettier, `tsc` estricto, SCSS con
  arquitectura 7-1 y BEM, componentes reutilizables, un archivo = una
  responsabilidad (limite de lineas por archivo con `max-lines`).

## Estructura

| Ruta | Descripcion |
| --- | --- |
| `Makefile` | Orquestador: targets `web*`, `run`, `up`, `clean`... |
| `Dockerfile` | Imagen multi-etapa: bun (front) -> Maven (jar) -> JRE 25 |
| `compose.yaml` | Un servicio en el puerto 8080 |
| `back/` | Backend Spring Boot (copia de `serviciosWeb/`) |
| `back/Makefile` | Targets del back; los de contenedor delegan en este Makefile |
| `frontend/` | Frontend React + Vite + PWA |
| `frontend/Makefile` | `install`, `build`, `dev`, `lint`, `typecheck`, `format`, `check` |
| `frontend/src/` | Codigo del front (ver su [README](frontend/README.md)) |

## Requisitos

- **Java 25+** para el back (no hace falta Maven: `back/mvnw` lo descarga).
- **Bun** para el front (`bun --version`).
- **Docker** solo para el despliegue en contenedor.

## Como ejecutarlo

### Local (desarrollo)

```bash
make web-install    # una vez: instala dependencias del front

# terminal 1: back en http://localhost:8080
make run

# terminal 2: front en http://localhost:5173 (proxy /api y /health -> :8080)
make web-dev
```

Abre <http://localhost:5173>.

### Local (front ya compilado, servido por el back)

```bash
make web            # compila el front hacia back/src/main/resources/static/
make run            # back en http://localhost:8080 (sirve la PWA en /)
```

Abre <http://localhost:8080>. El service worker solo se registra en contextos
seguros (`https://` o `localhost`), asi que ahi ya puedes probar la instalacion
y el modo offline.

### Docker

```bash
make up             # build (bun + maven) y arranque en el puerto 8080
make logs           # logs en tiempo real
make ps             # estado
make down           # para y borra el contenedor
```

### Calidad

```bash
make web-lint       # eslint
make web-typecheck  # tsc -b
make web-format     # prettier --write
make web-check      # format:check + lint + typecheck
```

## Probar la PWA

- **Instalable**: Chrome/Edge -> icono de instalar en la barra de direcciones, o
  menu -> "Instalar aplicacion". En movil: "Anadir a pantalla de inicio".
- **Offline**: DevTools -> Network -> "Offline" -> recarga: la app arranca desde
  el cache (app shell) y muestra el banner de sin conexion; al volver la red,
  los pedidos vuelven a funcionar.
- **Auditoria**: Lighthouse -> categoria PWA.

## Despliegue en Render

1. Sube el repositorio a GitHub.
2. En Render: **New + Web Service** -> conecta el repo.
3. Elige **Docker** como runtime y fija **Root Directory = `pwa`** (ahi esta el
   `Dockerfile`, que compila front y back en una sola imagen).
4. Render inyecta `PORT`; la app ya lo respeta (`server.port=${PORT:8080}`).
5. La URL publica queda tipo `https://...onrender.com` (HTTPS: la PWA es
   instalable ahi sin mas configuracion).

## Notas

- El estado del back (cola, contadores, almacen) vive en memoria y se reinicia
  con cada deploy, igual que en las practicas con sockets.
- En el plan gratuito de Render el servicio duerme por inactividad: la primera
  peticion tarda mas en despertarlo.
- El service worker cachea el app shell (html/js/css/iconos); las llamadas a
  `/api` y `/health` siempre van a red para no mostrar datos viejos como
  frescos.
