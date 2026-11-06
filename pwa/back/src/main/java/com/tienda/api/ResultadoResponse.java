package com.tienda.api;

import com.tienda.model.Resultado;

/**
 * Representacion JSON del resultado de un pedido.
 */
public record ResultadoResponse(int pedidoId, String producto, boolean procesado, long procesadoEn) {

    public static ResultadoResponse desde(Resultado resultado) {
        return new ResultadoResponse(
                resultado.getPedidoId(),
                resultado.getProducto(),
                resultado.isProcesado(),
                resultado.getProcesadoEn());
    }
}
