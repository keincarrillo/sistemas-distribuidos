# serviciosWeb — Tienda en linea como servicio web REST

Practica de la asignatura **Sistemas Distribuidos**: la misma tienda en linea de las
practicas anteriores (`procesosEHilos`, `clienteServidor`, `multClientesUnServidor`,
`multClientesMultServidores`), pero expuesta como **servicio web REST** con
**Spring Boot** y desplegable en **Render**.

Aqui la comunicacion ya no es por sockets TCP con `ObjectStreams`, sino por **HTTP +
JSON**: cualquier cliente que sepa hacer peticiones web (Postman, curl, un navegador
o cualquier lenguaje) puede usar la tienda.

## De que trata

- **API REST (`api/`)**: `POST /api/pedidos` recibe un pedido en JSON y devuelve el
  resultado cuando un worker lo procesa; `GET /api/estado` expone estadisticas;
  `GET /health` sirve de health check para Render.
- **Mismo nucleo concurrente que las practicas anteriores**:
  - `cola/ColaPedidos`: buffer circular con `Lock` + `Condition` (productor-consumidor).
  - `cola/ResultadosPendientes`: `CompletableFuture` por pedido (futures/promises).
  - `recurso/Almacen`: recurso compartido con `Semaphore` justo (aforo).
  - `worker/Worker`: hilos consumidores que procesan pedidos.
  - `model/GeneradorIds`: singleton con `AtomicInteger` para ids unicos.
- **Fachada (`PedidoService`)**: valida contra el catalogo, encola y espera el
  resultado con timeout; el controlador solo conoce esta clase.
- **Aforo de peticiones (`server/RegistroClientes`)**: limite de peticiones HTTP
  simultaneas; al superarlo se responde `503` (equivalente al limite de clientes
  simultaneos de la practica de sockets).
- **Configuracion por entorno (`config/TiendaProperties`)**: workers, capacidades y
  timeouts se pueden cambiar con variables de entorno (p. ej. `TIENDA_WORKERS=4`).
- Requiere **Java 25+** (el Dockerfile usa Eclipse Temurin 25).

## Estructura

| Ruta | Descripcion |
| --- | --- |
| `src/main/java/com/tienda/TiendaApplication.java` | Arranque de Spring Boot |
| `config/TiendaProperties.java` | Ajustes inyectables via entorno |
| `config/TiendaConfig.java` | Beans del nucleo (cola, almacen, futures, aforo) |
| `model/Pedido.java` | Pedido; el id lo asigna el servicio |
| `model/Resultado.java` | Respuesta al cliente (procesado o no) |
| `model/GeneradorIds.java` | Singleton que genera ids unicos |
| `cola/ColaPedidos.java` | Buffer circular con `tryPut` no bloqueante |
| `cola/ResultadosPendientes.java` | Futures por pedido |
| `recurso/Almacen.java` | Recurso compartido con `Semaphore` |
| `server/RegistroClientes.java` | Limite de peticiones simultaneas |
| `worker/Worker.java` | Hilo consumidor |
| `worker/GestorWorkers.java` | Ciclo de vida de los workers (`@PostConstruct`/`@PreDestroy`) |
| `api/PedidoService.java` | Fachada: validar, encolar, esperar |
| `api/PedidoController.java` | Endpoints REST |
| `api/ManejadorErroresApi.java` | Excepciones de dominio -> 400/503/504 |
| `Dockerfile` | Imagen multi-etapa (Maven compila, JRE 25 ejecuta) |
| `compose.yaml` | Un servicio en el puerto 8080 |
| `Makefile` | Compilacion, ejecucion y contenedores |

## Patrones de diseño usados

- **Singleton**: GeneradorIds garantiza ids unicos por instancia del servicio.
- **Productor-consumidor**: peticiones HTTP = productores; workers = consumidores;
  la cola acotada los desacopla.
