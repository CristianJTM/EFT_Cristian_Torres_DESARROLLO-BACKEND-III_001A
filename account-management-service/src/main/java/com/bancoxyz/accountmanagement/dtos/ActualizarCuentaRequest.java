package com.bancoxyz.accountmanagement.dtos;

import java.math.BigDecimal;

public class ActualizarCuentaRequest {

    private BigDecimal saldo;
    private String tipo;

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}