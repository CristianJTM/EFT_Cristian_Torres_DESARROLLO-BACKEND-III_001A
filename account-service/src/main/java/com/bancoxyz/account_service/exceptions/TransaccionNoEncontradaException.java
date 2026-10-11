package com.bancoxyz.account_service.exceptions;

public class TransaccionNoEncontradaException extends RuntimeException {

    public TransaccionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}