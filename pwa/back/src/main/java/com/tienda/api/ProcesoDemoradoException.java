package com.tienda.api;

/**
 * El worker no completo el pedido a tiempo (HTTP 504).
 */
public class ProcesoDemoradoException extends RuntimeException {

    public ProcesoDemoradoException(long timeoutMs) {
        super("El proceso del pedido excedio el tiempo maximo de " + timeoutMs + " ms");
    }
}
