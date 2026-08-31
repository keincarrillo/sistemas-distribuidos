package cola;

import model.Pedido;
import java.util.concurrent.locks.*;

public class ColaPedidos {
    private final Pedido[] buffer;
    private int cantidad;
    private int entrada;
    private int salida;
    private final ReentrantLock lock;
    private final Condition espacio;
    private final Condition datos;
    private boolean cerrado;

    public ColaPedidos(int capacidad) {
        this.buffer = new Pedido[capacidad];
        this.lock = new ReentrantLock();
        this.espacio = lock.newCondition();
        this.datos = lock.newCondition();
    }

    public void put(Pedido pedido) throws InterruptedException {
        lock.lock();
        try {
            while (cantidad == buffer.length && !cerrado) {
                espacio.await();
            }
            if (cerrado) return;
            buffer[entrada] = pedido;
            entrada = (entrada + 1) % buffer.length;
            cantidad++;
            datos.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Pedido take() throws InterruptedException {
        lock.lock();
        try {
            while (cantidad == 0 && !cerrado) {
                datos.await();
            }
            if (cantidad == 0) return null;
            Pedido pedido = buffer[salida];
            buffer[salida] = null;
            salida = (salida + 1) % buffer.length;
            cantidad--;
            espacio.signalAll();
            return pedido;
        } finally {
            lock.unlock();
        }
    }

    public void cerrar() {
        lock.lock();
        try {
            cerrado = true;
            espacio.signalAll();
            datos.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int getCantidad() {
        lock.lock();
        try {
            return cantidad;
        } finally {
            lock.unlock();
        }
    }

    public int getCapacidad() {
        return buffer.length;
    }
}
