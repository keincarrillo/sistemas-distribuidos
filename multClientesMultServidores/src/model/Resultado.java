package model;

import java.io.Serializable;

public class Resultado implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public int getPedidoId() { return pedidoId; }
    public String getProducto() { return producto; }
    public boolean isProcesado() { return procesado; }
    public long getProcesadoEn() { return procesadoEn; }
}
