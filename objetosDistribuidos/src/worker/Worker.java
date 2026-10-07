package worker;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import config.Config;
import model.Pedido;
import recurso.Almacen;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumidor del pool de workers: toma pedidos de la cola,
 * simula su procesamiento en el almacen y completa el future pendiente.
 */
public class Worker extends Thread {

    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;
    private static final AtomicInteger procesados = new AtomicInteger();

    public Worker(String nombre, ColaPedidos cola, Almacen almacen, ResultadosPendientes resultados) {
        super(nombre);
        this.cola = cola;
        this.almacen = almacen;
        this.resultados = resultados;
    }

    /** Total de pedidos completados por los workers de esta JVM. */
    public static int getProcesados() {
        return procesados.get();
    }

    @Override
    public void run() {
        int total = 0;
        try {
            while (true) {
                Pedido pedido = cola.take();
                if (pedido == null) {
                    break;
                }

                System.out.printf("  [%s] procesa pedido#%d (%s) de %s%n",
                        getName(), pedido.getId(), pedido.getProducto(), pedido.getCliente());

                if (almacen.entrar(getName())) {
                    try {
                        Thread.sleep(Config.PROCESO_WORKER_MS);
                    } finally {
                        almacen.salir(getName());
                    }
                }

                procesados.incrementAndGet();
                total++;
                System.out.printf("  [%s] completa pedido#%d%n", getName(), pedido.getId());

                resultados.completar(pedido, true, System.currentTimeMillis());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] termino, proceso %d pedidos%n", getName(), total);
    }
}
