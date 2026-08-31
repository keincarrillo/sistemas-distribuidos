# procesosEHilos — Tienda en línea concurrente

Práctica de la asignatura **Sistemas Distribuidos**: una tienda en línea en Java donde clientes generan pedidos y workers los procesan en paralelo usando hilos, sincronización (lock/condition y semáforo) y un proceso de monitor separado del proceso principal.

## De qué trata

- **Hilos productores**: `Cliente` genera pedidos aleatorios y los encola (patrón productor-consumidor).
- **Hilos consumidores**: `Worker` toma pedidos de la cola, usa un almacén compartido y los completa.
- **Cola acotada**: buffer circular con `ReentrantLock` + `Condition` que bloquea a los hilos cuando la cola está llena o vacía.
- **Recurso compartido**: `Almacen` con `Semaphore` justo (capacidad 2) que limita cuántos workers acceden a la vez.
- **Proceso separado**: `Monitor` se lanza como proceso del sistema operativo independiente vía `ProcessBuilder`, muestra su PID y permanece activo mientras dura la simulación; el proceso principal lo termina al finalizar.
- Requiere **Java 25+** (el `Dockerfile` usa Eclipse Temurin 25).

## Estructura

| Ruta | Descripción |
| --- | --- |
| `src/Main.java` | Punto de entrada: invoca `Tienda.ejecutar()` |
| `src/Tienda.java` | Orquestador: crea cola y almacén, lanza el proceso monitor, arranca los hilos, espera su fin y cierra todo |
| `src/hilo/Cliente.java` | Hilo productor: genera pedidos y los encola |
| `src/hilo/Worker.java` | Hilo consumidor: toma pedidos de la cola, usa el almacén y los completa |
| `src/cola/ColaPedidos.java` | Cola acotada productor-consumidor con `Lock` + `Condition` |
| `src/recurso/Almacen.java` | Recurso compartido con `Semaphore` justo de capacidad 2 |
| `src/model/Pedido.java` | Modelo de datos: id, producto y timestamp |
| `src/proceso/Monitor.java` | Proceso OS separado lanzado con `ProcessBuilder` |
| `Dockerfile` | Imagen multi-etapa (JDK 25 compila, JRE 25 ejecuta) |
| `compose.yaml` | Servicio `app` del contenedor |
| `Makefile` | Automatiza build, ejecución y contenedor |

## Conceptos que cubre

- Concurrencia con hilos Java (`Thread`, `join`).
- Patrón productor-consumidor con cola acotada (`Lock` + `Condition`).
- Exclusión mutua con semáforo (`Semaphore` justo) sobre un recurso compartido.
- Comunicación entre procesos: lanzar y terminar un proceso externo (`ProcessBuilder`, `ProcessHandle`).

## Requisitos

- JDK 25+ para ejecutar en local.
- Docker + Docker Compose para el contenedor (opcional).

## Cómo ejecutarlo

En local:

```bash
javac --release 25 -d out $(find src -name '*.java')
java -cp out Main
```

> El monitor se lanza con el classpath `out/`, así que hay que compilar antes de ejecutar.

Con Docker:

```bash
make up    # construye y levanta el contenedor
make logs  # logs del servicio
make ps    # estado del contenedor
make down  # detiene y elimina el contenedor
make clean # down + borra artefactos locales de compilación
```

Todos los targets disponibles se ven con `make` (o `make help`).

## Salida esperada

```
=== TIENDA EN LINEA CONCURRENTE ===

[Main] Monitor PID: 19217

  [Cliente-1] crea pedido#1 (Teclado)
  [Worker-2] procesa pedido#1 (Teclado)
  [Cliente-2] crea pedido#2 (Teclado)
  [Worker-1] procesa pedido#2 (Teclado)
  [Worker-1] entra a Almacen (0/2)
  [Worker-2] entra a Almacen (1/2)
╔══════════════════════════════════════╗
║  MONITOR - PID: 19217               ║
║  Proceso OS separado activo         ║
╚══════════════════════════════════════╝
  ...
  [Cliente-2] termino
  [Worker-1] termino, proceso 5 pedidos
  [Worker-2] termino, proceso 5 pedidos

=== FIN ===
Hilos: 2 clientes + 2 workers
Proceso separado: monitor
Sync: Lock+Conditions (cola), Semaphore (almacen)
```

La salida exacta varía en cada ejecución por el interleaving de los hilos (los productos y el orden de los pedidos son aleatorios).

## Notas

- El programa termina solo: los clientes crean un número fijo de pedidos, la cola se cierra y los workers salen al quedarse vacía.
- El monitor es un proceso OS independiente (no un hilo): se ve con `ps` mientras la simulación corre y se termina con `destroyForcibly()` al final.
- En el contenedor, la ejecución termina y queda en estado `Exited (0)` (no es un servidor).