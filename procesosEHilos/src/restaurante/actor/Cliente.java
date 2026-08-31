package restaurante.actor;

import restaurante.Estadisticas;
import restaurante.buffer.MesaPedidos;
import restaurante.model.Pedido;
import restaurante.model.Plato;

import java.util.Random;

// Productor: genera pedidos y los pone en la mesa
public class Cliente extends Thread {
    private final MesaPedidos mesa;
    private final Estadisticas stats;
    private final Random random = new Random();

    public Cliente(String nombre, MesaPedidos mesa, Estadisticas stats) {
        super(nombre);
        this.mesa = mesa;
        this.stats = stats;
    }

    @Override
    public void run() {
        try {
            int numPedidos = 1 + random.nextInt(3);
            for (int i = 0; i < numPedidos; i++) {
                Plato plato = Plato.values()[random.nextInt(Plato.values().length)];
                Pedido pedido = new Pedido(plato, getName());
                stats.registrarCreacion();

                System.out.printf("  [%s] Pide: %s%n", getName(), plato.getNombre());
                mesa.poner(pedido);

                Thread.sleep(500 + random.nextInt(1000));
            }
            System.out.printf("  [%s] Se fue pidio %d platos%n", getName(), numPedidos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
