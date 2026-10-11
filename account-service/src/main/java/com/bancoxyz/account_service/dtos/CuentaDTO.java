package com.bancoxyz.account_service.dtos;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class CuentaDTO {

    private Long cuentaId;

    private BigDecimal saldo;

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }
}