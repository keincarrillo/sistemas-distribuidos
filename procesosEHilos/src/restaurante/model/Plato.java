package restaurante.model;

public enum Plato {
    ENSALADA("Ensalada Cesar", 1500, "Freidora"),
    PASTA("Pasta Carbonara", 3000, "Estufa"),
    CARNE("Lomo al Trapo", 4500, "Parrilla"),
    PIZZA("Pizza Margherita", 3500, "Horno"),
    POSTRE("Tiramisu", 2000, "Estufa");

    private final String nombre;
    private final long tiempoCoccionMs;
    private final String estacion;

    Plato(String nombre, long tiempoCoccionMs, String estacion) {
        this.nombre = nombre;
        this.tiempoCoccionMs = tiempoCoccionMs;
        this.estacion = estacion;
    }

    public String getNombre() { return nombre; }
    public long getTiempoCoccionMs() { return tiempoCoccionMs; }
    public String getEstacion() { return estacion; }
}
