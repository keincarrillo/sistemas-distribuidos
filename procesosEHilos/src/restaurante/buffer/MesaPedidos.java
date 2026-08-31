package restaurante.buffer;

import restaurante.model.Pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Buffer acotado con Lock + Conditions (productor-consumidor clasico)
public class MesaPedidos {
    private final List<Pedido> buffer;
    private final int capacidad;
    private final ReentrantLock lock;
    private final Condition noLlena;
    private final Condition noVacia;
    private boolean cerrado;

    public MesaPedidos(int capacidad) {
        this.capacidad = capacidad;
        this.buffer = new ArrayList<>(capacidad);
        this.lock = new ReentrantLock();
        this.noLlena = lock.newCondition();
        this.noVacia = lock.newCondition();
        this.cerrado = false;
    }

    // Productor: pone un pedido (bloquea si llena)
    public void poner(Pedido pedido) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.size() == capacidad) {
                noLlena.await();
            }
            buffer.add(pedido);
            noVacia.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Consumidor: toma un pedido (bloquea si vacia, null si cerrado)
    public Pedido tomar() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty() && !cerrado) {
                noVacia.await();
            }
            if (buffer.isEmpty()) return null;
            Pedido p = buffer.remove(0);
            noLlena.signalAll();
            return p;
        } finally {
            lock.unlock();
        }
    }

    // Senal para que los cocineros terminen
    public void cerrar() {
        lock.lock();
        try {
            cerrado = true;
            noLlena.signalAll();
            noVacia.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean estaVacia() {
        lock.lock();
        try { return buffer.isEmpty(); }
        finally { lock.unlock(); }
    }

    public int tamanio() {
        lock.lock();
        try { return buffer.size(); }
        finally { lock.unlock(); }
    }

    public int getCapacidad() { return capacidad; }
}
