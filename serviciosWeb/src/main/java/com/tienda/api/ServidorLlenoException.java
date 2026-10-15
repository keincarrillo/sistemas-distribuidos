package com.tienda.api;

/**
 * Se supero el aforo de peticiones simultaneas (HTTP 503).
 */
public class ServidorLlenoException extends RuntimeException {

    public ServidorLlenoException(int maximo) {
        super("Servidor ocupado: " + maximo + " peticiones simultaneas en curso");
    }
}
