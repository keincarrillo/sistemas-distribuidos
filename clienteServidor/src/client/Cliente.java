package client;

import config.Config;
import model.Pedido;
import model.Resultado;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Cliente de la tienda distribuida: se conecta al servidor por un socket TCP
 * y actua como productor remoto. Envia una serie de pedidos (productos) y
 * espera la confirmacion de procesamiento de cada uno.
 *
 * Es el equivalente con sockets del hilo Cliente de procesosEHilos: los
 * productores ya no comparten memoria con los consumidores, sino que se
 * comunican a traves de la red.
 */
public class Cliente {
    private final String host;
    private final int puerto;
    private final String nombre;
    private final Random random = new Random();

    public Cliente(String nombre, String host, int puerto) {
        this.nombre = nombre;
        this.host = host;
        this.puerto = puerto;
    }

    public void ejecutar() throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.flush();
            System.out.printf("[%s] conectado a %s:%d%n", nombre, host, puerto);

            for (int i = 0; i < Config.PEDIDOS_POR_CLIENTE; i++) {
                String producto = Config.PRODUCTOS[random.nextInt(Config.PRODUCTOS.length)];
                Pedido pedido = new Pedido(producto, nombre);

                System.out.printf("  [%s] envia pedido (%s)%n",
                        nombre, producto);
                out.writeObject(pedido);
                out.flush();

                // Esperar la confirmacion del servidor
                Resultado resultado = (Resultado) in.readObject();
                if (resultado.isProcesado()) {
                    System.out.printf("  [%s] pedido#%d procesado (%s)%n",
                            nombre, resultado.getPedidoId(), resultado.getProducto());
                }

                try {
                    Thread.sleep(Config.ESPERA_CLIENTE_MIN_MS
                            + random.nextInt((int) Config.ESPERA_CLIENTE_RANGO_MS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.printf("[%s] termino%n", nombre);
        }
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : Config.HOST_DEFECTO;
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : Config.PUERTO;
        String nombre = args.length > 2 ? args[2] : "Cliente-remoto";
        new Cliente(nombre, host, puerto).ejecutar();
    }
}