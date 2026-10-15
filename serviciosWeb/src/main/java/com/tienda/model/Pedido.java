package com.tienda.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pedido enviado por un cliente. El id lo asigna el servidor
 * (o servicio web) al recibirlo, no quien lo envia.
 *
 * Lleva un estado atomico para coordinar al worker y al cliente:
 * el worker debe "tomarlo" antes de procesarlo y el cliente puede
 * cancelarlo (timeout) mientras nadie lo haya tomado.
 */
public class Pedido {

    private static final int ACTIVO = 0;
    private static final int EN_PROCESO = 1;
    private static final int CANCELADO = 2;

    private final AtomicInteger estado = new AtomicInteger(ACTIVO);
    private int id;
    private final String producto;
    private final String cliente;
    private final long timestamp;

    public Pedido(String producto, String cliente) {
        this.id = 0;
        this.producto = producto;
        this.cliente = cliente;
        this.timestamp = System.currentTimeMillis();
    }

    public void asignarId(int id) {
        if (this.id == 0) {
            this.id = id;
        }
    }

    /**
     * El worker intenta tomar el pedido para procesarlo.
     * Devuelve false si el cliente lo cancelo antes.
     */
    public boolean tomar() {
        return estado.compareAndSet(ACTIVO, EN_PROCESO);
    }

    /**
     * El cliente desiste (timeout). Devuelve false si el worker
     * ya habia tomado el pedido y no hay nada que cancelar.
     */
    public boolean cancelar() {
        return estado.compareAndSet(ACTIVO, CANCELADO);
    }

    public boolean estaCancelado() {
        return estado.get() == CANCELADO;
    }

    public int getId() {
        return id;
    }

    public String getProducto() {
        return producto;
    }

    public String getCliente() {
        return cliente;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
