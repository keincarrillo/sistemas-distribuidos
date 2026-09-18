package cola;

import model.Pedido;
import model.Resultado;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResultadosPendientes {
    private final Map<Integer, CompletableFuture<Resultado>> pendientes =
            new ConcurrentHashMap<>();

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
}