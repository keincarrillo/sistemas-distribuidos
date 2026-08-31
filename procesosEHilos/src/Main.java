import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Productor-Consumidor con buffer acotado + semaforo + proceso separado
public class Main {

    // === BUFFER ACOTADO (sincronizado con Lock + Conditions) ===
    static class Buffer {
        private final int[] buf = new int[4];
        private int count, in, out;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition lleno = lock.newCondition();
        private final Condition vacio = lock.newCondition();

        void put(int val) throws InterruptedException {
            lock.lock();
            try {
                while (count == buf.length) lleno.await();
                buf[in] = val;
                in = (in + 1) % buf.length;
                count++;
                System.out.println("  [Buffer] put " + val + " (" + count + "/" + buf.length + ")");
                vacio.signalAll();
            } finally { lock.unlock(); }
        }

        int take() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) vacio.await();
                int val = buf[out];
                out = (out + 1) % buf.length;
                count--;
                System.out.println("  [Buffer] take " + val + " (" + count + "/" + buf.length + ")");
                lleno.signalAll();
                return val;
            } finally { lock.unlock(); }
        }
    }

    // === SEMAFORO: recurso compartido (impresora con 1 cola) ===
    static class Impresora {
        private final Semaphore sem = new Semaphore(1, true);

        void imprimir(String hilo, int dato) throws InterruptedException {
            sem.acquire();
            try {
                System.out.println("  [" + hilo + "] imprime " + dato);
                Thread.sleep(600);
            } finally { sem.release(); }
        }
    }

    // === PRODUCTOR (hilo) ===
    static class Productor extends Thread {
        private final Buffer buf;
        Productor(Buffer b) { buf = b; }

        public void run() {
            try {
                for (int i = 1; i <= 8; i++) {
                    buf.put(i);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    // === CONSUMIDOR (hilo) ===
    static class Consumidor extends Thread {
        private final Buffer buf;
        private final Impresora imp;
        Consumidor(Buffer b, Impresora i) { buf = b; imp = i; }

        public void run() {
            try {
                for (int i = 0; i < 8; i++) {
                    int v = buf.take();
                    imp.imprimir(getName(), v);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    // === MAIN ===
    public static void main(String[] args) throws Exception {
        System.out.println("=== SISTEMA CONCURRENTE ===\n");

        Buffer buf = new Buffer();
        Impresora imp = new Impresora();

        // 1) Lanzar proceso monitor separado (ProcessBuilder)
        Process monitor = null;
        try {
            String java = ProcessHandle.current().info().command().orElse("java");
            ProcessBuilder pb = new ProcessBuilder(java, "-cp", "out", "Monitor");
            pb.inheritIO();
            monitor = pb.start();
            System.out.println("[Main] Monitor PID: " + monitor.pid());
        } catch (Exception e) {
            System.out.println("[Main] Monitor no disponible");
        }

        // 2) Lanzar hilos productor y consumidor
        Productor p = new Productor(buf);
        Consumidor c = new Consumidor(buf, imp);
        p.start();
        c.start();

        // 3) Esperar a que terminen
        p.join();
        c.join();

        // 4) Cerrar monitor
        if (monitor != null) monitor.destroyForcibly();

        System.out.println("\n=== FIN ===");
        System.out.println("Hilos: productor + consumidor");
        System.out.println("Procesos: principal + monitor");
        System.out.println("Sync: Lock+Conditions (buffer), Semaphore (impresora)");
    }
}
