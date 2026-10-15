package com.tienda.server;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite de peticiones HTTP simultaneas: si no hay plaza, se responde 503.
 * Es el equivalente al aforo de clientes simultaneos de la practica de
 * sockets, aplicado aqui a peticiones en vuelo.
 */
public class RegistroClientes {

    private final Semaphore plazas;
    private final AtomicInteger activos = new AtomicInteger();
    private final AtomicInteger atendidos = new AtomicInteger();
    private final AtomicInteger rechazados = new AtomicInteger();

    public RegistroClientes(int maxSimultaneos) {
        this.plazas = new Semaphore(maxSimultaneos, true);
    }

    public boolean entrar() {
        boolean ok = plazas.tryAcquire();
        if (ok) {
            activos.incrementAndGet();
            atendidos.incrementAndGet();
        } else {
            rechazados.incrementAndGet();
        }
        return ok;
    }

    public void salir() {
        activos.decrementAndGet();
        plazas.release();
    }

    public int getActivos() {
        return activos.get();
    }

    public int getAtendidos() {
        return atendidos.get();
    }

    public int getRechazados() {
        return rechazados.get();
    }
}
