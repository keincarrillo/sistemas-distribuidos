package rmi;

import model.Estadisticas;
import model.Pedido;
import model.Resultado;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato del objeto distribuido (stub/skeleton generados por RMI).
 * Todas las operaciones remotas deben declarar {@link RemoteException}.
 */
public interface TiendaRemota extends Remote {

    /**
     * Envia un pedido y espera (de forma síncrona) a que un worker lo procese.
     *
     * @throws ServidorLlenoException si el servidor ya alcanzo el aforo de llamadas simultaneas
     */
    Resultado hacerPedido(Pedido pedido) throws RemoteException, ServidorLlenoException;

    /** Devuelve las estadísticas acumuladas del servidor. */
    Estadisticas obtenerEstadisticas() throws RemoteException;
}