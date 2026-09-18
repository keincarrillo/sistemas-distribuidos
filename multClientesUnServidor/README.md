# multClientesUnServidor — Tienda en línea distribuida: varios clientes, un servidor

Práctica de la asignatura **Sistemas Distribuidos**: evolución de la versión con
sockets de [`clienteServidor`](../clienteServidor/) que enfoca el caso **muchos
clientes conectados a la vez contra un único servidor**.

En `clienteServidor` se lanza un cliente cada vez y el servidor acepta conexiones.
Aquí el servidor sigue siendo **uno solo**, pero debe atender **varios clientes
simultáneos**: cada cliente es un proceso Java separado (JVM distinta) que se
conecta por TCP, y el servidor le dedica un hilo mientras respeta un **límite de
clientes simultáneos** con un semáforo.

## De qué trata

- **Un solo servidor (`server/Servidor`)**: centraliza la tienda (cola acotada,
  almacén compartido y workers) y acepta conexiones TCP de muchos clientes a la vez.
- **Muchos clientes (`client/Cliente`)**: procesos independientes que se lanzan en
  paralelo (`java Main clientes N` o `make clientes N=...`) y envían pedidos al
  mismo servidor; cada uno recibe la confirmación de sus pedidos por su socket.
- **Concurrencia real de conexiones**: el servidor crea un hilo por cliente
  conectado (`ClienteHandler`), así varios productores remotos son atendidos en
  paralelo contra un único proceso servidor.
- **Límite de plazas (`server/RegistroClientes`)**: un semáforo justo controla el
  máximo de clientes simultáneos (`Config.MAX_CLIENTES_SIMULTANEOS`). Si el
  servidor está lleno, rechaza la conexión nueva y la contabiliza como rechazada.
- **Ids globales**: cada cliente corre en su propia JVM, así que el id de pedido
  **lo asigna el servidor** (`GeneradorIds`, patrón Singleton) para garantizar
  unicidad entre todos los clientes.
- **Estadísticas finales**: el servidor resume clientes atendidos, rechazados y
  pedidos procesados, demostrando el comportamiento multi cliente.
- Requiere **Java 25+** (el `Dockerfile` usa Eclipse Temurin 25).

## Estructura

| Ruta | Descripción |
| --- | --- |
| `src/Main.java` | Launcher: `java Main server`, `client` o `clientes [N]` |
| `src/config/Config.java` | Constantes centralizadas (puerto, límite, tiempos) |
| `src/model/Pedido.java` | Modelo: producto + cliente; el id lo asigna el servidor |
| `src/model/Resultado.java` | Respuesta al cliente (procesado o no) |
| `src/model/GeneradorIds.java` | Singleton que genera ids globales únicos |
| `src/cola/ColaPedidos.java` | Buffer circular acotado con `Lock` + `Condition` |
| `src/cola/ResultadosPendientes.java` | Futures por pedido para responder al cliente remoto |
| `src/recurso/Almacen.java` | Recurso compartido con `Semaphore` justo (capacidad 2) |
| `src/worker/Worker.java` | Hilo consumidor: procesa pedidos y notifica resultados |
| `src/server/Servidor.java` | Único servidor: `ServerSocket`, workers y aceptación de clientes |
| `src/server/RegistroClientes.java` | Límite de clientes simultáneos (Semaphore) y estadísticas |
| `src/server/ClienteHandler.java` | Atiende un socket: encola pedidos y devuelve resultados |
| `src/client/Cliente.java` | Productor remoto que se conecta por socket |
| `Dockerfile` | Imagen multi-etapa (JDK 25 compila, JRE 25 ejecuta el servidor) |
| `compose.yaml` | Servicio `server` con puerto 4444 publicado |
| `Makefile` | Automatiza compilación, ejecución y contenedor |

## Patrones de diseño usados

- **Singleton**: `GeneradorIds` garantiza una única fuente de ids en el servidor.
- **Handler por conexión (thread-per-client)**: `ClienteHandler` aísla la red de la
  lógica de negocio; el servidor crea un hilo por cliente conectado.
- **Semáforo como aforo**: `RegistroClientes` limita cuántos clientes atiende el
  servidor a la vez y rechaza el exceso.
- **Config centralizada**: `Config` evita valores mágicos esparcidos por el código.
- **Futures / Promises**: `ResultadosPendientes` asocia cada pedido con un
  `CompletableFuture` que el worker completa al terminar de procesarlo.
- **Separación por capas**: `model` (datos), `cola`/`recurso` (sincronización),
  `worker` (consumidores), `server`/`client` (red).

