package com.bancoxyz.auditoria.config;

import com.bancoxyz.auditoria.kafka.CuentaEvento;
import com.bancoxyz.auditoria.kafka.TransaccionEvento;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> consumerProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );
        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "true"
        );

        return properties;
    }

    @Bean
    public ConsumerFactory<String, TransaccionEvento>
    transaccionConsumerFactory() {

        JacksonJsonDeserializer<TransaccionEvento> jsonDeserializer =
                new JacksonJsonDeserializer<>(TransaccionEvento.class);

        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages(
                "com.bancoxyz.auditoria.kafka"
        );

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConsumerFactory<String, CuentaEvento> cuentaConsumerFactory() {

        JacksonJsonDeserializer<CuentaEvento> jsonDeserializer =
                new JacksonJsonDeserializer<>(CuentaEvento.class);

        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages(
                "com.bancoxyz.auditoria.kafka"
        );

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransaccionEvento>
    transaccionKafkaListenerContainerFactory() {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, TransaccionEvento>();

        factory.setConsumerFactory(transaccionConsumerFactory());

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CuentaEvento>
    cuentaKafkaListenerContainerFactory() {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, CuentaEvento>();

        factory.setConsumerFactory(cuentaConsumerFactory());

        return factory;
    }
}