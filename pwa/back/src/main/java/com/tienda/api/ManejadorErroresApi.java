package com.tienda.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Convierte las excepciones de dominio en respuestas HTTP con
 * un cuerpo de error uniforme.
 */
@RestControllerAdvice
public class ManejadorErroresApi {

    @ExceptionHandler(ProductoInvalidoException.class)
    public ResponseEntity<Map<String, String>> productoInvalido(ProductoInvalidoException e) {
        return responder(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler({ColaLlenaException.class, ServidorLlenoException.class})
    public ResponseEntity<Map<String, String>> servidorOcupado(RuntimeException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    @ExceptionHandler(ProcesoDemoradoException.class)
    public ResponseEntity<Map<String, String>> procesoDemorado(ProcesoDemoradoException e) {
        return responder(HttpStatus.GATEWAY_TIMEOUT, e);
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
