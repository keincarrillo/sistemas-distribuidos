package util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil {
    private static final HttpClient cliente = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    public static boolean descargar(String url, String rutaDestino) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<byte[]> respuesta = cliente.send(
                    peticion, HttpResponse.BodyHandlers.ofByteArray()
            );

            if (respuesta.statusCode() != 200) {
                System.err.println("Error HTTP " + respuesta.statusCode() + " -> " + url);
                return false;
            }

            // Crea la carpeta destino si no existe
            File archivo = new File(rutaDestino);
            archivo.getParentFile().mkdirs();

            try (FileOutputStream salida = new FileOutputStream(archivo)) {
                salida.write(respuesta.body());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Excepción descargando " + url + ": " + e.getMessage());
            return false;
        }
    }
}
