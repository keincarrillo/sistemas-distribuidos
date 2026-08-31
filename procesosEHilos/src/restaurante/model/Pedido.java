package restaurante.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Pedido {
    private static final AtomicInteger CONTADOR = new AtomicInteger(0);

    private final int numero;
    private final Plato plato;
    private final String clienteOrigen;
    private final long timestampCreacion;
    private long timestampInicioCoccion;
    private long timestampFinCoccion;

    public Pedido(Plato plato, String clienteOrigen) {
        this.numero = CONTADOR.incrementAndGet();
        this.plato = plato;
        this.clienteOrigen = clienteOrigen;
        this.timestampCreacion = System.currentTimeMillis();
    }

    public int getNumero() { return numero; }
    public Plato getPlato() { return plato; }
    public String getClienteOrigen() { return clienteOrigen; }
    public long getTimestampCreacion() { return timestampCreacion; }
    public long getTimestampInicioCoccion() { return timestampInicioCoccion; }
    public long getTimestampFinCoccion() { return timestampFinCoccion; }

    public void setTimestampInicioCoccion(long t) { this.timestampInicioCoccion = t; }
    public void setTimestampFinCoccion(long t) { this.timestampFinCoccion = t; }

    public long getTiempoEspera() {
        if (timestampInicioCoccion == 0) return 0;
        return timestampInicioCoccion - timestampCreacion;
    }

    public long getTiempoCoccion() {
        if (timestampFinCoccion == 0 || timestampInicioCoccion == 0) return 0;
        return timestampFinCoccion - timestampInicioCoccion;
    }

    public long getTiempoTotal() {
        if (timestampFinCoccion == 0) return 0;
        return timestampFinCoccion - timestampCreacion;
    }
}
