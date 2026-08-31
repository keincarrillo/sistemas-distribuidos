package restaurante.actor;

import restaurante.model.Pedido;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Consumidor: toma platos cocinados y los sirve
// Usa take() con timeout largo, se detiene con interrupcion
public class Mesero extends Thread {
    private final BlockingQueue<Pedido> mesaResultado;
    private final AtomicInteger servidos;
    private volatile boolean activo = true;

    public Mesero(String nombre, BlockingQueue<Pedido> mesaResultado,
                  AtomicInteger servidos) {
        super(nombre);
        this.mesaResultado = mesaResultado;
        this.servidos = servidos;
    }

    public void detener() {
        activo = false;
        interrupt();
    }

    @Override
    public void run() {
        int totalServidos = 0;
        try {
            while (activo) {
                Pedido pedido = mesaResultado.take();

                System.out.printf("  [%s] Sirve: %s #%d para %s (espera %dms, total %dms)%n",
                        getName(),
                        pedido.getPlato().getNombre(),
                        pedido.getNumero(),
                        pedido.getClienteOrigen(),
                        pedido.getTiempoEspera(),
                        pedido.getTiempoTotal());

                servidos.incrementAndGet();
                totalServidos++;
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] Termino, sirvio %d platos%n", getName(), totalServidos);
    }
}
