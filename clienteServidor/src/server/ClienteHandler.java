package server;

import cola.ColaPedidos;
import cola.ResultadosPendientes;
import model.GeneradorIds;
import model.Pedido;
import model.Resultado;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * Atiende a UN cliente remoto conectado por socket. Es el punto de entrada
 * de un productor: lee sus pedidos, los encola en la cola compartida y espera
 * a que un worker los procese para enviarle la confirmacion de vuelta.
 *
 * Patron: separa la responsabilidad de red del resto de la logica del servidor.
 */
public class ClienteHandler implements Runnable {
    private final Servidor servidor;
    private final Socket socket;

    public ClienteHandler(Servidor servidor, Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
    }

    @Override
    public void run() {
        String clienteId = "Cliente-" + socket.getPort();
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.flush(); // envia la cabecera del ObjectOutputStream

            while (true) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException e) {
                    break; // el cliente cerro la conexion
                }
                if (!(obj instanceof Pedido pedido)) {
                    System.out.printf("  [%s] objeto inesperado%n", clienteId);
                    continue;
                }

                // El servidor asigna el id global unico (evita colisiones entre clientes)
                pedido.asignarId(GeneradorIds.getInstancia().siguiente());

                System.out.printf("  [%s] envia pedido#%d (%s)%n",
                        clienteId, pedido.getId(), pedido.getProducto());

                CompletableFuture<Resultado> futuro =
                        servidor.getResultados().registrar(pedido.getId());

                // Encolar para que un worker lo procese (bloquea si cola llena)
                try {
                    servidor.getCola().put(pedido);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Esperar a que el worker complete y enviar la respuesta
                Resultado resultado = futuro.join();
                out.writeObject(resultado);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.printf("  [%s] desconectado: %s%n", clienteId, e.getMessage());
        } finally {
            servidor.clienteDesconectado(socket);
            System.out.printf("  [%s] termino%n", clienteId);
        }
    }
}