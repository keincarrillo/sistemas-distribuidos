package config;

/**
 * Parametrizacion centralizada de la practica.
 * Evita literales magicos repartidos por el codigo.
 */
public final class Config {

    private Config() {
    }

    // ------------------------------------------------------------------
    // Red / RMI
    // ------------------------------------------------------------------
    /** Puerto base del registry RMI; el servidor N escucha en PUERTO_BASE + N - 1. */
    public static final int PUERTO_BASE = 4444;
    public static final int NUM_SERVIDORES = 3;
    public static final String HOST_DEFECTO = "localhost";
    /** Nombre bajo el cual se publica el objeto remoto en el registry. */
    public static final String NOMBRE_RMI = "Tienda";
    /**
     * Hostname que se incrusta en los stubs RMI.
     * Se sobreescribe con la variable de entorno RMI_HOSTNAME (ver ServidorRmi).
     */
    public static final String HOSTNAME_DEFECTO = "localhost";

    // ------------------------------------------------------------------
    // Dominio
    // ------------------------------------------------------------------
    public static final int WORKERS = 2;
    public static final int CAPACIDAD_COLA = 5;
    public static final int CAPACIDAD_ALMACEN = 2;
    public static final int PEDIDOS_POR_CLIENTE = 5;

    // ------------------------------------------------------------------
    // Aforo (llamadas remotas simultaneas)
    // ------------------------------------------------------------------
    public static final int MAX_LLAMADAS_SIMULTANEAS = 5;
    public static final int CLIENTES_POR_DEFECTO = 3;

    // ------------------------------------------------------------------
    // Reintentos del balanceador
    // ------------------------------------------------------------------
    public static final int MAX_INTENTOS = NUM_SERVIDORES;

    // ------------------------------------------------------------------
    // Temporizaciones (ms)
    // ------------------------------------------------------------------
    public static final long PROCESO_WORKER_MS = 800;
    public static final long ESPERA_CLIENTE_MIN_MS = 400;
    public static final long ESPERA_CLIENTE_RANGO_MS = 400;
    /** Espera entre intentos cuando un servidor esta lleno o caido. */
    public static final long ESPERA_REINTENTO_MS = 300;

    // ------------------------------------------------------------------
    // Catalogo
    // ------------------------------------------------------------------
    public static final String[] PRODUCTOS = {"Laptop", "Mouse", "Teclado", "Monitor", "USB"};
}
