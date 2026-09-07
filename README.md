# sistemas-distribuidos

Repositorio de prácticas de la asignatura **Sistemas Distribuidos**.

## Prácticas

### [procesosEHilos](procesosEHilos/README.md)

Tienda en línea concurrente en Java con hilos: patrón productor-consumidor con cola acotada
(`Lock` + `Condition`), recurso compartido con semáforo y un proceso monitor separado. Incluye
el código fuente, contenedor docker y Makefile.

### [clienteServidor](clienteServidor/README.md)

Versión distribuida de la tienda en línea con **sockets TCP**: los clientes son procesos remotos
que envían pedidos por la red a un servidor, que los encola en una cola acotada y los procesa con
workers, devolviendo la confirmación por el mismo socket. Igual que la anterior, incluye el código
fuente, contenedor docker y Makefile.
