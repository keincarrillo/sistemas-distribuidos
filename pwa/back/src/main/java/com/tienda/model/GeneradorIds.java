package com.tienda.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton que genera ids unicos dentro de esta instancia del servicio.
 * AtomicInteger garantiza unicidad aunque varios hilos atiendan a la vez.
 */
public final class GeneradorIds {

    private static final GeneradorIds INSTANCIA = new GeneradorIds();
    private final AtomicInteger contador = new AtomicInteger();

    private GeneradorIds() {
    }

    public static GeneradorIds getInstancia() {
        return INSTANCIA;
    }

    public int siguiente() {
        return contador.incrementAndGet();
    }
}
