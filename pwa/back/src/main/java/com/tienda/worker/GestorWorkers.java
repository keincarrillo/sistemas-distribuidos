package com.tienda.worker;

import com.tienda.cola.ColaPedidos;
import com.tienda.cola.ResultadosPendientes;
import com.tienda.config.TiendaProperties;
import com.tienda.recurso.Almacen;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Arranca los workers al subir la aplicacion y los detiene ordenadamente
 * al apagarla (cierre de cola + join de hilos).
 */
@Component
public class GestorWorkers {

    private final TiendaProperties propiedades;
    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;

    private Worker[] workers;

    public GestorWorkers(TiendaProperties propiedades, ColaPedidos cola,
                         Almacen almacen, ResultadosPendientes resultados) {
        this.propiedades = propiedades;
        this.cola = cola;
        this.almacen = almacen;
        this.resultados = resultados;
    }

    @PostConstruct
    public void arrancar() {
        workers = new Worker[propiedades.getWorkers()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-" + (i + 1), cola, almacen, resultados, propiedades);
            workers[i].start();
        }
        System.out.printf("[Tienda] %d workers activos%n", workers.length);
    }

    @PreDestroy
    public void detener() {
        cola.cerrar();
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[Tienda] workers detenidos");
    }
}
