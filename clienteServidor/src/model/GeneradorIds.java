package model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Genera ids globales unicos de pedido en el servidor.
 *
 * Patron Singleton: solo existe una instancia en la JVM del servidor, asi dos
 * clientes diferentes nunca generan el mismo id de pedido.
 */
public final class GeneradorIds {
    private static final GeneradorIds INSTANCIA = new GeneradorIds();
    private final AtomicInteger contador = new AtomicInteger();

    private GeneradorIds() {
        // Constructor privado: uso exclusivo via getInstancia()
    }

    public static GeneradorIds getInstancia() {
        return INSTANCIA;
    }

    public int siguiente() {
        return contador.incrementAndGet();
    }
}