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

            out.flush();

            while (true) {
                Object obj;
                try {
                    obj = in.readObject();
                } catch (EOFException e) {
                    break;
                }
                if (!(obj instanceof Pedido pedido)) {
                    System.out.printf("  [%s] objeto inesperado%n", clienteId);
                    continue;
                }

                pedido.asignarId(GeneradorIds.getInstancia().siguiente());

                System.out.printf("  [Server-%d] envia pedido#%d (%s)%n",
                        servidor.getServidorId(), pedido.getId(), pedido.getProducto());

                CompletableFuture<Resultado> futuro =
                        servidor.getResultados().registrar(pedido.getId());

                try {
                    servidor.getCola().put(pedido);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

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
