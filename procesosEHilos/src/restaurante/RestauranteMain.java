package restaurante;

import restaurante.actor.Cliente;
import restaurante.actor.Cocinero;
import restaurante.actor.Mesero;
import restaurante.buffer.MesaPedidos;
import restaurante.model.Pedido;
import restaurante.recurso.EstacionCocina;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RestauranteMain {

    private static final int NUM_CLIENTES = 4;
    private static final int NUM_COCINEROS = 2;
    private static final int NUM_MESEROS = 1;
    private static final int CAPACIDAD_MESA = 5;
    private static final String STATS_FILE = "/tmp/restaurante_stats.txt";

    public static void main(String[] args) {
        // Limpiar archivo de stats anterior
        new File(STATS_FILE).delete();

        System.out.println();
        System.out.println("========================================");
        System.out.println("  COCINA CONCURRENTE - RESTAURANTE SD");
        System.out.println("========================================");
        System.out.println();
        System.out.println("  Hilos:        " + (NUM_CLIENTES + NUM_COCINEROS + NUM_MESEROS));
        System.out.println("  Productores:  " + NUM_CLIENTES + " clientes");
        System.out.println("  Cocineros:    " + NUM_COCINEROS);
        System.out.println("  Consumidores: " + NUM_MESEROS + " mesero(s)");
        System.out.println("  Buffer:       " + CAPACIDAD_MESA + " pedidos");
        System.out.println();

        // --- Semaforos: estaciones de cocina compartidas ---
        Map<String, EstacionCocina> estaciones = Map.of(
                "Estufa", new EstacionCocina("Estufa", 2),
                "Horno", new EstacionCocina("Horno", 1),
                "Parrilla", new EstacionCocina("Parrilla", 1),
                "Freidora", new EstacionCocina("Freidora", 1)
        );

        System.out.println("  Estaciones de cocina (semaforos):");
        estaciones.forEach((k, v) ->
                System.out.printf("    - %s x%d permisos%n", k, v.getPermisosDisponibles()));
        System.out.println();

        // --- Buffers compartidos ---
        MesaPedidos mesaPedidos = new MesaPedidos(CAPACIDAD_MESA);
        BlockingQueue<Pedido> mesaResultado = new LinkedBlockingQueue<>();

        // --- Estadisticas thread-safe ---
        AtomicInteger platosCompletados = new AtomicInteger();
        AtomicInteger platosServidos = new AtomicInteger();
        Estadisticas stats = new Estadisticas();

        // --- Crear hilos ---
        List<Cliente> clientes = java.util.stream.IntStream.range(0, NUM_CLIENTES)
                .mapToObj(i -> new Cliente("Cliente-" + (i + 1), mesaPedidos, stats))
                .toList();

        List<Cocinero> cocineros = java.util.stream.IntStream.range(0, NUM_COCINEROS)
                .mapToObj(i -> new Cocinero("Cocinero-" + (i + 1), mesaPedidos,
                        mesaResultado, estaciones, platosCompletados, stats))
                .toList();

        List<Mesero> meseros = java.util.stream.IntStream.range(0, NUM_MESEROS)
                .mapToObj(i -> new Mesero("Mesero-" + (i + 1), mesaResultado, platosServidos))
                .toList();

        // --- Lanzar proceso monitor separado (demonstracion de procesos OS) ---
        Process monitorProcess = null;
        try {
            String javaBin = ProcessHandle.current().info().command().orElse("java");
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin, "-cp", "out", "restaurante.proceso.MonitorProceso", STATS_FILE
            );
            pb.inheritIO();
            monitorProcess = pb.start();
            System.out.println("  [Main] Monitor PID: " + monitorProcess.pid());
        } catch (Exception e) {
            System.out.println("  [Main] Monitor no disponible: " + e.getMessage());
        }

        // --- Hilo que escribe stats para el monitor ---
        ScheduledExecutorService statsScheduler = Executors.newSingleThreadScheduledExecutor();
        statsScheduler.scheduleAtFixedRate(() -> {
            stats.setEnMesa(mesaPedidos.tamanio());
            stats.setEnResultado(mesaResultado.size());
            try (PrintWriter pw = new PrintWriter(STATS_FILE)) {
                pw.println(stats.toLinea());
            } catch (Exception ignored) {}
        }, 500, 500, TimeUnit.MILLISECONDS);

        // --- Lanzar todos los hilos ---
        long inicio = System.currentTimeMillis();
        System.out.println("\n  === INICIO SIMULACION ===\n");

        clientes.forEach(Thread::start);
        cocineros.forEach(Thread::start);
        meseros.forEach(Thread::start);

        try {
            // 1. Esperar a que clientes terminen
            for (Cliente c : clientes) c.join();
            System.out.println("\n  --- Clientes terminaron ---");

            // 2. Cerrar mesa (senal para cocineros)
            mesaPedidos.cerrar();
            System.out.println("  --- Mesa cerrada ---");

            // 3. Esperar cocineros
            for (Cocinero co : cocineros) co.join();
            System.out.println("  --- Cocineros terminaron ---");

            // 4. Detener meseros
            for (Mesero m : meseros) m.detener();
            for (Mesero m : meseros) m.join();
            System.out.println("  --- Meseros terminaron ---");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duracion = System.currentTimeMillis() - inicio;

        // --- Limpiar ---
        statsScheduler.shutdown();
        if (monitorProcess != null) {
            monitorProcess.destroyForcibly();
        }

        // --- Resumen ---
        System.out.println();
        System.out.println("========================================");
        System.out.println("  RESUMEN");
        System.out.println("========================================");
        System.out.println("  Pedidos creados:     " + stats.getPedidosCreados());
        System.out.println("  Platos completados:  " + stats.getPedidosCompletados());
        System.out.println("  Platos servidos:     " + platosServidos.get());
        System.out.printf("  Tiempo espera prom:  %.0f ms%n", stats.getTiempoPromedioEspera());
        System.out.printf("  Tiempo coccion prom: %.0f ms%n", stats.getTiempoPromedioCoccion());
        System.out.println("  Duracion total:      " + duracion + " ms");
        System.out.println("  Hilos totales:       " + (NUM_CLIENTES + NUM_COCINEROS + NUM_MESEROS));
        System.out.println("  Procesos OS:         1 monitor + 1 principal");
        System.out.println();
        System.out.println("  Sincronizacion usada:");
        System.out.println("    - ReentrantLock + Conditions (buffer productor-consumidor)");
        System.out.println("    - Semaphore (estaciones de cocina)");
        System.out.println("    - AtomicInteger (estadisticas thread-safe)");
        System.out.println("    - BlockingQueue (mesa de resultado)");
        System.out.println("    - Thread.join / Thread.interrupt (coordinacion)");
        System.out.println("    - ProcessBuilder (proceso monitor separado)");
        System.out.println("========================================");
        System.out.println();
    }
}
