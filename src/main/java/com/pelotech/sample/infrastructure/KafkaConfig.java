package com.pelotech.sample.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pelotech.sample.domain.model.DetectionEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, DetectionEvent> kafkaProducerFactory(SslBundles sslBundles,
                                                                        KafkaProperties properties,
                                                                        ObjectMapper objectMapper) {
        return new DefaultKafkaProducerFactory<>(
                properties.buildProducerProperties(sslBundles),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, DetectionEvent> kafkaTemplate(ProducerFactory<String, DetectionEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
