package com.tienda.api;

/**
 * El cliente no puede indicar el id: lo asigna el servicio.
 * La unica restriccion de negocio es que el producto este en el catalogo.
 */
public record PedidoRequest(String producto, String cliente) {
}
