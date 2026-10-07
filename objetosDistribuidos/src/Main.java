import client.Cliente;
import config.Config;
import server.ServidorRmi;

import java.io.File;

/**
 * Punto de entrada unico de la practica.
 * Modos:
 *   java Main server [id]      - inicia un servidor RMI (id=1..3)
 *   java Main client [nombre]  - cliente con balanceo aleatorio
 *   java Main clientes [N]     - lanza N clientes en paralelo (procesos JVM)
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java Main <server|client|clientes> [args]");
            System.out.println("  - server [id]: inicia un servidor RMI (id=1..3)");
            System.out.println("  - client: lanza un cliente (balanceo aleatorio)");
            System.out.println("  - clientes [N]: lanza N clientes a la vez");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> {
                int id = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                ServidorRmi.main(new String[]{String.valueOf(id)});
            }
            case "client" -> lanzarCliente(args);
            case "clientes" -> lanzarClientes(args);
            default -> System.out.println("Comando desconocido: " + args[0]);
        }
    }

    // ------------------------------------------------------------------
    // Cliente individual
    // ------------------------------------------------------------------

    private static void lanzarCliente(String[] args) {
        String nombre = args.length > 1 ? args[1] : "Cliente-remoto";

        try {
            if (args.length > 2) {
                String host = args[2];
                int puerto = args.length > 3 ? Integer.parseInt(args[3]) : Config.PUERTO_BASE;
                System.out.printf("[Main] Cliente %s contra %s:%d%n", nombre, host, puerto);
                new Cliente(nombre, host, puerto).ejecutar();
            } else {
                new Cliente(nombre).ejecutar();
            }
        } catch (Exception e) {
            System.out.println("[Cliente] Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Varios clientes en paralelo (un proceso JVM por cliente)
    // ------------------------------------------------------------------

    private static void lanzarClientes(String[] args) {
        int total = args.length > 1 ? Integer.parseInt(args[1]) : Config.CLIENTES_POR_DEFECTO;
        System.out.printf("[Main] Lanzando %d clientes con balanceo aleatorio%n", total);

        String java = ProcessHandle.current().info().command().orElse("java");
        String classpath = System.getProperty("user.dir") + File.separator + "out";

        Process[] procesos = new Process[total];
        for (int i = 0; i < total; i++) {
            String nombre = "Cliente-" + (i + 1);
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        java, "-cp", classpath, "Main", "client", nombre);
                pb.inheritIO();
                procesos[i] = pb.start();
                System.out.printf("[Main] Lanzado %s (PID %d)%n", nombre, procesos[i].pid());
            } catch (Exception e) {
                System.out.println("[Main] No se pudo lanzar " + nombre + ": " + e.getMessage());
            }
        }

        try {
            for (Process p : procesos) {
                if (p != null) {
                    p.waitFor();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Main] Todos los clientes terminaron");
    }
}