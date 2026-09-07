package server;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import config.Config;
import recurso.Almacen;
import worker.Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servidor de la tienda en linea distribuida.
 *
 * Centraliza la logica del patron productor-consumidor:
 *  - Cola acotada de pedidos + almacen compartido (recursos concurrentes).
 *  - Workers (consumidores) que procesan los pedidos.
 *  - Escucha conexiones TCP; cada cliente remoto es un productor que manda
 *    pedidos por el socket y recibe la confirmacion de procesamiento.
 *
 * Es la version con sockets del ejemplo Tienda de procesosEHilos: lo que alli
 * eran hilos productores compartiendo memoria, aqui son procesos clientes
 * distintos conectados por red.
 */
public class Servidor {
    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;
    private final AtomicInteger clientesActivos = new AtomicInteger();
    private ServerSocket serverSocket;
    private Worker[] workers;

    public Servidor() {
        this.cola = new ColaPedidos(Config.CAPACIDAD_COLA);
        this.almacen = new Almacen("Almacen", Config.CAPACIDAD_ALMACEN);
        this.resultados = new ResultadosPendientes();
    }

    public ColaPedidos getCola() { return cola; }
    public ResultadosPendientes getResultados() { return resultados; }

    public void iniciar() {
        System.out.println("=== TIENDA EN LINEA DISTRIBUIDA (SOCKETS) ===\n");
        System.out.printf("[Servidor] Escuchando en puerto %d...%n", Config.PUERTO);

        // Cierre ordenado con Ctrl+C: para el accept, cierra la cola y espera workers
        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));

        arrancarWorkers();

        try {
            serverSocket = new ServerSocket(Config.PUERTO);
            System.out.println("[Servidor] Listo. Ctrl+C para detener.\n");
            while (true) {
                Socket socket = serverSocket.accept();
                clientesActivos.incrementAndGet();
                System.out.printf("[Servidor] Cliente conectado: %s%n", socket.getInetAddress());
                Thread handler = new Thread(new ClienteHandler(this, socket));
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Server socket cerrado: " + e.getMessage());
        }
    }

    /** Crea y arranca los workers (consumidores) del servidor. */
    private void arrancarWorkers() {
        workers = new Worker[Config.WORKERS];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-" + (i + 1), cola, almacen, resultados);
            workers[i].start();
        }
        System.out.printf("[Servidor] %d workers activos%n", workers.length);
    }

    /** Gestiona la desconexion de un cliente y cierra el canal de accept. */
    void clienteDesconectado(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        int restantes = clientesActivos.decrementAndGet();
        System.out.printf("[Servidor] Cliente desconectado (%d activos)%n", restantes);
    }

    /**
     * Apaga el servidor de forma ordenada: cierra el server socket para salir
     * del accept, cierra la cola para que los workers drenen lo pendiente y
     * espera a que terminen.
     */
    private void cerrar() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }

        cola.cerrar();
        if (workers != null) {
            for (Worker w : workers) {
                try {
                    w.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("\n=== FIN ===");
        System.out.printf("Workers: %d%n", Config.WORKERS);
        System.out.printf("Pedidos procesados: %d%n", Worker.getProcesados());
        System.out.println("Comunicacion: Sockets TCP (productor-consumidor distribuido)");
    }

    public static void main(String[] args) {
        new Servidor().iniciar();
    }
}