import restaurante.RestauranteMain;

import java.util.Scanner;

void main() throws Exception {
    System.out.println();
    System.out.println("========================================");
    System.out.println("  SISTEMAS DISTRIBUIDOS - PRACTICA");
    System.out.println("========================================");
    System.out.println();
    System.out.println("  1. Simulador de cocina concurrente");
    System.out.println("     (hilos + procesos + semaforos)");
    System.out.println();
    System.out.print("  Elige opcion [1]: ");

    Scanner sc = new Scanner(System.in);
    String opcion = sc.nextLine().trim();

    switch (opcion) {
        case "1" -> {
            System.out.println();
            RestauranteMain.main(new String[]{});
        }
        default -> System.out.println("  Opcion invalida");
    }
}
