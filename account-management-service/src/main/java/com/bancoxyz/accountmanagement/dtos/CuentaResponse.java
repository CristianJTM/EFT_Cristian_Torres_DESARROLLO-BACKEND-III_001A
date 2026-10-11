package com.bancoxyz.accountmanagement.dtos;

import java.math.BigDecimal;

public class CuentaResponse {

    private Long cuentaId;
    private BigDecimal saldo;
    private String tipo;

    public CuentaResponse() {
    }

    public CuentaResponse(Long cuentaId, BigDecimal saldo, String tipo) {
        this.cuentaId = cuentaId;
        this.saldo = saldo;
        this.tipo = tipo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

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