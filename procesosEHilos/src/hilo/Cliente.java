package hilo;

import cola.ColaPedidos;
import model.Pedido;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Cliente extends Thread {
    private final ColaPedidos cola;
    private final Random random;
    private static final AtomicInteger contadorPedidos = new AtomicInteger();

    public Cliente(String nombre, ColaPedidos cola) {
        super(nombre);
        this.cola = cola;
        this.random = new Random();
    }

    public static int getContadorPedidos() {
        return contadorPedidos.get();
    }

    @Override
    public void run() {
        String[] productos = {"Laptop", "Mouse", "Teclado", "Monitor", "USB"};
        try {
            for (int i = 0; i < 5; i++) {
                String producto = productos[random.nextInt(productos.length)];
                Pedido pedido = new Pedido(producto);
                contadorPedidos.incrementAndGet();
                cola.put(pedido);
                System.out.printf("  [%s] crea pedido#%d (%s)%n",
                        getName(), pedido.getId(), producto);
                Thread.sleep(400 + random.nextInt(400));
            }
            System.out.printf("  [%s] termino%n", getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
