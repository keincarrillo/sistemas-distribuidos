package server;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aforo de llamadas remotas simultaneas sobre el objeto distribuido.
 * Adaptacion del RegistroClientes de la practica de sockets: en RMI el
 * "cliente" no mantiene una sesion de conexion, asi que el limite se mide
 * sobre las llamadas en vuelo (activAs).
 */
public class RegistroLlamadas {

    private final Semaphore plazas;
    private final AtomicInteger activas = new AtomicInteger();
    private final AtomicInteger atendidas = new AtomicInteger();
    private final AtomicInteger rechazadas = new AtomicInteger();

    public RegistroLlamadas(int maxSimultaneas) {
        this.plazas = new Semaphore(maxSimultaneas, true);
    }

    /** true si hay plaza; en caso contrario cuenta un rechazo. */
    public boolean entrar() {
        boolean ok = plazas.tryAcquire();
        if (ok) {
            activas.incrementAndGet();
            atendidas.incrementAndGet();
        } else {
            rechazadas.incrementAndGet();
        }
        return ok;
    }

    public void salir() {
        activas.decrementAndGet();
        plazas.release();
    }

    public int getActivas() {
        return activas.get();
    }

    public int getAtendidas() {
        return atendidas.get();
    }

    public int getRechazadas() {
        return rechazadas.get();
    }
}