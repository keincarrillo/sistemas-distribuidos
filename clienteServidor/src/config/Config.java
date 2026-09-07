package config;

/**
 * Configuracion centralizada de la tienda distribuida.
 * Centraliza constantes para evitar valores magicos en el codigo.
 */
public final class Config {
    private Config() {
        // Utilidad: no instanciable
    }

    // Red
    public static final int PUERTO = 4444;
    public static final String HOST_DEFECTO = "localhost";

    // Dominio (mismos valores que la version con hilos/procesos)
    public static final int WORKERS = 2;
    public static final int CAPACIDAD_COLA = 5;
    public static final int CAPACIDAD_ALMACEN = 2;
    public static final int PEDIDOS_POR_CLIENTE = 5;

    // Temporizaciones (ms)
    public static final long PROCESO_WORKER_MS = 800;
    public static final long ESPERA_CLIENTE_MIN_MS = 400;
    public static final long ESPERA_CLIENTE_RANGO_MS = 400;

    // Productos disponibles
    public static final String[] PRODUCTOS = {"Laptop", "Mouse", "Teclado", "Monitor", "USB"};
}
