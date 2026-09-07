import client.Cliente;
import config.Config;
import server.Servidor;

/**
 * Launcher de la practica: arranca el servidor o un cliente.
 *
 * Uso:
 *   java Main server                 -> lanza el servidor (escucha en 4444)
 *   java Main client [host] [puerto] -> lanza un cliente que manda pedidos
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java Main <server|client> [host] [puerto]");
            System.out.println("  - server: inicia el servidor de la tienda");
            System.out.println("  - client: lanza un cliente conectado al servidor");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> Servidor.main(new String[0]);
            case "client" -> lanzarCliente(args);
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
}