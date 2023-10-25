package com.pelotech.sample.domain;

import com.pelotech.sample.domain.model.DetectionEvent;
import com.pelotech.sample.infrastructure.TopicProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogConsumerTest {
    @Mock
    private KafkaTemplate<String, DetectionEvent> kafkaTemplate;
    @Mock
    private MeterRegistry metricsRegistry;
    @Mock
    private Counter counter;
    @Mock
    private TopicProperties topicProperties;
    @InjectMocks
    private LogConsumer logConsumer;

    @Test
    void shouldConsume() {
        when(metricsRegistry.counter("metric", Tags.of("key", "value"))).thenReturn(counter);
        when(topicProperties.sink()).thenReturn("sink");
        ConsumerRecord<String, String> consumerRecord =
                new ConsumerRecord<>("topic", 0, 0L, "key", "value");

        logConsumer.listen(consumerRecord);

        verify(kafkaTemplate, times(1)).send(anyString(), any(DetectionEvent.class));
        verify(counter, times(1)).increment();
    }
}
