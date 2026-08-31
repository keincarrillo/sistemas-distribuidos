import cola.ColaPedidos;
import hilo.Cliente;
import hilo.Worker;
import recurso.Almacen;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.*;

public class Tienda {
    private static final int CLIENTES = 2;
    private static final int WORKERS = 2;
    private static final int CAPACIDAD_COLA = 5;
    private static final int CAPACIDAD_ALMACEN = 2;
    private static final String STATS_FILE = "/tmp/stats.txt";

    public static void ejecutar() {
        System.out.println("=== TIENDA EN LINEA CONCURRENTE ===\n");

        ColaPedidos cola = new ColaPedidos(CAPACIDAD_COLA);
        Almacen almacen = new Almacen("Almacen", CAPACIDAD_ALMACEN);

        // Lanzar proceso monitor (clase separada via ProcessBuilder)
        Process monitor = null;
        try {
            String java = ProcessHandle.current().info().command().orElse("java");
            String classpath = System.getProperty("user.dir") + File.separator + "out";
            ProcessBuilder pb = new ProcessBuilder(
                    java, "-cp", classpath, "proceso.Monitor", STATS_FILE);
            pb.inheritIO();
            monitor = pb.start();
            System.out.printf("[Main] Monitor PID: %d%n", monitor.pid());
        } catch (Exception e) {
            System.out.println("[Main] Monitor no disponible: " + e.getMessage());
        }

        // Hilo que escribe stats para el monitor
        ScheduledExecutorService stats = Executors.newSingleThreadScheduledExecutor();
        stats.scheduleAtFixedRate(() -> {
            try (PrintWriter pw = new PrintWriter(STATS_FILE)) {
                pw.printf("%d|%d|%d|%d%n",
                        Cliente.getContadorPedidos(),
                        Worker.getProcesados(),
                        cola.getCantidad(),
                        CAPACIDAD_ALMACEN - almacen.getDisponibles());
            } catch (Exception ignored) {}
        }, 500, 500, TimeUnit.MILLISECONDS);

        // Lanzar hilos
        Cliente[] clientes = new Cliente[CLIENTES];
        Worker[] workers = new Worker[WORKERS];

        for (int i = 0; i < CLIENTES; i++) {
            clientes[i] = new Cliente("Cliente-" + (i + 1), cola);
        }
        for (int i = 0; i < WORKERS; i++) {
            workers[i] = new Worker("Worker-" + (i + 1), cola, almacen);
        }

        System.out.println();
        for (Cliente c : clientes) c.start();
        for (Worker w : workers) w.start();

        // Esperar a que terminen
        try {
            for (Cliente c : clientes) c.join();
            cola.cerrar();
            for (Worker w : workers) w.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Limpiar
        stats.shutdown();
        if (monitor != null) monitor.destroyForcibly();

        System.out.println("\n=== FIN ===");
        System.out.printf("Hilos: %d clientes + %d workers%n", CLIENTES, WORKERS);
        System.out.println("Proceso separado: monitor");
        System.out.println("Sync: Lock+Conditions (cola), Semaphore (almacen)");
    }
}