## Cómo se compara con clienteServidor

| Aspecto | clienteServidor | multClientesUnServidor |
| --- | --- | --- |
| Clientes | Se lanzan de a uno (`make run-client`) | Se lanzan de a muchos (`make clientes N=...`) |
| Servidor | Acepta clientes sin límite | Un solo servidor con límite de plazas |
| Conexiones | Hilo por cliente | Igual, pero con aforo y estadísticas |
| Rechazo | No existe | Semáforo lleno: conexión rechazada |
| Resumen final | Pedidos procesados | Pedidos + atendidos + rechazados |
| Comunicación | Sockets TCP | Sockets TCP (multi cliente) |

## Requisitos

- JDK 25+ para ejecutar en local.
- Docker + Docker Compose para el contenedor (opcional).

## Cómo ejecutarlo

### Local

En dos terminales:

```bash
# Terminal 1: compilar y arrancar el UNICO servidor (queda escuchando en 4444)
make compile
make run-server
```

```bash
# Terminal 2: lanzar 3 clientes a la vez (procesos Java separados)
make clientes
# o con la cantidad deseada:
make clientes N=8
# o un cliente suelto:
make run-client
```

También se puede lanzar sin Makefile:

```bash
javac --release 25 -d out $(find src -name '*.java')
java -cp out Main server
java -cp out Main clientes 3            # N clientes como procesos separados
java -cp out Main client localhost 4444 ClienteA
```

Cada cliente respeta `Config.PEDIDOS_POR_CLIENTE` pedidos; si se lanzan más
clientes que `Config.MAX_CLIENTES_SIMULTANEOS`, los excedentes son rechazados
y muestran un mensaje como `servidor lleno o caido`.

### Docker

```bash
make up      # construye y levanta el contenedor del servidor
make logs    # logs del server
make clientes N=3   # lanza N clientes desde el host contra el contenedor
make cliente        # lanza UN cliente con nombre NOMBRE=...
make ps      # estado del contenedor
make down    # detiene y elimina el contenedor
make clean   # down + borra artefactos locales
```

Todos los targets disponibles se ven con `make` (o `make help`).

## Salida esperada

Servidor (local o dentro del contenedor):

```
=== TIENDA EN LINEA DISTRIBUIDA (MULTI CLIENTES) ===

[Servidor] Escuchando en puerto 4444...
[Servidor] Maximo 5 clientes simultaneos
[Servidor] 2 workers activos
[Servidor] Listo. Ctrl+C para detener.

[Servidor] Cliente conectado (1/5 activos)
[Servidor] Cliente conectado (2/5 activos)
[Servidor] Cliente conectado (3/5 activos)
  [Cliente-50509] envia pedido#2 (Teclado)
  [Worker-1] procesa pedido#2 (Teclado) de Cliente-1
  ...
[Servidor] Cliente desconectado (0/5 activos)

=== FIN ===           (al pulsar Ctrl+C)
Workers: 2
Pedidos procesados: 15
Clientes atendidos: 3
Clientes rechazados: 0
Comunicacion: Sockets TCP (multi clientes, un servidor)
```

Si se supera el límite (por ejemplo `make clientes N=8` con límite 5):

```
[Servidor] Cliente conectado (5/5 activos)
[Servidor] Rechaza cliente: limite de 5 alcanzado
...
Clientes atendidos: 5
Clientes rechazados: 3
```

Clientes:

```
[Main] Lanzando 3 clientes hacia localhost:4444
[Main] Lanzado Cliente-1 (PID 25807)
[Cliente-1] conectado a localhost:4444
  [Cliente-1] envia pedido (Teclado)
  [Cliente-1] pedido#2 procesado (Teclado)
  ...
[Cliente-1] termino
[Main] Todos los clientes terminaron
```

La salida exacta varía en cada ejecución por el interleaving de los clientes, los
workers y el orden aleatorio de los productos.

## Notas

- Los clientes son **procesos separados** (JVM distintas): `make clientes` los lanza
  en paralelo, no como hilos de una misma JVM.
- El servidor se detiene con `Ctrl+C`; antes de salir cierra la cola y deja que los
  workers procesen lo pendiente, y después imprime el resumen final.
- Si el servidor está lleno, la conexión nueva se cierra: el cliente puede llegar a
  imprimir `conectado` antes de ser rechazado (race natural del handshake TCP).
- El contenedor ejecuta el servidor; los clientes se lanzan desde el host contra el
  puerto 4444 mapeado (`make clientes` o `make cliente`).