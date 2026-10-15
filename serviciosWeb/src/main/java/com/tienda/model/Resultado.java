package com.tienda.model;

/**
 * Respuesta al cliente: el pedido fue procesado (o no) por un worker.
 */
public class Resultado {

    private final int pedidoId;
    private final String producto;
    private final boolean procesado;
    private final long procesadoEn;

    public Resultado(int pedidoId, String producto, boolean procesado, long procesadoEn) {
        this.pedidoId = pedidoId;
        this.producto = producto;
        this.procesado = procesado;
        this.procesadoEn = procesadoEn;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public String getProducto() {
        return producto;
    }

    public boolean isProcesado() {
        return procesado;
    }

    public long getProcesadoEn() {
        return procesadoEn;
    }
}
