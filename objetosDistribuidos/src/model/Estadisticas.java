package model;

import java.io.Serializable;

/**
 * DTO serializable con las estadísticas de un servidor.
 * Se obtiene invocando el metodo remoto {@code obtenerEstadisticas()}.
 */
public class Estadisticas implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int servidorId;
    private final int pedidosProcesados;
    private final int llamadasAtendidas;
    private final int llamadasRechazadas;
    private final int llamadasActivas;
    private final int pedidosEnCola;

    public Estadisticas(int servidorId, int pedidosProcesados, int llamadasAtendidas,
                        int llamadasRechazadas, int llamadasActivas, int pedidosEnCola) {
        this.servidorId = servidorId;
        this.pedidosProcesados = pedidosProcesados;
        this.llamadasAtendidas = llamadasAtendidas;
        this.llamadasRechazadas = llamadasRechazadas;
        this.llamadasActivas = llamadasActivas;
        this.pedidosEnCola = pedidosEnCola;
    }

    public int getServidorId() {
        return servidorId;
    }

    public int getPedidosProcesados() {
        return pedidosProcesados;
    }

    public int getLlamadasAtendidas() {
        return llamadasAtendidas;
    }

    public int getLlamadasRechazadas() {
        return llamadasRechazadas;
    }

    public int getLlamadasActivas() {
        return llamadasActivas;
    }

    public int getPedidosEnCola() {
        return pedidosEnCola;
    }

    @Override
    public String toString() {
        return String.format("Servidor-%d {procesados=%d, atendidas=%d, rechazadas=%d, activas=%d, enCola=%d}",
                servidorId, pedidosProcesados, llamadasAtendidas, llamadasRechazadas,
                llamadasActivas, pedidosEnCola);
    }
}
