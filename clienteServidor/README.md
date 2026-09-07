# clienteServidor — Tienda en línea distribuida con Sockets

Práctica de la asignatura **Sistemas Distribuidos**: versión con **sockets TCP** del ejemplo productor-consumidor de [`procesosEHilos`](../procesosEHilos/).

En `procesosEHilos` los productores y consumidores son **hilos que comparten memoria**
(una `ColaPedidos` y un `Almacen`). Aquí se **distribuyen por red**: los clientes
son **procesos conectados por socket** que envían pedidos, y el **servidor** los
recibe, los encola y los procesa con workers, devolviendo la confirmación por el
mismo socket.

## De qué trata

- **Servidor (`server/Servidor`)**: centraliza la lógica de la tienda. Mantiene la
  cola acotada y el almacén compartido, lanza los workers (consumidores) y acepta
  conexiones TCP de clientes (productores remotos).
- **Clientes (`client/Cliente`)**: procesos independientes que se conectan al
  servidor y envían una serie de pedidos; reciben la confirmación de cada uno.
- **Patrón productor-consumidor distribuido**: la `ColaPedidos` sigue siendo un
  buffer acotado con `ReentrantLock` + `Condition`, pero ahora los productores
  escriben desde distintos procesos a través de la red.
- **Petición-respuesta sobre TCP**: cada pedido del cliente se registra en
  `ResultadosPendientes` con un `CompletableFuture`; cuando un worker lo procesa,
  resuelve el future y el `ClienteHandler` responde al cliente el `Resultado`.
- **Ids globales**: cada cliente corre en su propia JVM, así que el id de pedido
  **lo asigna el servidor** (`GeneradorIds`, patrón Singleton) para garantizar
  unicidad entre todos los clientes.
- Requiere **Java 25+** (el `Dockerfile` usa Eclipse Temurin 25).

## Estructura

| Ruta | Descripción |
| --- | --- |
| `src/Main.java` | Launcher: `java Main server` o `java Main client` |
| `src/config/Config.java` | Constantes centralizadas (puerto, capacidades, tiempos) |
| `src/model/Pedido.java` | Modelo: producto + cliente; el id lo asigna el servidor |
| `src/model/Resultado.java` | Respuesta al cliente (procesado o no) |
| `src/model/GeneradorIds.java` | Singleton que genera ids globales únicos |
| `src/cola/ColaPedidos.java` | Buffer circular acotado con `Lock` + `Condition` |
| `src/cola/ResultadosPendientes.java` | Futures por pedido para responder al cliente remoto |
| `src/recurso/Almacen.java` | Recurso compartido con `Semaphore` justo (capacidad 2) |
| `src/worker/Worker.java` | Hilo consumidor: procesa pedidos y notifica resultados |
| `src/server/Servidor.java` | `ServerSocket`, workers y aceptación de clientes |
| `src/server/ClienteHandler.java` | Atiende un socket: encola pedidos y devuelve resultados |
| `src/client/Cliente.java` | Productor remoto que se conecta por socket |
| `Dockerfile` | Imagen multi-etapa (JDK 25 compila, JRE 25 ejecuta el servidor) |
| `compose.yaml` | Servicio `server` con puerto 4444 publicado |
| `Makefile` | Automatiza compilación, ejecución y contenedor |

## Patrones de diseño usados

- **Singleton**: `GeneradorIds` garantiza una única fuente de ids en el servidor.
- **Handler por conexión**: `ClienteHandler` aísla la responsabilidad de red de la
  lógica de negocio; el servidor crea un hilo por cliente (patrón clásico de sockets).
- **Config centralizada**: `Config` evita valores mágicos esparcidos por el código.
- **Futures / Promises**: `ResultadosPendientes` asocia cada pedido con un
  `CompletableFuture` que el worker completa al terminar de procesarlo.
- **Separación por capas**: `model` (datos), `cola`/`recurso` (sincronización),
  `worker` (consumidores), `server`/`client` (red).

## Cómo se compara con procesosEHilos

| Aspecto | procesosEHilos | clienteServidor |
| --- | --- | --- |
| Productores | Hilos `Cliente` en la misma JVM | Procesos `Cliente` conectados por socket |
| Comunicación | Memoria compartida (cola) | Red TCP (serialización de objetos) |
| Consumidores | Hilos `Worker` locales | Workers en el servidor |
| Cola | `ColaPedidos` (Lock+Condition) | Misma `ColaPedidos`, en el servidor |
| Recurso | `Almacen` (Semaphore) | Mismo `Almacen`, en el servidor |
| Id de pedido | Contador estático de la JVM | `GeneradorIds` global del servidor |
| Confirmación al productor | Implícita (misma memoria) | `Resultado` serializado por el socket |

## Requisitos

- JDK 25+ para ejecutar en local.
- Docker + Docker Compose para el contenedor (opcional).

## Cómo ejecutarlo

### Local

En tres terminales:

```bash
# Terminal 1: compilar y arrancar el servidor (queda escuchando en 4444)
make compile
make run-server
```

```bash
# Terminal 2 y 3: lanzar clientes (productores remotos)
make run-client
# o con nombre personalizado: java -cp out Main client localhost 4444 ClienteA
```

También se puede lanzar sin Makefile:

```bash
javac --release 25 -d out $(find src -name '*.java')
java -cp out Main server
java -cp out Main client localhost 4444 ClienteA
```

### Docker

```bash
make up      # construye y levanta el contenedor del servidor
make logs    # logs del server
make cliente # lanza un cliente desde el host contra el contenedor (con NOMBRE=...)
make ps      # estado del contenedor
make down    # detiene y elimina el contenedor
make clean   # down + borra artefactos locales
```

Todos los targets disponibles se ven con `make` (o `make help`).

## Salida esperada

Servidor:

```
=== TIENDA EN LINEA DISTRIBUIDA (SOCKETS) ===

[Servidor] Escuchando en puerto 4444...
[Servidor] 2 workers activos
[Servidor] Listo. Ctrl+C para detener.

[Servidor] Cliente conectado: /127.0.0.1
  [Cliente-59254] envia pedido#2 (Teclado)
  [Worker-1] procesa pedido#2 (Teclado) de Cliente-A
  [Worker-1] entra a Almacen (1/2)
  [Worker-1] sale de Almacen (2/2)
  [Worker-1] completa pedido#2
  ...
[Servidor] Cliente desconectado (0 activos)

=== FIN ===           (al pulsar Ctrl+C)
Workers: 2
Pedidos procesados: 5
Comunicacion: Sockets TCP (productor-consumidor distribuido)
```

Cliente:

```
[Cliente-A] conectado a localhost:4444
  [Cliente-A] envia pedido (Teclado)
  [Cliente-A] pedido#2 procesado (Teclado)
  ...
[Cliente-A] termino
```

La salida exacta varía en cada ejecución por el interleaving de los workers y el
orden aleatorio de los productos.

## Notas

- Los clientes son **procesos separados** (JVM distintas): se lanzan con `java Main client`.
- El servidor se detiene con `Ctrl+C`; antes de salir cierra la cola y deja que los
  workers procesen lo pendiente.
- Si la cola está llena, el `ClienteHandler` se bloquea (la cola espera espacio)
  igual que hacía el productor en memoria.
- El contenedor ejecuta el servidor; el resto del ciclo completo (cliente desde fuera)
  se hace con `make cliente`.