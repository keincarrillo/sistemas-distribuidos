import client.Cliente;
import client.LoadBalancer;
import config.Config;
import server.Servidor;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java Main <server|client|clientes> [args]");
            System.out.println("  - server [id]: inicia un servidor (id=1..3)");
            System.out.println("  - client: lanza un cliente contra un servidor aleatorio");
            System.out.println("  - clientes: lanza N clientes a la vez");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> {
                int id = args.length > 1 ? Integer.parseInt(args[1]) : 1;
                Servidor.main(new String[]{String.valueOf(id)});
            }
            case "client" -> lanzarCliente(args);
            case "clientes" -> lanzarClientes(args);
            default -> System.out.println("Comando desconocido: " + args[0]);
        }
    }

    private static void lanzarCliente(String[] args) {
        LoadBalancer lb = new LoadBalancer();
        String host = Config.HOST_DEFECTO;
        int puerto = Config.PUERTO_BASE;
        String nombre = args.length > 1 ? args[1] : "Cliente-remoto";

        if (args.length > 2) {
            host = args[2];
            puerto = args.length > 3 ? Integer.parseInt(args[3]) : puerto;
        }

        if (args.length == 1 || (args.length <= 2 && !args[1].contains(":"))) {
            LoadBalancer.ServidorInfo info = lb.siguiente();
            host = info.host;
            puerto = info.puerto;
            System.out.printf("[Main] Cliente usa servidor %s%n", info);
        }

        try {
            new Cliente(nombre, host, puerto).ejecutar();
        } catch (Exception e) {
            System.out.println("[Cliente] Error: " + e.getMessage());
        }
    }

    private static void lanzarClientes(String[] args) {
        int total = args.length > 1 ? Integer.parseInt(args[1]) : Config.CLIENTES_POR_DEFECTO;
        String host = Config.HOST_DEFECTO;
        int puerto = Config.PUERTO_BASE;

        System.out.printf("[Main] Lanzando %d clientes hacia %s:%d%n",
                total, host, puerto);

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
                if (p != null) p.waitFor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Main] Todos los clientes terminaron");
    }
}
