package restaurante.proceso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// Proceso OS separado que monitorea el sistema
// Se lanza con ProcessBuilder desde RestauranteMain
public class MonitorProceso {

    public static void main(String[] args) {
        String rutaStats = args.length > 0 ? args[0] : "/tmp/restaurante_stats.txt";
        long inicio = System.currentTimeMillis();

        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  MONITOR DE PROCESOS - PID: "
                + ProcessHandle.current().pid() + "       ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  Leyendo: " + rutaStats + "  ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        while (true) {
            File archivo = new File(rutaStats);
            if (!archivo.exists()) {
                try { Thread.sleep(200); } catch (InterruptedException e) { break; }
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea = br.readLine();
                if (linea == null) {
                    Thread.sleep(500);
                    continue;
                }

                String[] partes = linea.split("\\|");
                if (partes.length < 7) {
                    Thread.sleep(500);
                    continue;
                }

                int creados = Integer.parseInt(partes[0].trim());
                int completados = Integer.parseInt(partes[1].trim());
                int enMesa = Integer.parseInt(partes[2].trim());
                int enResultado = Integer.parseInt(partes[3].trim());
                long tiempoEspera = Long.parseLong(partes[5].trim());
                long tiempoCoccion = Long.parseLong(partes[6].trim());

                long elapsed = (System.currentTimeMillis() - inicio) / 1000;

                System.out.printf("\r  [MONITOR %ds] Creados: %d | Completados: %d | Mesa: %d | Cola: %d | Espera: %dms | Coccion: %dms   ",
                        elapsed, creados, completados, enMesa, enResultado, tiempoEspera, tiempoCoccion);

                // Si todos los pedidos estan completados y no hay nada en cola, salir
                if (creados > 0 && completados >= creados && enMesa == 0 && enResultado == 0) {
                    System.out.println("\n");
                    System.out.println("  [MONITOR] Simulacion completa");
                    break;
                }

            } catch (IOException | InterruptedException e) {
                break;
            } catch (NumberFormatException ignored) {}

            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
    }
}
