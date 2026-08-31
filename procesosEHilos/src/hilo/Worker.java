package hilo;

import cola.ColaPedidos;
import model.Pedido;
import recurso.Almacen;

import java.util.concurrent.atomic.AtomicInteger;

public class Worker extends Thread {
    private final ColaPedidos cola;
    private final Almacen almacen;
    private static final AtomicInteger procesados = new AtomicInteger();

    public Worker(String nombre, ColaPedidos cola, Almacen almacen) {
        super(nombre);
        this.cola = cola;
        this.almacen = almacen;
    }

    public static int getProcesados() {
        return procesados.get();
    }

    @Override
    public void run() {
        int total = 0;
        try {
            while (true) {
                Pedido pedido = cola.take();
                if (pedido == null) break;

                System.out.printf("  [%s] procesa pedido#%d (%s)%n",
                        getName(), pedido.getId(), pedido.getProducto());

                if (almacen.entrar(getName())) {
                    try {
                        Thread.sleep(800);
                    } finally {
                        almacen.salir(getName());
                    }
                }

                procesados.incrementAndGet();
                total++;
                System.out.printf("  [%s] completa pedido#%d%n",
                        getName(), pedido.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] termino, proceso %d pedidos%n", getName(), total);
    }
}
