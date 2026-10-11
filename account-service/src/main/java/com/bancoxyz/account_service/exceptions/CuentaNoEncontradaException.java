package com.bancoxyz.account_service.exceptions;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}