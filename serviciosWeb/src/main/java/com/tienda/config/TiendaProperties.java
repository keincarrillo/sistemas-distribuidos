package com.tienda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ajustes de la tienda, todos sobreescribibles por variables de entorno
 * (p. ej. TIENDA_WORKERS=4 en Render).
 */
@ConfigurationProperties(prefix = "tienda")
public class TiendaProperties {

    /** Hilos consumidores de pedidos. */
    private int workers = 2;

    /** Capacidad del buffer circular de pedidos. */
    private int capacidadCola = 5;

    /** Aforo maximo del almacen compartido. */
    private int capacidadAlmacen = 2;

    /** Peticiones HTTP simultaneas admitidas (demas -> 503). */
    private int maxPeticionesSimultaneas = 5;

    /** Tiempo que tarda un worker en procesar un pedido (ms). */
    private long procesoWorkerMs = 800;

    /** Espera maxima del resultado antes de responder 504 (ms). */
    private long timeoutResultadoMs = 10_000;

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getCapacidadCola() {
        return capacidadCola;
    }

    public void setCapacidadCola(int capacidadCola) {
        this.capacidadCola = capacidadCola;
    }

    public int getCapacidadAlmacen() {
        return capacidadAlmacen;
    }

    public void setCapacidadAlmacen(int capacidadAlmacen) {
        this.capacidadAlmacen = capacidadAlmacen;
    }

    public int getMaxPeticionesSimultaneas() {
        return maxPeticionesSimultaneas;
    }

    public void setMaxPeticionesSimultaneas(int maxPeticionesSimultaneas) {
        this.maxPeticionesSimultaneas = maxPeticionesSimultaneas;
    }

    public long getProcesoWorkerMs() {
        return procesoWorkerMs;
    }

    public void setProcesoWorkerMs(long procesoWorkerMs) {
        this.procesoWorkerMs = procesoWorkerMs;
    }

    public long getTimeoutResultadoMs() {
        return timeoutResultadoMs;
    }

    public void setTimeoutResultadoMs(long timeoutResultadoMs) {
        this.timeoutResultadoMs = timeoutResultadoMs;
    }
}
