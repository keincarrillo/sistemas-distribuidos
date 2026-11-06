package com.tienda.cola;

import com.tienda.model.Pedido;
import com.tienda.model.Resultado;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Futures por pedido: asocia cada id con su CompletableFuture para que
 * el hilo que atiende la peticion HTTP espere el resultado del worker.
 */
public class ResultadosPendientes {

    private final Map<Integer, CompletableFuture<Resultado>> pendientes = new ConcurrentHashMap<>();

    public CompletableFuture<Resultado> registrar(int pedidoId) {
        CompletableFuture<Resultado> future = new CompletableFuture<>();
        pendientes.put(pedidoId, future);
        return future;
    }

    public void completar(Pedido pedido, boolean ok, long procesadoEn) {
        CompletableFuture<Resultado> future = pendientes.remove(pedido.getId());
        if (future != null) {
            future.complete(new Resultado(pedido.getId(), pedido.getProducto(), ok, procesadoEn));
        }
    }

    /**
     * Elimina el future sin completarlo, por ejemplo si el cliente
     * desistio o el timeout vencio.
     */
    public void cancelar(int pedidoId) {
        pendientes.remove(pedidoId);
    }
}
