package com.tienda.api;

import com.tienda.cola.ColaPedidos;
import com.tienda.cola.ResultadosPendientes;
import com.tienda.config.TiendaProperties;
import com.tienda.model.GeneradorIds;
import com.tienda.model.Pedido;
import com.tienda.model.Resultado;
import com.tienda.recurso.Almacen;
import com.tienda.server.RegistroClientes;
import com.tienda.worker.Worker;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fachada del nucleo de la tienda: valida, encola y espera el resultado
 * del worker. El controlador solo conoce esta clase.
 */
@Service
public class PedidoService {

    private static final List<String> CATALOGO = List.of("Laptop", "Mouse", "Teclado", "Monitor", "USB");

    private final ColaPedidos cola;
    private final ResultadosPendientes resultados;
    private final RegistroClientes registro;
    private final TiendaProperties propiedades;
    private final Almacen almacen;

    public PedidoService(ColaPedidos cola, ResultadosPendientes resultados,
                         RegistroClientes registro, TiendaProperties propiedades,
                         Almacen almacen) {
        this.cola = cola;
        this.resultados = resultados;
        this.registro = registro;
        this.propiedades = propiedades;
        this.almacen = almacen;
    }

    /**
     * Flujo completo de un pedido: aforo -> id -> cola -> espera del worker.
     */
    public ResultadoResponse enviar(PedidoRequest request) {
        if (request.producto() == null || !CATALOGO.contains(request.producto())) {
            throw new ProductoInvalidoException(request.producto());
        }

        if (!registro.entrar()) {
            throw new ServidorLlenoException(propiedades.getMaxPeticionesSimultaneas());
        }
        try {
            Pedido pedido = new Pedido(request.producto(),
                    request.cliente() == null ? "anonimo" : request.cliente());
            pedido.asignarId(GeneradorIds.getInstancia().siguiente());

            CompletableFuture<Resultado> futuro = resultados.registrar(pedido.getId());
            if (!cola.tryPut(pedido)) {
                resultados.cancelar(pedido.getId());
                throw new ColaLlenaException(cola.getCapacidad());
            }

            System.out.printf("[Servicio] pedido#%d (%s) de %s en cola (%d/%d)%n",
                    pedido.getId(), pedido.getProducto(), pedido.getCliente(),
                    cola.getCantidad(), cola.getCapacidad());

            Resultado resultado;
            try {
                resultado = futuro.get(propiedades.getTimeoutResultadoMs(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // Cancelo el pedido: si aun esta en la cola, el worker lo descartara
                pedido.cancelar();
                resultados.cancelar(pedido.getId());
                throw new ProcesoDemoradoException(propiedades.getTimeoutResultadoMs());
            } catch (ExecutionException e) {
                throw new IllegalStateException("El worker fallo al procesar el pedido", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Espera del resultado interrumpida", e);
            }
            return ResultadoResponse.desde(resultado);
        } finally {
            registro.salir();
        }
    }

    public EstadoResponse estado() {
        return new EstadoResponse(
                "ok",
                cola.getCantidad(),
                cola.getCapacidad(),
                almacen.getDisponibles(),
                almacen.getCapacidad(),
                registro.getActivos(),
                registro.getAtendidos(),
                registro.getRechazados(),
                Worker.getProcesados(),
                propiedades.getWorkers());
    }
}
