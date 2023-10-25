package com.pelotech.sample;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pelotech.sample.domain.model.DetectionEvent;
import io.findify.s3mock.S3Mock;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(topics = DetectApplicationTests.SOURCE_TOPIC)
class DetectApplicationTests {
    public static final String SOURCE_TOPIC = "source-topic";
    private static final String SINK_TOPIC = "sink-topic";
    private static final int S3_PORT = 8001;
    private static final String BUCKET_NAME = "bucket-name";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private S3Mock s3Mock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonS3 amazonS3;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    public void setUp() {
        s3Mock = new S3Mock.Builder().withPort(S3_PORT).withInMemoryBackend().build();
        s3Mock.start();
        s3Mock.p().createBucket(BUCKET_NAME, null);

        producer = createKafkaProducer();
        consumer = createKafkaConsumer();
        consumer.subscribe(List.of(SINK_TOPIC));
    }

    @Test
    public void shouldConsumeEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        String eventKey = "SOURCE KEY";
        String eventValue = "SOURCE VALUE";
        producer.send(new ProducerRecord<>(SOURCE_TOPIC, eventKey, eventValue)).get();

        ConsumerRecord<String, String> consumerRecord =
                KafkaTestUtils.getSingleRecord(consumer, SINK_TOPIC, Duration.ofSeconds(5));

        DetectionEvent record = objectMapper.readValue(consumerRecord.value(), DetectionEvent.class);

        assertEquals("id", record.id());
    }

    @Test
    public void shouldRetrieveS3Object() throws IOException {
        String s3Key = "/path";
        String s3Value = "content";
        amazonS3.putObject(BUCKET_NAME, s3Key, s3Value);

        S3Object s3Object = amazonS3.getObject(BUCKET_NAME, s3Key);
        String result = new String(s3Object.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(s3Value, result);
    }

    @AfterEach
    public void tearDown() {
        s3Mock.shutdown();
        consumer.unsubscribe();
        consumer.close();
        producer.close();
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new KafkaConsumer<>(consumerProps);
    }

    private KafkaProducer<String, String> createKafkaProducer() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaProducer<>(producerProps);
    }

    @TestConfiguration
    static class S3TestConfig {
        @Bean
        @Primary
        public AmazonS3 amazonS3SClient() {
            AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:%s".formatted(S3_PORT), "us-west-2");

            return AmazonS3ClientBuilder
                    .standard()
                    .withPathStyleAccessEnabled(true)
                    .withEndpointConfiguration(endpoint)
                    .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                    .build();
        }
    }
}
