# Pruebas de la API — serviciosWeb

Evidencia de pruebas de la tienda en línea desplegada como servicio web REST en
**Render** y verificada contra la URL pública con `curl`.

- **Servicio**: <https://servicioweb-5uia.onrender.com>
- **Runtime**: Docker (imagen multi-etapa, Eclipse Temurin 25)
- **Configuración por defecto**: 2 workers, cola de 5, almacén con aforo de 2,
  máximo 5 peticiones simultáneas, timeout de resultado 10 000 ms.
- **Fecha de las pruebas**: 14 de octubre de 2026

## 1. Health check

Comprueba que el servicio está vivo (lo usa Render como health check).

```bash
curl https://servicioweb-5uia.onrender.com/health
```

| Resultado | Código |
| --- | --- |
| `{"status":"UP"}` | `200` |

## 2. Envío de pedido válido

El producto está en el catálogo (`Laptop`, `Mouse`, `Teclado`, `Monitor`, `USB`).
El servicio asigna el id, encola el pedido y devuelve el resultado cuando un worker
lo procesa (pasa por el almacén).

```bash
curl -X POST https://servicioweb-5uia.onrender.com/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Laptop","cliente":"Ana"}'
```

| Resultado | Código |
| --- | --- |
| `{"pedidoId":1,"producto":"Laptop","procesado":true,"procesadoEn":1792030799183}` | `200` |

## 3. Pedido sin cliente

El campo `cliente` es opcional: si falta, el servicio lo registra como `anonimo`.

```bash
curl -X POST https://servicioweb-5uia.onrender.com/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Mouse"}'
```

| Resultado | Código |
| --- | --- |
| `{"pedidoId":2,"producto":"Mouse","procesado":true,"procesadoEn":1792030800576}` | `200` |

## 4. Producto fuera del catálogo

La validación de negocio rechaza productos desconocidos antes de encolar.

```bash
curl -X POST https://servicioweb-5uia.onrender.com/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"producto":"Patinete","cliente":"Ana"}'
```

| Resultado | Código |
| --- | --- |
| `{"error":"Producto no valido: Patinete"}` | `400` |

## 5. Cuerpo vacío

```bash
curl -X POST https://servicioweb-5uia.onrender.com/api/pedidos \
  -H "Content-Type: application/json" -d '{}'
```

| Resultado | Código |
| --- | --- |
| `{"error":"Producto no valido: null"}` | `400` |

## 6. Estado del servicio

Expone las estadísticas internas: cola, almacén, peticiones atendidas y
rechazadas, pedidos procesados y workers activos.

```bash
curl https://servicioweb-5uia.onrender.com/api/estado
```

Resultado tras las pruebas 2 y 3:

```json
{
  "estado": "ok",
  "colaActual": 0, "capacidadCola": 5,
  "almacenDisponible": 2, "capacidadAlmacen": 2,
  "peticionesActivas": 0, "peticionesAtendidas": 2, "peticionesRechazadas": 0,
  "pedidosProcesados": 2, "workers": 2
}
```

## 7. Aforo de peticiones simultáneas (503)

Ráfaga de **12 peticiones simultáneas** contra un aforo de **5**. Como el aforo es
un semáforo justo, se atienden 5 a la vez y el resto se rechaza al instante con
`503` (no hay espera encolada para el cliente).

```bash
for i in $(seq 1 12); do
  curl -s -o /dev/null -w "%{http_code} " -X POST https://servicioweb-5uia.onrender.com/api/pedidos \
    -H "Content-Type: application/json" -d "{\"producto\":\"Teclado\",\"cliente\":\"R$i\"}" &
done; wait
```

Salida (5 × `200`, 7 × `503`):

```
503 503 503 503 503 503 503 200 200 200 200 200
```

Estado después de la ráfaga (7 atendidas + 7 rechazadas acumuladas; las 2
primeras son de las pruebas 2 y 3):

```json
{
  "estado": "ok",
  "colaActual": 0, "capacidadCola": 5,
  "almacenDisponible": 2, "capacidadAlmacen": 2,
  "peticionesActivas": 0, "peticionesAtendidas": 7, "peticionesRechazadas": 7,
  "pedidosProcesados": 7, "workers": 2
}
```

## 8. Timeout del worker (504) y cancelación del pedido

Si un worker no completa el pedido dentro del tiempo máximo
(`TIENDA_TIMEOUT_RESULTADO_MS`, 10 s por defecto), el cliente recibe `504`:

```json
{"error":"El proceso del pedido excedio el tiempo maximo de 1000 ms"}
```

Con la configuración desplegada el `504` no es provocable desde fuera (los workers
procesan en 800 ms y el aforo limita la entrada), así que se verificó en local
acelerando los tiempos:

```bash
TIENDA_PROCESO_WORKER_MS=2000 TIENDA_TIMEOUT_RESULTADO_MS=1000 ./mvnw spring-boot:run
```

Ráfaga de 4 pedidos con 2 workers: 2 quedan en la cola y expiran. El servicio
**cancela el pedido** (estado atómico `ACTIVO → CANCELADO`) y el worker lo
descarta en vez de procesarlo:

```
[Worker-1] descarta pedido#2 cancelado por el cliente
[Worker-2] descarta pedido#3 cancelado por el cliente
```

`pedidosProcesados` solo cuenta los realmente procesados, no los descartados.

## Resumen de códigos

| Endpoint | Método | Código | Cuándo |
| --- | --- | --- | --- |
| `/health` | GET | `200` | Servicio vivo |
| `/api/pedidos` | POST | `200` | Pedido procesado por un worker |
| `/api/pedidos` | POST | `400` | Producto inválido o ausente |
| `/api/pedidos` | POST | `503` | Cola llena o aforo agotado |
| `/api/pedidos` | POST | `504` | Worker no completó a tiempo |
| `/api/estado` | GET | `200` | Estadísticas internas |

## Notas

- El estado (cola, contadores, almacén) vive en memoria: se reinicia con cada
  deploy o reinicio; los ids de pedido son únicos por instancia.
- En el plan gratuito de Render el servicio se duerme tras inactividad: la primera
  petición tarda más en despertarlo.
- El aforo y las capacidades se ajustan por variable de entorno:
  `TIENDA_WORKERS`, `TIENDA_CAPACIDAD_COLA`, `TIENDA_CAPACIDAD_ALMACEN`,
  `TIENDA_MAX_PETICIONES_SIMULTANEAS`, `TIENDA_TIMEOUT_RESULTADO_MS`.
