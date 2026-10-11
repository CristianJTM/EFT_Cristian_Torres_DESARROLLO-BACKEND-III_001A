package com.bancoxyz.accountmanagement.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CuentaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(CuentaProducer.class);

    private final KafkaTemplate<String, CuentaEvento> kafkaTemplate;
    private final String topic;

    public CuentaProducer(
            KafkaTemplate<String, CuentaEvento> kafkaTemplate,
            @Value("${app.kafka.topic.cuentas}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publicar(
            String tipoEvento,
            Long cuentaId,
            java.math.BigDecimal saldo,
            String tipo) {

        CuentaEvento evento = new CuentaEvento(
                tipoEvento,
                cuentaId,
                saldo,
                tipo,
                Instant.now()
        );

        kafkaTemplate.send(topic, String.valueOf(cuentaId), evento)
                .whenComplete((resultado, error) -> {
                    if (error != null) {
                        log.error(
                                "Error publicando evento {} para cuenta {}",
                                tipoEvento,
                                cuentaId,
                                error
                        );
                    } else {
                        log.info(
                                "Evento {} enviado para cuenta {} al tópico {}",
                                tipoEvento,
                                cuentaId,
                                topic
                        );
                    }
                });
    }
}
