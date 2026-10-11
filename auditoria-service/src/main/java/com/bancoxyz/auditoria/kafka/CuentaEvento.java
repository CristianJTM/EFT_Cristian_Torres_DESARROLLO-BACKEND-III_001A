package com.bancoxyz.auditoria.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaEvento (
    String evento,
    Long cuentaId,
    BigDecimal saldo,
    String tipo,
    LocalDateTime fecha
) {
}
