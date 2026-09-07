package model;

import java.io.Serializable;

/**
 * Pedido creado por un cliente remoto y procesado por un worker del servidor.
 * Es Serializable para poder viajar por el socket.
 *
 * El id NO lo genera el cliente (cada proceso tiene su propio contador); lo
 * asigna el servidor con GeneradorIds para que sea unico en toda la tienda.
 */
public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final String producto;
    private final String cliente;
    private final long timestamp;

    public Pedido(String producto, String cliente) {
        this.id = 0; // el servidor asigna el id global
        this.producto = producto;
        this.cliente = cliente;
        this.timestamp = System.currentTimeMillis();
    }

    /** Asigna el id global del servidor (solo una vez). */
    public void asignarId(int id) {
        if (this.id == 0) {
            this.id = id;
        }
    }

    public int getId() { return id; }
    public String getProducto() { return producto; }
    public String getCliente() { return cliente; }
    public long getTimestamp() { return timestamp; }
}