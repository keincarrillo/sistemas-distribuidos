package recurso;

import java.util.concurrent.Semaphore;

/**
 * Recurso compartido de los workers del servidor: limita cuantos pueden
 * procesar pedidos a la vez (semaphore justo).
 */
public class Almacen {
    private final Semaphore semaforo;
    private final String nombre;
    private final int capacidad;

    public Almacen(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.semaforo = new Semaphore(capacidad, true);
    }

    public boolean entrar(String hilo) throws InterruptedException {
        boolean ok = semaforo.tryAcquire();
        if (ok) {
            System.out.printf("  [%s] entra a %s (%d/%d)%n",
                    hilo, nombre, semaforo.availablePermits(), capacidad);
        }
        return ok;
    }

    public void salir(String hilo) {
        semaforo.release();
        System.out.printf("  [%s] sale de %s (%d/%d)%n",
                hilo, nombre, semaforo.availablePermits(), capacidad);
    }

    public int getDisponibles() {
        return semaforo.availablePermits();
    }
}