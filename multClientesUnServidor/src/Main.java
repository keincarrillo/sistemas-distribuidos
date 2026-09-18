import client.Cliente;
import config.Config;
import server.Servidor;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java Main <server|client|clientes> [args]");
            System.out.println("  - server:   inicia el unico servidor de la tienda");
            System.out.println("  - client:   lanza un cliente conectado al servidor");
            System.out.println("  - clientes: lanza N procesos cliente a la vez");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> Servidor.main(new String[0]);
            case "client" -> lanzarCliente(args);
            case "clientes" -> lanzarClientes(args);
            default -> System.out.println("Comando desconocido: " + args[0]);
        }
    }

    private static void lanzarCliente(String[] args) {
        String host = args.length > 1 ? args[1] : Config.HOST_DEFECTO;
        int puerto = args.length > 2 ? Integer.parseInt(args[2]) : Config.PUERTO;
        String nombre = args.length > 3 ? args[3] : "Cliente-remoto";
        try {
            new Cliente(nombre, host, puerto).ejecutar();
        } catch (Exception e) {
            System.out.println("[Cliente] Error: " + e.getMessage());
        }
    }

    // lanza N clientes como procesos java separados (jvm distintas)
    private static void lanzarClientes(String[] args) {
        int total = args.length > 1 ? Integer.parseInt(args[1]) : Config.CLIENTES_POR_DEFECTO;
        String host = args.length > 2 ? args[2] : Config.HOST_DEFECTO;
        int puerto = args.length > 3 ? Integer.parseInt(args[3]) : Config.PUERTO;

        System.out.printf("[Main] Lanzando %d clientes hacia %s:%d%n", total, host, puerto);

        String java = ProcessHandle.current().info().command().orElse("java");
        String classpath = System.getProperty("user.dir") + File.separator + "out";

        Process[] procesos = new Process[total];
        for (int i = 0; i < total; i++) {
            String nombre = "Cliente-" + (i + 1);
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        java, "-cp", classpath, "Main", "client", host,
                        String.valueOf(puerto), nombre);
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