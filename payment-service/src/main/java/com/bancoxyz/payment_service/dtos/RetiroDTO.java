package com.bancoxyz.payment_service.dtos;

import java.math.BigDecimal;

public class RetiroDTO {

    private BigDecimal monto;

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
