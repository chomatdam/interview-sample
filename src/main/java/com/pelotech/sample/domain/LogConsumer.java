package com.pelotech.sample.domain;

import com.pelotech.sample.domain.model.DetectionEvent;
import com.pelotech.sample.infrastructure.TopicProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogConsumer {
    private final KafkaTemplate<String, DetectionEvent> kafkaTemplate;
    private final MeterRegistry metricsRegistry;
    private final TopicProperties topicProperties;

    @KafkaListener(topics = "${topics.source}")
    public void listen(ConsumerRecord<String, String> event) {
        log.info("Got an event! {}", event);
        metricsRegistry.counter("metric", Tags.of("key", "value")).increment();

        try {
            kafkaTemplate.send(topicProperties.sink(), new DetectionEvent("id", "source", "userId")).get();
        } catch (Exception e) {}
    }
}
