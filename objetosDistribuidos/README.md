# objetosDistribuidos

Tienda en línea distribuida con **objetos distribuidos (Java RMI)**. Es la evolución de
[`multClientesMultServidores`](../multClientesMultServidores/README.md): en lugar de enviar
`Pedido`/`Resultado` por sockets a mano, el cliente obtiene un **stub** del objeto remoto
`Tienda` y lo invoca **como si fuera local**; RMI se encarga de serializar argumentos y
resultados, del transporte y de la localización vía *registry*.

## Objetivos de la práctica

- Trabajar con el modelo de **objetos distribuidos** (interfaces `Remote`, stubs/skeletons
  generados por RMI, registry).
- Mantener el mismo dominio de las prácticas anteriores: cola acotada productor-consumidor,
  workers y almacén con aforo **dentro de cada servidor**.
- Mantener la topología multi-servidor: **3 servidores independientes** (cada uno con su
  propio registry) y un **balanceador aleatorio con reintento** ante caídas o servidores llenos.
- Reflejar la evolución conceptual del repositorio:
  `procesosEHilos` → `clienteServidor` → `multClientesUnServidor` → `multClientesMultServidores` → **`objetosDistribuidos`**.

## Arquitectura

```
                    ┌────────────────────────────────────────────┐
                    │            Cliente (proceso JVM)            │
                    │   stub TiendaRemota (objeto distribuido)    │
                    └──────────────────┬─────────────────────────┘
                              invocacion remota (RMI)
                    ┌──────────────────▼─────────────────────────┐
                    │            BalanceadorRmi (aleatorio)       │
                    │   retorna el primer registry disponible     │
                    └──────────────────┬─────────────────────────┘
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                              ▼                             ▼
┌───────────────┐            ┌───────────────┐            ┌───────────────┐
│  Servidor RMI │            │  Servidor RMI │            │  Servidor RMI │
│  registry 4444│            │  registry 4445│            │  registry 4446│
│  Tienda   4444│            │  Tienda   4445│            │  Tienda   4446│
│  workers      │            │  workers      │            │  workers      │
│  cola/almacen │            │  cola/almacen │            │  cola/almacen │
└───────────────┘            └───────────────┘            └───────────────┘
```

Cada servidor publica su objeto remoto en su **propio puerto** (`4444`, `4445`, `4446`),
que aloja el *registry* y el objeto a la vez. El *hostname* incrustado en los stubs es
`java.rmi.server.hostname` (configurable con `RMI_HOSTNAME`, por defecto `localhost`).

### Flujo de una llamada

1. El cliente obtiene el stub: `Registry.lookup("Tienda")` (directo o vía balanceador).
2. `tienda.hacerPedido(pedido)`: RMI serializa el `Pedido` y lo entrega al servidor.
3. `TiendaImpl` comprueba el **aforo de llamadas simultáneas** (`RegistroLlamadas`); si está
   lleno lanza `ServidorLlenoException` (viaja intacta al cliente, que reintenta con otro servidor).
4. Asigna id con `GeneradorIds`, registra un `CompletableFuture` y encola el pedido.
5. Un `Worker` toma el pedido, procesa en el `Almacen` (semáforo con aforo) y completa el future.
6. `TiendaImpl` devuelve el `Resultado` como **valor de retorno** de la llamada remota.

### Patrones de diseño

| Patrón | Dónde |
|---|---|
| Proxy / Stub | stubs RMI generados automáticamente (`TiendaRemota`) |
| Remote Facade | `server/TiendaImpl` (fachada remota sobre cola/workers) |
| Singleton | `model/GeneradorIds` (ids únicos por JVM/servidor) |
| Productor-Consumidor | `cola/ColaPedidos` (buffer circular acotado, `Lock` + `Condition`) |
| Future / Promise | `cola/ResultadosPendientes` (`ConcurrentHashMap` + `CompletableFuture`) |
| Load Balancer | `client/BalanceadorRmi` (aleatorio con reintento ante caída) |
| Semaphore Limiting | `server/RegistroLlamadas` y `recurso/Almacen` |
| DTO serializable | `model/Pedido`, `model/Resultado`, `model/Estadisticas` |

## Estructura de carpetas

```
src/
├── Main.java                CLI: server | client | clientes
├── config/Config.java       parametrizacion centralizada
├── model/                   Pedido, Resultado, Estadisticas, GeneradorIds (Singleton)
├── cola/                    ColaPedidos (acotada), ResultadosPendientes (futures)
├── recurso/Almacen.java     recurso compartido con semaforo
├── worker/Worker.java       pool de consumidores
├── rmi/                     TiendaRemota (interface Remote), ServidorLlenoException
├── server/                  ServidorRmi (registry + export), TiendaImpl (Remote Facade),
│                            RegistroLlamadas (aforo)
└── client/                  Cliente (invocacion remota), BalanceadorRmi (aleatorio + reintento)
```

## Requisitos

- Java 25+ (se compila con `javac --release 25`; probado con JDK 25/26).
- Make.
- Docker + Docker Compose (opcional, para la topología con contenedores).

## Uso

### Compilar

```bash
make compile    # genera las clases en out/
```

### En local (sin Docker)

```bash
make servers       # lanza los 3 servidores RMI en background (logs-server-*.txt)
make clientes      # lanza 3 clientes (N=<k> para mas; balanceo aleatorio)
make cliente NOMBRE=Ana            # cliente contra un servidor aleatorio
make cliente NOMBRE=Ana PUERTO=4445 # cliente contra un servidor concreto
make stop-servers  # detiene los servidores locales
```

### Con Docker

```bash
make up        # construye y levanta los 3 servidores (4444, 4445, 4446)
make ps        # estado de los contenedores
make logs      # sigue los logs de server1
make clientes  # lanza 3 clientes desde el host contra los contenedores
make down      # detiene los contenedores
make clean     # down + borra out/ y artefactos locales
```

## Comportamiento observable

- **Balanceo**: cada cliente se conecta a un servidor elegido al azar; con varios clientes
  los pedidos se reparten entre los servidores disponibles.
- **Tolerancia a fallos**: si un servidor está caído o lleno, el balanceador prueba con otro
  (hasta `MAX_INTENTOS`); también funciona a mitad de sesión.
- **Aforo**: cada servidor rechaza llamadas remotas cuando se superan
  `MAX_LLAMADAS_SIMULTANEAS` en vuelo (`ServidorLlenoException`), contando atendidas/rechazadas.
- **Estadísticas**: el método remoto `obtenerEstadisticas()` devuelve un `Estadisticas` con los
  pedidos procesados, llamadas atendidas/rechazadas, activas y pedidos en cola.