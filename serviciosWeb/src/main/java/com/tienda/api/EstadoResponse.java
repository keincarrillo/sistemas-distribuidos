package com.tienda.api;

/**
 * Estado interno del servicio para monitoreo.
 */
public record EstadoResponse(
        String estado,
        int colaActual,
        int capacidadCola,
        int almacenDisponible,
        int capacidadAlmacen,
        int peticionesActivas,
        int peticionesAtendidas,
        int peticionesRechazadas,
        int pedidosProcesados,
        int workers) {
}
