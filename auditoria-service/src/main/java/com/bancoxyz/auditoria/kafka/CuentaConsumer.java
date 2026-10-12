package com.bancoxyz.auditoria.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CuentaConsumer {

    private static final String TOPIC = "cuentas-eventos";

    @KafkaListener(
            topics = TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "cuentaKafkaListenerContainerFactory"
    )
    public void consumirCuenta(CuentaEvento evento) {

        log.info("========================================");
        log.info("EVENTO DE CUENTA RECIBIDO");
        log.info("Evento: {}", evento.evento());
        log.info("Cuenta: {}", evento.cuentaId());
        log.info("Saldo: {}", evento.saldo());
        log.info("Tipo: {}", evento.tipo());
        log.info("Fecha: {}", evento.fecha());
        log.info("========================================");
    }
}