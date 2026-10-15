package com.tienda.model;

/**
 * Pedido enviado por un cliente. El id lo asigna el servidor
 * (o servicio web) al recibirlo, no quien lo envia.
 */
public class Pedido {

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
