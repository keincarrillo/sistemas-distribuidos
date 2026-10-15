package com.tienda.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exponen la tienda como servicio web REST.
 */
@RestController
@RequestMapping("/api")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    /**
     * POST /api/pedidos — envia un pedido y devuelve su resultado.
     */
    @PostMapping("/pedidos")
    public ResultadoResponse enviar(@RequestBody PedidoRequest request) {
        return servicio.enviar(request);
    }

    /**
     * GET /api/estado — estadisticas internas del servicio.
     */
    @GetMapping("/estado")
    public EstadoResponse estado() {
        return servicio.estado();
    }
}
