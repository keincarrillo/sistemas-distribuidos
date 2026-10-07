package client;

import config.Config;
import rmi.TiendaRemota;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Balanceo de carga aleatorio entre los N servidores RMI con reintento:
 * si un registry no responde (servidor caido) o el objeto no esta publicado,
 * se prueba con otro. Es el reintento que en la practica de sockets quedaba
 * pendiente de implementar.
 */
public class BalanceadorRmi {

    private final Random random = new Random();

    /**
     * Resultado de una conexion: el stub del objeto remoto y el id del servidor.
     */
    public static class ServidorRemoto {
        public final TiendaRemota tienda;
        public final int servidorId;
        public final String endpoint;

        ServidorRemoto(TiendaRemota tienda, int servidorId, String endpoint) {
            this.tienda = tienda;
            this.servidorId = servidorId;
            this.endpoint = endpoint;
        }
    }

    /** Conecta al primer servidor disponible (orden aleatorio). */
    public ServidorRemoto siguiente() throws RemoteException {
        List<Integer> orden = new ArrayList<>();
        for (int i = 0; i < Config.NUM_SERVIDORES; i++) {
            orden.add(i);
        }
        Collections.shuffle(orden, random);

        RemoteException ultimoError = null;
        for (int offset : orden) {
            try {
                return conectar(Config.HOST_DEFECTO, Config.PUERTO_BASE + offset);
            } catch (RemoteException e) {
                ultimoError = e;
                System.out.printf("[Balanceador] Servidor %d no disponible, probando otro...%n", offset + 1);
            }
        }
        throw new RemoteException("Ningun servidor RMI disponible", ultimoError);
    }

    /** Conexion directa a un servidor concreto (host:puerto). */
    public ServidorRemoto conectar(String host, int puerto) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host, puerto);
        try {
            TiendaRemota tienda = (TiendaRemota) registry.lookup(Config.NOMBRE_RMI);
            int id = puerto - Config.PUERTO_BASE + 1;
            String endpoint = String.format("rmi://%s:%d/%s", host, puerto, Config.NOMBRE_RMI);
            System.out.printf("[Balanceador] Conectado a servidor %d (%s)%n", id, endpoint);
            return new ServidorRemoto(tienda, id, endpoint);
        } catch (NotBoundException e) {
            throw new RemoteException(
                    String.format("Objeto '%s' no publicado en %s:%d", Config.NOMBRE_RMI, host, puerto), e);
        }
    }
}