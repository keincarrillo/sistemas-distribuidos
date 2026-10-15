package com.tienda.worker;

import com.tienda.cola.ColaPedidos;
import com.tienda.cola.ResultadosPendientes;
import com.tienda.config.TiendaProperties;
import com.tienda.model.Pedido;
import com.tienda.recurso.Almacen;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hilo consumidor: toma pedidos de la cola, los procesa (respetando el
 * aforo del almacen) y notifica el resultado a traves del future.
 */
public class Worker extends Thread {

    private static final AtomicInteger procesados = new AtomicInteger();

    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;
    private final TiendaProperties propiedades;

    public Worker(String nombre, ColaPedidos cola, Almacen almacen,
                  ResultadosPendientes resultados, TiendaProperties propiedades) {
        super(nombre);
        this.cola = cola;
        this.almacen = almacen;
        this.resultados = resultados;
        this.propiedades = propiedades;
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
                if (pedido == null) {
                    break;
                }

                // Reclamo atomico: si el cliente cancelo por timeout, lo descarto
                if (!pedido.tomar()) {
                    System.out.printf("  [%s] descarta pedido#%d cancelado por el cliente%n",
                            getName(), pedido.getId());
                    continue;
                }

                System.out.printf("  [%s] procesa pedido#%d (%s) de %s%n",
                        getName(), pedido.getId(), pedido.getProducto(), pedido.getCliente());

                if (almacen.entrar(getName())) {
                    try {
                        Thread.sleep(propiedades.getProcesoWorkerMs());
                    } finally {
                        almacen.salir(getName());
                    }
                }

                total++;
                procesados.incrementAndGet();
                System.out.printf("  [%s] completa pedido#%d%n", getName(), pedido.getId());
                resultados.completar(pedido, true, System.currentTimeMillis());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] termino, proceso %d pedidos%n", getName(), total);
    }
}
