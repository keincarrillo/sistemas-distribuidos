package restaurante;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// Estadisticas compartidas entre hilos (thread-safe)
public class Estadisticas {
    private final AtomicInteger pedidosCreados = new AtomicInteger();
    private final AtomicInteger pedidosCompletados = new AtomicInteger();
    private final AtomicLong tiempoTotalEspera = new AtomicLong();
    private final AtomicLong tiempoTotalCoccion = new AtomicLong();
    private volatile int enMesa = 0;
    private volatile int enResultado = 0;

    public void registrarCreacion() { pedidosCreados.incrementAndGet(); }
    public void registrarCompletado(long espera, long coccion) {
        pedidosCompletados.incrementAndGet();
        tiempoTotalEspera.addAndGet(espera);
        tiempoTotalCoccion.addAndGet(coccion);
    }

    public void setEnMesa(int v) { enMesa = v; }
    public void setEnResultado(int v) { enResultado = v; }

    public int getPedidosCreados() { return pedidosCreados.get(); }
    public int getPedidosCompletados() { return pedidosCompletados.get(); }
    public int getEnMesa() { return enMesa; }
    public int getEnResultado() { return enResultado; }

    public double getTiempoPromedioEspera() {
        int c = pedidosCompletados.get();
        return c == 0 ? 0 : (double) tiempoTotalEspera.get() / c;
    }

    public double getTiempoPromedioCoccion() {
        int c = pedidosCompletados.get();
        return c == 0 ? 0 : (double) tiempoTotalCoccion.get() / c;
    }

    public String toLinea() {
        return String.format("%d|%d|%d|%d|%d|%.0f|%.0f",
                pedidosCreados.get(), pedidosCompletados.get(),
                enMesa, enResultado,
                pedidosCompletados.get(),
                getTiempoPromedioEspera(),
                getTiempoPromedioCoccion());
    }
}
