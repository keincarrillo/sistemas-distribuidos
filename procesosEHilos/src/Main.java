import model.ImagenDescarga;
import service.DescargadorService;

void main() throws InterruptedException {
    List<ImagenDescarga> imagenes = List.of(
            new ImagenDescarga("https://picsum.photos/id/10/400/300", "img1.jpg"),
            new ImagenDescarga("https://picsum.photos/id/20/400/300", "img2.jpg"),
            new ImagenDescarga("https://picsum.photos/id/30/400/300", "img3.jpg"),
            new ImagenDescarga("https://picsum.photos/id/40/400/300", "img4.jpg")
    );

    DescargadorService servicio = new DescargadorService(4);
    servicio.descargarTodas(imagenes);
}
