package restaurante.recurso;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Recurso compartido controlado por semaforo
public class EstacionCocina {
    private final String nombre;
    private final Semaphore semaforo;
    private volatile String ocupanteActual;

    public EstacionCocina(String nombre, int capacidad) {
        this.nombre = nombre;
        this.semaforo = new Semaphore(capacidad, true);
    }

    public boolean adquirir(String cocinero) throws InterruptedException {
        boolean ok = semaforo.tryAcquire(10, TimeUnit.SECONDS);
        if (ok) {
            ocupanteActual = cocinero;
        }
        return ok;
    }

    public void liberar(String cocinero) {
        semaforo.release();
        ocupanteActual = null;
    }

    public String getNombre() { return nombre; }
    public int getPermisosDisponibles() { return semaforo.availablePermits(); }
    public String getOcupanteActual() { return ocupanteActual; }
}
