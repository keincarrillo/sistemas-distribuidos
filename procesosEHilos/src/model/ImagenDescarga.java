package model;

public class ImagenDescarga {
    private String url;
    private String nombreArchivo;

    public ImagenDescarga(String url, String nombreArchivo) {
        this.url = url;
        this.nombreArchivo = nombreArchivo;
    }

    public String getUrl() {
        return url;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}
