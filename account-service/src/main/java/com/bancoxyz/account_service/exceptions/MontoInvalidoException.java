package com.bancoxyz.account_service.exceptions;

public class MontoInvalidoException extends RuntimeException {

    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }
}