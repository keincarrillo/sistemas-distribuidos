package service;

import model.ImagenDescarga;
import util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DescargadorService {
    private static final String CARPETA_DESTINO = "descargas/";
    private final int maxHilos;

    public DescargadorService(int maxHilos) {
        this.maxHilos = maxHilos;
    }

    public void descargarTodas(List<ImagenDescarga> imagenes) throws InterruptedException {
        // Pool de hilos: máximo maxHilos trabajando al mismo tiempo
        ExecutorService pool = Executors.newFixedThreadPool(maxHilos);
        List<Future<Boolean>> resultados = new ArrayList<>();

        long inicio = System.currentTimeMillis();

        // Lanza un hilo por cada imagen
        for (ImagenDescarga img : imagenes) {
            resultados.add(pool.submit(() ->
                    HttpUtil.descargar(img.getUrl(), CARPETA_DESTINO + img.getNombreArchivo())
            ));
        }

        // Recoge los resultados conforme terminan
        int exitosas = 0;
        for (int i = 0; i < resultados.size(); i++) {
            try {
                boolean ok = resultados.get(i).get(); // espera a que ese hilo termine
                String estado = ok ? "200" : "500";
                System.out.println(estado + " " + imagenes.get(i).getNombreArchivo());
                if (ok) exitosas++;
            } catch (ExecutionException e) {
                System.err.println("Fallo en hilo: " + e.getMessage());
            }
        }

        pool.shutdown();
        long duracion = System.currentTimeMillis() - inicio;
        System.out.println("\n" + exitosas + "/" + imagenes.size()
                + " descargadas en " + duracion + " ms");
    }
}
