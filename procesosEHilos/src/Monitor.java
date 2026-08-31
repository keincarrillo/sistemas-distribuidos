// Proceso OS separado, se lanza con ProcessBuilder
public class Monitor {
    public static void main(String[] args) {
        System.out.println("[Monitor] PID=" + ProcessHandle.current().pid() + " activo");
        try { Thread.currentThread().join(); }
        catch (InterruptedException e) { System.out.println("[Monitor] detenido"); }
    }
}
