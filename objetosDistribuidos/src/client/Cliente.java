package client;

import config.Config;
import model.Estadisticas;
import model.Pedido;
import model.Resultado;
import rmi.ServidorLlenoException;
import rmi.TiendaRemota;

import java.rmi.RemoteException;
import java.util.Random;

/**
 * Cliente de objetos distribuidos: obtiene el stub remoto (directo o via
 * balanceador aleatorio) e invoca metodos sobre el objeto Tienda
 * como si fuera local. Los pedidos viajan como argumentos serializados
 * por RMI y las respuestas vuelven como valores de retorno.
 */
public class Cliente {

    private final String nombre;
    private final String hostFijo;
    private final int puertoFijo; // 0 => balanceo aleatorio entre servidores
    private final Random random = new Random();
    private final BalanceadorRmi balanceador = new BalanceadorRmi();

    private TiendaRemota tienda;
    private int servidorActual;

    public Cliente(String nombre) {
        this(nombre, null, 0);
    }

    public Cliente(String nombre, String host, int puerto) {
        this.nombre = nombre;
        this.hostFijo = host;
        this.puertoFijo = puerto;
    }

    public void ejecutar() throws RemoteException {
        conectar();

        for (int i = 0; i < Config.PEDIDOS_POR_CLIENTE; i++) {
            String producto = Config.PRODUCTOS[random.nextInt(Config.PRODUCTOS.length)];
            Pedido pedido = new Pedido(producto, nombre);
            System.out.printf("  [%s] envia pedido (%s) al servidor %d%n",
                    nombre, producto, servidorActual);

            Resultado resultado = enviarConReintento(pedido);
            System.out.printf("  [%s] pedido#%d procesado (%s) por servidor %d%n",
                    nombre, resultado.getPedidoId(), resultado.getProducto(), servidorActual);

            pausa(Config.ESPERA_CLIENTE_MIN_MS + random.nextInt((int) Config.ESPERA_CLIENTE_RANGO_MS));
        }

        Estadisticas stats = tienda.obtenerEstadisticas();
        System.out.printf("[%s] Estadisticas: %s%n", nombre, stats);
        System.out.printf("[%s] termino%n", nombre);
    }

    // ------------------------------------------------------------------
    // Conexion
    // ------------------------------------------------------------------

    private void conectar() throws RemoteException {
        if (puertoFijo > 0) {
            conectarDirecto(hostFijo, puertoFijo);
        } else {
            elegirServidor();
        }
        System.out.printf("[%s] conectado al servidor %d%n", nombre, servidorActual);
    }

    private void elegirServidor() throws RemoteException {
        BalanceadorRmi.ServidorRemoto sr = balanceador.siguiente();
        tienda = sr.tienda;
        servidorActual = sr.servidorId;
    }

    private void conectarDirecto(String host, int puerto) throws RemoteException {
        BalanceadorRmi.ServidorRemoto sr = balanceador.conectar(host, puerto);
        tienda = sr.tienda;
        servidorActual = sr.servidorId;
    }

    // ------------------------------------------------------------------
    // Invocacion remota con reintento ante servidor lleno o caido
    // ------------------------------------------------------------------

    private Resultado enviarConReintento(Pedido pedido) throws RemoteException {
        for (int intento = 0; intento < Config.MAX_INTENTOS; intento++) {
            try {
                return tienda.hacerPedido(pedido);
            } catch (ServidorLlenoException e) {
                System.out.printf("  [%s] %s%n", nombre, e.getMessage());
            } catch (RemoteException e) {
                System.out.printf("  [%s] servidor %d caido: %s%n", nombre, servidorActual, e.getMessage());
            }

            if (puertoFijo > 0) {
                throw new RemoteException("El servidor " + servidorActual + " no proceso el pedido");
            }
            pausa(Config.ESPERA_REINTENTO_MS);
            elegirServidor();
            System.out.printf("  [%s] reintenta con el servidor %d%n", nombre, servidorActual);
        }
        throw new RemoteException("No se pudo completar el pedido tras " + Config.MAX_INTENTOS + " intentos");
    }

    private static void pausa(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws RemoteException {
        String nombre = args.length > 0 ? args[0] : "Cliente-remoto";
        if (args.length > 2) {
            new Cliente(nombre, args[1], Integer.parseInt(args[2])).ejecutar();
        } else {
            new Cliente(nombre).ejecutar();
        }
    }
}