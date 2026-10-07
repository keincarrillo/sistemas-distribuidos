package rmi;

import java.io.Serializable;

/**
 * Excepcion de aplicacion: el servidor rechaza la llamada porque ya se
 * alcanzo el limite de llamadas remotas simultaneas. Al ser Serializable
 * y estar declarada en la interfaz remota, viaja intacta hasta el cliente.
 */
public class ServidorLlenoException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    public ServidorLlenoException(String mensaje) {
        super(mensaje);
    }
}