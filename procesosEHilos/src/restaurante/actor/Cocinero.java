package restaurante.actor;

import restaurante.Estadisticas;
import restaurante.buffer.MesaPedidos;
import restaurante.model.Pedido;
import restaurante.recurso.EstacionCocina;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// Consumidor de pedidos + productor de platos cocinados
public class Cocinero extends Thread {
    private final MesaPedidos mesaEntrada;
    private final BlockingQueue<Pedido> mesaResultado;
    private final Map<String, EstacionCocina> estaciones;
    private final AtomicInteger platosCompletados;
    private final Estadisticas stats;

    public Cocinero(String nombre, MesaPedidos mesaEntrada,
                    BlockingQueue<Pedido> mesaResultado,
                    Map<String, EstacionCocina> estaciones,
                    AtomicInteger platosCompletados,
                    Estadisticas stats) {
        super(nombre);
        this.mesaEntrada = mesaEntrada;
        this.mesaResultado = mesaResultado;
        this.estaciones = estaciones;
        this.platosCompletados = platosCompletados;
        this.stats = stats;
    }

    @Override
    public void run() {
        int cocinados = 0;
        try {
            while (true) {
                Pedido pedido = mesaEntrada.tomar();
                if (pedido == null) break;

                System.out.printf("  [%s] Cocina: %s (Pedido #%d)%n",
                        getName(), pedido.getPlato().getNombre(), pedido.getNumero());

                pedido.setTimestampInicioCoccion(System.currentTimeMillis());

                // Adquiere estacion de cocina (semaforo)
                EstacionCocina estacion = estaciones.get(pedido.getPlato().getEstacion());
                if (estacion != null) {
                    estacion.adquirir(getName());
                    try {
                        Thread.sleep(pedido.getPlato().getTiempoCoccionMs());
                    } finally {
                        estacion.liberar(getName());
                    }
                } else {
                    Thread.sleep(pedido.getPlato().getTiempoCoccionMs());
                }

                pedido.setTimestampFinCoccion(System.currentTimeMillis());
                platosCompletados.incrementAndGet();
                stats.registrarCompletado(pedido.getTiempoEspera(), pedido.getTiempoCoccion());
                mesaResultado.put(pedido);
                cocinados++;

                System.out.printf("  [%s] Listo: %s (Pedido #%d)%n",
                        getName(), pedido.getPlato().getNombre(), pedido.getNumero());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] Termino, cocino %d platos%n", getName(), cocinados);
    }
}
