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
    private final int servidorId;
    private ServerSocket serverSocket;
    private Worker[] workers;

    public Servidor(int servidorId, int puerto) {
        this.servidorId = servidorId;
        this.cola = new ColaPedidos(Config.CAPACIDAD_COLA);
        this.almacen = new Almacen("Almacen", Config.CAPACIDAD_ALMACEN);
        this.resultados = new ResultadosPendientes();
        this.registro = new RegistroClientes(Config.MAX_CLIENTES_SIMULTANEOS);
    }

    public int getServidorId() { return servidorId; }

    public ColaPedidos getCola() { return cola; }
    public ResultadosPendientes getResultados() { return resultados; }

    public void iniciar() {
        System.out.println("=== TIENDA EN LINEA DISTRIBUIDA (MULTI SERVIDORES) ===\n");
        System.out.printf("[Servidor-%d] Escuchando en puerto %d...%n",
                servidorId, Config.PUERTO_BASE + servidorId - 1);
        System.out.printf("[Servidor-%d] Maximo %d clientes simultaneos%n",
                servidorId, Config.MAX_CLIENTES_SIMULTANEOS);

        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));

        arrancarWorkers();

        int puerto = Config.PUERTO_BASE + servidorId - 1;
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.printf("[Servidor-%d] Listo. Ctrl+C para detener.%n%n", servidorId);
            while (true) {
                Socket socket = serverSocket.accept();
                if (registro.entrar()) {
                    System.out.printf("[Servidor-%d] Cliente conectado (%d/%d activos)%n",
                            servidorId, registro.getActivos(), Config.MAX_CLIENTES_SIMULTANEOS);
                    Thread handler = new Thread(new ClienteHandler(this, socket));
                    handler.start();
                } else {
                    System.out.printf("[Servidor-%d] Rechaza cliente: limite de %d alcanzado%n",
                            servidorId, Config.MAX_CLIENTES_SIMULTANEOS);
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.printf("[Servidor-%d] Server socket cerrado: %s%n",
                    servidorId, e.getMessage());
        }
    }

    private void arrancarWorkers() {
        workers = new Worker[Config.WORKERS];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker-" + servidorId + "-" + (i + 1),
                    cola, almacen, resultados);
            workers[i].start();
        }
        System.out.printf("[Servidor-%d] %d workers activos%n",
                servidorId, workers.length);
    }

    void clienteDesconectado(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        registro.salir();
        System.out.printf("[Servidor-%d] Cliente desconectado (%d/%d activos)%n",
                servidorId, registro.getActivos(), Config.MAX_CLIENTES_SIMULTANEOS);
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

        System.out.printf("%n=== SERVIDOR %d FIN ===%n", servidorId);
        System.out.printf("Workers: %d%n", Config.WORKERS);
        System.out.printf("Pedidos procesados: %d%n", Worker.getProcesados());
        System.out.printf("Clientes atendidos: %d%n", registro.getAtendidos());
        System.out.printf("Clientes rechazados: %d%n", registro.getRechazados());
        System.out.println("Comunicacion: Sockets TCP (multi clientes, multi servidores)");
    }

    public static void main(String[] args) {
        int id = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        new Servidor(id, Config.PUERTO_BASE + id - 1).iniciar();
    }
}
