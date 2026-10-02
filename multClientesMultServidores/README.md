# multClientesMultServidores — Tienda en linea distribuida: varios clientes, varios servidores

Practica de la asignatura **Sistemas Distribuidos**: evolucion de la version con
sockets de [`clienteServidor`](../clienteServidor/) y [`multClientesUnServidor`](../multClientesUnServidor/)
que enfoca el caso **muchos clientes conectados a la vez contra varios servidores**.

En `multClientesUnServidor` el servidor es uno solo con limite de plazas.
Aqui hay **varios servidores independientes** (3 por defecto), cada uno con su
propio puerto, cola, workers y almacen. Los clientes eligen un servidor al azar
gracias a un balanceador de carga simple.

## De que trata

- **Varios servidores (`server/Servidor`)**: cada uno escucha en su puerto
  (4444, 4445, 4446), acepta conexiones TCP y atiende clientes con hilos
  dedicados y workers consumidores.
- **Balanceador (`client/LoadBalancer`)**: asigna cada cliente a un servidor
  aleatorio; si falla la conexion, prueba con otro.
- **Muchos clientes (`client/Cliente`)**: procesos independientes que se
  lanzan en paralelo (`java Main clientes N` o `make clientes N=...`).
- **Concurrencia real**: cada servidor es un proceso Java separado; los
  clientes se conectan por TCP y envian pedidos.
- **Ids globales**: cada servidor tiene su propio GeneradorIds, asi los ids
  son unicos dentro de cada servidor.
- **Estadisticas finales**: cada servidor resume clientes atendidos, rechazados
  y pedidos procesados.
- Requiere **Java 25+** (el Dockerfile usa Eclipse Temurin 25).

## Estructura

| Ruta | Descripcion |
| --- | --- |
| `src/Main.java` | Launcher: `server`, `client` o `clientes [N]` |
| `src/config/Config.java` | Constantes centralizadas (puertos, limites, tiempos) |
| `src/model/Pedido.java` | Modelo: producto + cliente; id asignado por servidor |
| `src/model/Resultado.java` | Respuesta al cliente (procesado o no) |
| `src/model/GeneradorIds.java` | Singleton que genera ids unicos por servidor |
| `src/cola/ColaPedidos.java` | Buffer circular acotado con Lock + Condition |
| `src/cola/ResultadosPendientes.java` | Futures por pedido para responder al cliente |
| `src/recurso/Almacen.java` | Recurso compartido con Semaphore justo |
| `src/worker/Worker.java` | Hilo consumidor: procesa pedidos y notifica |
| `src/server/Servidor.java` | Servidor independiente: acepta clientes y encola |
| `src/server/RegistroClientes.java` | Limite de clientes simultaneos |
| `src/server/ClienteHandler.java` | Atiende socket: encola pedidos y devuelve resultados |
| `src/client/Cliente.java` | Productor remoto que se conecta por socket |
| `src/client/LoadBalancer.java` | Elige servidor aleatorio para cada cliente |
| `Dockerfile` | Imagen multi-etapa (JDK 25 compila, JRE 25 ejecuta) |
| `compose.yaml` | Tres servicios de servidor con puertos 4444-4446 |
| `Makefile` | Automatiza compilacion, ejecucion y contenedores |

## Patrones de diseño usados

- **Singleton**: GeneradorIds por servidor garantiza ids unicos internos.
- **Handler por conexion**: ClienteHandler aisla red de logica de negocio.
- **Semafaro como aforo**: RegistroClientes limita clientes simultaneos.
- **Futures / Promises**: ResultadosPendientes asocia cada pedido con su futuro.
- **Carga aleatoria**: LoadBalancer distribuye clientes entre servidores.
- **Separacion por capas**: model, cola, recurso, worker, server, client.

## Como ejecutarlo

### Local

En una terminal, arranca los 3 servidores en puertos distintos:

```bash
make compile
java -cp out Main server 1 &
java -cp out Main server 2 &
java -cp out Main server 3 &
```

En otra terminal lanza clientes:

```bash
java -cp out Main clientes 3
# o contra un servidor especifico:
java -cp out Main client ClienteA localhost 4444
```

### Docker

```bash
make up      # levanta los 3 servidores
make logs    # logs del server1
make clientes N=3   # lanza N clientes desde el host
make ps      # estado de los contenedores
make down    # detiene y elimina contenedores
make clean   # down + borra artefactos locales
```

## Salida esperada

Servidor (cada uno independiente):

```
=== TIENDA EN LINEA DISTRIBUIDA (MULTI SERVIDORES) ===

[Server-1] Escuchando en puerto 4444...
[Server-1] Maximo 5 clientes simultaneos
[Server-1] 2 workers activos
[Server-1] Listo. Ctrl+C para detener.

[Server-1] Cliente conectado (1/5 activos)
  [Cliente-50509] envia pedido#2 (Teclado)
  [Worker-1-1] procesa pedido#2 (Teclado) de Cliente-1
  ...
```

Cliente:

```
[Cliente-1] conectado a localhost:4445 (server 2)
  [Cliente-1] envia pedido (Laptop)
  [Cliente-1] pedido#2 procesado (Laptop)
  ...
[Cliente-1] termino
```

## Notas

- Los clientes son procesos separados (JVM distintas): `make clientes` los
  lanza en paralelo.
- Cada servidor es independiente: su propia cola, workers y almacen.
- Si un servidor esta lleno, el cliente puede reintentar con otro.
- El contenedor ejecuta un servidor; para varios, compose levanta 3
  contenedores con puertos mapeados distintos.
