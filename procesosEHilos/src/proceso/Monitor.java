package proceso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Monitor {
    public static void main(String[] args) {
        String archivo = args.length > 0 ? args[0] : "/tmp/stats.txt";
        long inicio = System.currentTimeMillis();

        System.out.printf("╔══════════════════════════════════════╗%n");
        System.out.printf("║  MONITOR - PID: %-20d║%n", ProcessHandle.current().pid());
        System.out.printf("╠══════════════════════════════════════╣%n");
        System.out.printf("║  Archivo: %-26s║%n", archivo);
        System.out.printf("╚══════════════════════════════════════╝%n%n");

        try {
            while (true) {
                Thread.sleep(1000);

                BufferedReader br = new BufferedReader(new FileReader(archivo));
                String linea = br.readLine();
                br.close();

                if (linea == null) continue;
                String[] p = linea.split("\\|");
                if (p.length < 4) continue;

                int creados = Integer.parseInt(p[0].trim());
                int procesados = Integer.parseInt(p[1].trim());
                int enCola = Integer.parseInt(p[2].trim());
                int enAlmacen = Integer.parseInt(p[3].trim());

                long segundos = (System.currentTimeMillis() - inicio) / 1000;

                System.out.printf("\r  [%ds] Creados: %d | Procesados: %d | Cola: %d | Almacen: %d   ",
                        segundos, creados, procesados, enCola, enAlmacen);

                if (creados > 0 && procesados >= creados && enCola == 0) {
                    System.out.printf("%n%n  [Monitor] Simulacion completa%n");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.printf("%n  [Monitor] Detenido%n");
        }
    }
}
