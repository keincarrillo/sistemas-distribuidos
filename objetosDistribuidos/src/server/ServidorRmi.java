package server;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import config.Config;
import recurso.Almacen;
import rmi.TiendaRemota;
import worker.Worker;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Bootstrap de un servidor de objetos distribuidos (RMI):
 * crea el registry, exporta y publica el objeto remoto y gestiona el cierre.
 * Cada proceso/contenedor ejecuta su propio ServidorRmi con su propio registry.
 */
public class ServidorRmi {

    private final int servidorId;
    private final int puerto;
    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;
    private final RegistroLlamadas registro;

    private Registry registry;
    private TiendaImpl tienda;
    private Worker[] workers;

    public ServidorRmi(int servidorId) {
        this.servidorId = servidorId;
        this.puerto = Config.PUERTO_BASE + servidorId - 1;
        this.cola = new ColaPedidos(Config.CAPACIDAD_COLA);
        this.almacen = new Almacen("Almacen", Config.CAPACIDAD_ALMACEN);
        this.resultados = new ResultadosPendientes();
        this.registro = new RegistroLlamadas(Config.MAX_LLAMADAS_SIMULTANEAS);
    }

    public void iniciar() {
        System.out.println("=== TIENDA EN LINEA CON OBJETOS DISTRIBUIDOS (RMI) ===\n");
        System.out.printf("[Servidor-%d] Registry + objeto remoto en puerto %d%n", servidorId, puerto);
        System.out.printf("[Servidor-%d] Maximo %d llamadas remotas simultaneas%n",
                servidorId, Config.MAX_LLAMADAS_SIMULTANEAS);

        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));
        arrancarWorkers();

        try {
            // El hostname se incrusta en los stubs; configurable para Docker/hosts remotos.
            String hostname = System.getenv().getOrDefault("RMI_HOSTNAME", Config.HOSTNAME_DEFECTO);
            System.setProperty("java.rmi.server.hostname", hostname);

            registry = LocateRegistry.createRegistry(puerto);
            tienda = new TiendaImpl(servidorId, cola, resultados, registro);
            TiendaRemota stub = (TiendaRemota) UnicastRemoteObject.exportObject(tienda, puerto);
            registry.rebind(Config.NOMBRE_RMI, stub);

            System.out.printf("[Servidor-%d] Objeto '%s' publicado en rmi://%s:%d/%s%n",
                    servidorId, Config.NOMBRE_RMI, hostname, puerto, Config.NOMBRE_RMI);
            System.out.printf("[Servidor-%d] Listo. Ctrl+C para detener.%n%n", servidorId);

            Thread.currentThread().join();
        } catch (RemoteException e) {
            System.out.printf("[Servidor-%d] No se pudo publicar el objeto: %s%n", servidorId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void arrancarWorkers() {
        workers = new Worker[Config.WORKERS];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-" + servidorId + "-" + (i + 1),
                    cola, almacen, resultados);
            workers[i].start();
        }
        System.out.printf("[Servidor-%d] %d workers activos%n", servidorId, workers.length);
    }

    private void cerrar() {
        try {
            if (registry != null) {
                registry.unbind(Config.NOMBRE_RMI);
            }
        } catch (RemoteException | NotBoundException ignored) {
        }

        if (tienda != null) {
            try {
                UnicastRemoteObject.unexportObject(tienda, true);
            } catch (RemoteException ignored) {
            }
        }
        if (registry != null) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
            } catch (RemoteException ignored) {
            }
        }

        cola.cerrar();
        for (Worker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.printf("%n=== SERVIDOR %d FIN ===%n", servidorId);
        System.out.printf("Workers: %d%n", Config.WORKERS);
        System.out.printf("Pedidos procesados: %d%n", Worker.getProcesados());
        System.out.printf("Llamadas atendidas: %d%n", registro.getAtendidas());
        System.out.printf("Llamadas rechazadas: %d%n", registro.getRechazadas());
        System.out.println("Comunicacion: RMI (objetos distribuidos)");
    }

    public static void main(String[] args) {
        int id = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        new ServidorRmi(id).iniciar();
    }
}