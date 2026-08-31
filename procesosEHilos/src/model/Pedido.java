package model;

public class Pedido {
    private static int contador = 0;

    private final int id;
    private final String producto;
    private final long timestamp;

    public Pedido(String producto) {
        this.id = ++contador;
        this.producto = producto;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public String getProducto() { return producto; }
    public long getTimestamp() { return timestamp; }
}
