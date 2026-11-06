package com.tienda.api;

/**
 * La cola de pedidos esta llena: no se puede encolar ahora (HTTP 503).
 */
public class ColaLlenaException extends RuntimeException {

    public ColaLlenaException(int capacidad) {
        super("Cola de pedidos llena (capacidad " + capacidad + ")");
    }
}
