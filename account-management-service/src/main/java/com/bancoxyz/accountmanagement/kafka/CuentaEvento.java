package com.bancoxyz.accountmanagement.kafka;

import java.math.BigDecimal;
import java.time.Instant;

public class CuentaEvento {

    private String evento;
    private Long cuentaId;
    private BigDecimal saldo;
    private String tipo;
    private Instant fecha;

    public CuentaEvento() {
    }

    public CuentaEvento(
            String evento,
            Long cuentaId,
            BigDecimal saldo,
            String tipo,
            Instant fecha) {
        this.evento = evento;
        this.cuentaId = cuentaId;
        this.saldo = saldo;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
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

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}