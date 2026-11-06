package com.tienda.cola;

import com.tienda.model.Pedido;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Buffer circular acotado entre los hilos productores (peticiones HTTP)
 * y los workers consumidores. Lock + Condition como en la practica de hilos.
 */
public class ColaPedidos {

    private final Pedido[] buffer;
    private int cantidad;
    private int entrada;
    private int salida;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition datos = lock.newCondition();
    private boolean cerrado;

    public ColaPedidos(int capacidad) {
        this.buffer = new Pedido[capacidad];
    }

    /**
     * Intenta encolar sin bloquearse: en HTTP no tiene sentido esperar
     * a que un worker haga sitio. Devuelve false si la cola esta llena.
     */
    public boolean tryPut(Pedido pedido) {
        lock.lock();
        try {
            if (cerrado || cantidad == buffer.length) {
                return false;
            }
            buffer[entrada] = pedido;
            entrada = (entrada + 1) % buffer.length;
            cantidad++;
            datos.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Bloquea hasta que haya un pedido. Devuelve null si la cola
     * esta cerrada y vacia (fin de los workers).
     */
    public Pedido take() throws InterruptedException {
        lock.lock();
        try {
            while (cantidad == 0 && !cerrado) {
                datos.await();
            }
            if (cantidad == 0) {
                return null;
            }
            Pedido pedido = buffer[salida];
            buffer[salida] = null;
            salida = (salida + 1) % buffer.length;
            cantidad--;
            return pedido;
        } finally {
            lock.unlock();
        }
    }

    public void cerrar() {
        lock.lock();
        try {
            cerrado = true;
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
