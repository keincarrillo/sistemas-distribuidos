package model;

import java.util.concurrent.atomic.AtomicInteger;

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