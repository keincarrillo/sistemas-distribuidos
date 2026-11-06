package com.tienda.api;

/**
 * El producto pedido no esta en el catalogo (HTTP 400).
 */
public class ProductoInvalidoException extends RuntimeException {

    public ProductoInvalidoException(String producto) {
        super("Producto no valido: " + producto);
    }
}
