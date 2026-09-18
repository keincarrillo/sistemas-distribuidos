package config;

public final class Config {
    private Config() {
    }

    // red
    public static final int PUERTO = 4444;
    public static final String HOST_DEFECTO = "localhost";

    // dominio
    public static final int WORKERS = 2;
    public static final int CAPACIDAD_COLA = 5;
    public static final int CAPACIDAD_ALMACEN = 2;
    public static final int PEDIDOS_POR_CLIENTE = 5;

    // multi clientes contra un solo servidor
    public static final int MAX_CLIENTES_SIMULTANEOS = 5;
    public static final int CLIENTES_POR_DEFECTO = 3;

    // temporizaciones (ms)
    public static final long PROCESO_WORKER_MS = 800;
    public static final long ESPERA_CLIENTE_MIN_MS = 400;
    public static final long ESPERA_CLIENTE_RANGO_MS = 400;

    // productos disponibles
    public static final String[] PRODUCTOS = {"Laptop", "Mouse", "Teclado", "Monitor", "USB"};
}