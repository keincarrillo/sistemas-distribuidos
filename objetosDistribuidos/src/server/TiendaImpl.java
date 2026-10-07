package server;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import model.Estadisticas;
import model.GeneradorIds;
import model.Pedido;
import model.Resultado;
import rmi.ServidorLlenoException;
import rmi.TiendaRemota;
import worker.Worker;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

/**
 * Implementacion del objeto distribuido (Remote Facade): expone la tienda
 * como un unico objeto remoto que orquesta la cola, los futures y el aforo.
 * Sustituye al ClienteHandler de la practica de sockets: RMI se encarga del
 * transporte y aqui solo queda la logica de cada llamada remota.
 */
public class TiendaImpl implements TiendaRemota {

    private final int servidorId;
    private final ColaPedidos cola;
    private final ResultadosPendientes resultados;
    private final RegistroLlamadas registro;

    public TiendaImpl(int servidorId, ColaPedidos cola,
                      ResultadosPendientes resultados, RegistroLlamadas registro) {
        this.servidorId = servidorId;
        this.cola = cola;
        this.resultados = resultados;
        this.registro = registro;
    }

    @Override
    public Resultado hacerPedido(Pedido pedido) throws RemoteException, ServidorLlenoException {
        if (!registro.entrar()) {
            throw new ServidorLlenoException(String.format(
                    "Servidor-%d al limite (%d llamadas simultaneas)",
                    servidorId, registro.getActivas()));
        }
        try {
            pedido.asignarId(GeneradorIds.getInstancia().siguiente());
            System.out.printf("  [Server-%d] recibe pedido#%d (%s) de %s%n",
                    servidorId, pedido.getId(), pedido.getProducto(), pedido.getCliente());

            CompletableFuture<Resultado> futuro = resultados.registrar(pedido.getId());
            try {
                cola.put(pedido);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RemoteException("cola interrumpida", e);
            }
            return futuro.join();
        } finally {
            registro.salir();
        }
    }

    @Override
    public Estadisticas obtenerEstadisticas() throws RemoteException {
        return new Estadisticas(servidorId,
                Worker.getProcesados(),
                registro.getAtendidas(),
                registro.getRechazadas(),
                registro.getActivas(),
                cola.getCantidad());
    }
}