package proceso;

public class Monitor {
    public static void main(String[] args) {
        System.out.printf("╔══════════════════════════════════════╗%n");
        System.out.printf("║  MONITOR - PID: %-20d║%n", ProcessHandle.current().pid());
        System.out.printf("║  Proceso OS separado activo         ║%n");
        System.out.printf("╚══════════════════════════════════════╝%n");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("[Monitor] Detenido");
        }
    }
}
