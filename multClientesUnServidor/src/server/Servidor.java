package server;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import config.Config;
import recurso.Almacen;
import worker.Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final ColaPedidos cola;
    private final Almacen almacen;
    private final ResultadosPendientes resultados;
    private final RegistroClientes registro;
    private ServerSocket serverSocket;
    private Worker[] workers;

    public Servidor() {
        this.cola = new ColaPedidos(Config.CAPACIDAD_COLA);
        this.almacen = new Almacen("Almacen", Config.CAPACIDAD_ALMACEN);
        this.resultados = new ResultadosPendientes();
        this.registro = new RegistroClientes(Config.MAX_CLIENTES_SIMULTANEOS);
    }

    public ColaPedidos getCola() { return cola; }
    public ResultadosPendientes getResultados() { return resultados; }

    public void iniciar() {
        System.out.println("=== TIENDA EN LINEA DISTRIBUIDA (MULTI CLIENTES) ===\n");
        System.out.printf("[Servidor] Escuchando en puerto %d...%n", Config.PUERTO);
        System.out.printf("[Servidor] Maximo %d clientes simultaneos%n",
                Config.MAX_CLIENTES_SIMULTANEOS);

        // cierre ordenado con ctrl+c: para el accept, cierra la cola y espera workers
        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));

        arrancarWorkers();

        try {
            serverSocket = new ServerSocket(Config.PUERTO);
            System.out.println("[Servidor] Listo. Ctrl+C para detener.\n");
            while (true) {
                Socket socket = serverSocket.accept();
                if (registro.entrar()) {
                    System.out.printf("[Servidor] Cliente conectado (%d/%d activos)%n",
                            registro.getActivos(), Config.MAX_CLIENTES_SIMULTANEOS);
                    Thread handler = new Thread(new ClienteHandler(this, socket));
                    handler.start();
                } else {
                    // servidor lleno: no se acepta la conexion
                    System.out.printf("[Servidor] Rechaza cliente: limite de %d alcanzado%n",
                            Config.MAX_CLIENTES_SIMULTANEOS);
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Server socket cerrado: " + e.getMessage());
        }
    }

    private void arrancarWorkers() {
        workers = new Worker[Config.WORKERS];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-" + (i + 1), cola, almacen, resultados);
            workers[i].start();
        }
        System.out.printf("[Servidor] %d workers activos%n", workers.length);
    }

    // gestiona la desconexion de un cliente y libera su plaza
    void clienteDesconectado(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        registro.salir();
        System.out.printf("[Servidor] Cliente desconectado (%d/%d activos)%n",
                registro.getActivos(), Config.MAX_CLIENTES_SIMULTANEOS);
    }

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
        System.out.printf("Clientes atendidos: %d%n", registro.getAtendidos());
        System.out.printf("Clientes rechazados: %d%n", registro.getRechazados());
        System.out.println("Comunicacion: Sockets TCP (multi clientes, un servidor)");
    }

    public static void main(String[] args) {
        new Servidor().iniciar();
    }
}