- **Futures / Promises**: cada pedido se registra con su `CompletableFuture` y el
  hilo HTTP espera el resultado sin bloquear a los demas clientes.
- **Semafaro como aforo**: `Almacen` limita la concurrencia real del trabajo pesado;
  `RegistroClientes` limita peticiones en vuelo.
- **Fachada**: `PedidoService` escoleta la logica; el controlador solo traduce HTTP.
- **DTOs**: `PedidoRequest`/`ResultadoResponse`/`EstadoResponse` separan la API JSON
  del modelo interno.
- **Separacion por capas**: model, cola, recurso, worker, server, api, config.

## La API

### `POST /api/pedidos`

```json
{ "producto": "Laptop", "cliente": "Ana" }
```

Producto valido (200):

```json
{ "pedidoId": 1, "producto": "Laptop", "procesado": true, "procesadoEn": 1792024624884 }
```

Catalogo: `Laptop`, `Mouse`, `Teclado`, `Monitor`, `USB`.

| Codigo | Cuando |
| --- | --- |
| `200` | Pedido procesado por un worker |
| `400` | Producto no valido o ausente |
| `503` | Cola llena o aforo de peticiones simultaneas agotado |
| `504` | El worker no completo el pedido a tiempo |

### `GET /api/estado`

```json
{
  "estado": "ok",
  "colaActual": 0, "capacidadCola": 5,
  "almacenDisponible": 2, "capacidadAlmacen": 2,
  "peticionesActivas": 0, "peticionesAtendidas": 6, "peticionesRechazadas": 7,
  "pedidosProcesados": 6, "workers": 2
}
```

### `GET /health`

```json
{ "status": "UP" }
```

## Como ejecutarlo

### Local

Necesitas Java 25+ (no hace falta Maven: el wrapper `./mvnw` lo descarga solo).

```bash
make run            # arranca en http://localhost:8080
```

En otra terminal, con curl:

```bash
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Laptop","cliente":"Ana"}'

curl http://localhost:8080/api/estado
```

En Postman: metodo `POST`, URL `http://localhost:8080/api/pedidos`, body -> raw -> JSON,
y el mismo JSON de arriba.

### Docker

```bash
make up             # build + arranque en puerto 8080
make logs           # logs en tiempo real
make ps             # estado
make down           # para y borra el contenedor
```

### Carga

Para ver el aforo en accion, lanza una rafaga de peticiones simultaneas (con
`xargs -P` o multiples pestañas de Postman). Con la configuracion por defecto
(aforo 5), algunas responden `503`:

```bash
seq 12 | xargs -P 12 -I{} curl -s -o /dev/null -w "{} -> %{http_code}\n" \
  -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Mouse","cliente":"C{}"}'
```

## Despliegue en Render

1. Sube este repositorio a GitHub.
2. En Render: **New + Web Service** -> conecta el repo.
3. Elige **Docker** como runtime (Render detecta el `Dockerfile`).
4. Render inyecta la variable `PORT`; la app ya la respeta
   (`server.port=${PORT:8080}`).
5. Al desplegar, la URL publica queda tipo `https://servicios-web-xxxx.onrender.com`.

Prueba en la URL publica:

```bash
curl https://<tu-servicio>.onrender.com/health
curl -X POST https://<tu-servicio>.onrender.com/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Teclado","cliente":"Postman"}'
```

## Notas

- El estado (cola, contadores, almacen) vive en memoria: se reinicia con cada
  deploy o reinicio del servicio, igual que en las practicas con sockets.
- Los ids de pedido son unicos dentro de cada instancia del servicio.
- En Render el plan gratuito duerme el servicio tras un periodo de inactividad: la
  primera peticion tarda mas en despertarlo.
- El aforo y las capacidades se ajustan por entorno, por ejemplo:
  `TIENDA_WORKERS=4`, `TIENDA_CAPACIDAD_COLA=20`, `TIENDA_MAX_PETICIONES_SIMULTANEAS=10`.
