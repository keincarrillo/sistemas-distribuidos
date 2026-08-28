# procesosEHilos — Descargador de imágenes con hilos

Práctica de la asignatura **Sistemas Distribuidos**: un descargador de imágenes en Java que las obtiene en paralelo usando un pool de hilos fijo.

## De qué trata

- Descarga un conjunto de imágenes desde [picsum.photos](https://picsum.photos) usando el cliente HTTP estándar de Java (`java.net.http`).
- Lanza un hilo por imagen sobre un `ExecutorService` con un límite máximo de hilos concurrentes (`DescargadorService`).
- Recoge los resultados conforme terminan y muestra un resumen: `exitosas/total descargadas en X ms`.
- Requiere **Java 25+**: usa *implicit main* (`void main()`), sintaxis que pasó a ser definitiva en Java 25 (en versiones anteriores era preview).

## Estructura

| Ruta | Descripción |
| --- | --- |
| `src/Main.java` | Punto de entrada: define la lista de imágenes y lanza la descarga |
| `src/model/ImagenDescarga.java` | Modelo: URL y nombre de archivo |
| `src/service/DescargadorService.java` | Pool de hilos y coordinación de las descargas |
| `src/util/HttpUtil.java` | Cliente HTTP y escritura a disco |
| `Dockerfile` | Imagen multi-etapa (JDK 25 compila, JRE 25 ejecuta) |
| `compose.yaml` | Servicio `app` del contenedor |
| `Makefile` | Automatiza build, ejecución y contenedor |

## Requisitos

- JDK 25+ para ejecutar en local.
- Docker + Docker Compose para el contenedor (opcional).

## Cómo ejecutarlo

### En local (sin Docker)

```bash
make compile    # compila a out/classes
make run-local  # compila (si hace falta) y ejecuta
```

### En contenedor

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
200 img1.jpg
200 img2.jpg
200 img3.jpg
200 img4.jpg

4/4 descargadas en 604 ms
```

## Notas

- El programa descarga y termina (no es un servidor): el contenedor queda en estado `Exited (0)`.
- Las imágenes descargadas en el contenedor **viven solo dentro de él** (no hay volúmenes montados) y se pierden con `docker compose down`.
- Al ejecutar en local, las imágenes se guardan en `descargas/` (carpeta gitignoreada).