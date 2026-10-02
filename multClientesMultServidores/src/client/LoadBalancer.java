package client;

import config.Config;
import java.util.Random;

public class LoadBalancer {
    private static final int NUM_SERVIDORES = Config.NUM_SERVIDORES;
    private static final int PUERTO_BASE = Config.PUERTO_BASE;
    private static final String HOST = Config.HOST_DEFECTO;
    private final Random random = new Random();

    public ServidorInfo siguiente() {
        int offset = random.nextInt(NUM_SERVIDORES);
        int puerto = PUERTO_BASE + offset;
        return new ServidorInfo(HOST, puerto, offset + 1);
    }

    public ServidorInfo[] getAll() {
        ServidorInfo[] servidores = new ServidorInfo[NUM_SERVIDORES];
        for (int i = 0; i < NUM_SERVIDORES; i++) {
            servidores[i] = new ServidorInfo(HOST, PUERTO_BASE + i, i + 1);
        }
        return servidores;
    }

    public static class ServidorInfo {
        public final String host;
        public final int puerto;
        public final int id;

        public ServidorInfo(String host, int puerto, int id) {
            this.host = host;
            this.puerto = puerto;
            this.id = id;
        }

        @Override
        public String toString() {
            return host + ":" + puerto + " (server" + id + ")";
        }
    }
}
