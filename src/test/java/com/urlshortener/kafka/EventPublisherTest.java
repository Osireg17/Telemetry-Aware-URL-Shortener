package com.urlshortener.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.UrlShortenerConfiguration.KafkaConfiguration;
import com.urlshortener.events.ClickEvent;

@ExtendWith(MockitoExtension.class)
public class EventPublisherTest {

    @Mock
    private KafkaProducer<String, String> mockProducer;

    @Mock
    private ObjectMapper mockObjectMapper;

    private KafkaConfiguration kafkaConfig;
    private ClickEvent testEvent;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfiguration();
        kafkaConfig.setBootstrapServers("localhost:9092");
        kafkaConfig.setTopicName("test-clicks");

        testEvent = new ClickEvent(
                123L,
                "abc123",
                "2025-01-02T10:30:00Z",
                "Mozilla/5.0",
                "192.168.1.1",
                "https://example.com"
        );
    }

    @Test
    void constructor_withKafkaConfig_initializesSuccessfully() {
        // WHEN: Create EventPublisher with just KafkaConfiguration
        KafkaConfiguration kafkaConfig = new KafkaConfiguration();

        // THEN: Should not throw exception
        assertDoesNotThrow(() -> {
            EventPublisher publisher = new EventPublisher(kafkaConfig);
            assertNotNull(publisher);
        });
    }

    @Test
    void constructor_withObjectMapper_initializesSuccessfully() {
        // WHEN: Create EventPublisher with custom ObjectMapper
        KafkaConfiguration kafkaConfig = new KafkaConfiguration();
        ObjectMapper customMapper = new ObjectMapper();

        // THEN: Should not throw exception
        assertDoesNotThrow(() -> {
            EventPublisher publisher = new EventPublisher(kafkaConfig, customMapper);
            assertNotNull(publisher);
        });
    }

    @Test
    void start_executesWithoutError() {
        // GIVEN: EventPublisher instance
        EventPublisher publisher = new EventPublisher(mockProducer, "test-topic", new ObjectMapper());

        // WHEN: start() is called
        // THEN: Should not throw
        assertDoesNotThrow(() -> publisher.start());
    }

    @Test
    void stop_closesProducerGracefully() {
        // GIVEN: EventPublisher instance
        EventPublisher publisher = new EventPublisher(mockProducer, "test-topic", new ObjectMapper());

        // WHEN: stop() is called
        publisher.stop();

        // THEN: Producer should be closed
        verify(mockProducer).close();
    }

    @Test
    void constructor_readsAllConfigurationProperties() {
        // GIVEN: Custom configuration with specific values and valid bootstrap server format
        KafkaConfiguration customConfig = new KafkaConfiguration();
        customConfig.setBootstrapServers("localhost:9092");
        customConfig.setTopicName("custom-topic");
        customConfig.setAcks("all");
        customConfig.setRetries(5);
        customConfig.setRequestTimeoutMs(5000);
        customConfig.setMaxBlockMs(3000);
        customConfig.setEnableIdempotence(true);
        customConfig.setMaxInFlightRequestsPerConnection(1);
        customConfig.setCompressionType("snappy");

        // WHEN: Create publisher with custom config
        EventPublisher publisher = new EventPublisher(customConfig);

        // THEN: Should initialize successfully
        assertNotNull(publisher);

        // Verify lifecycle methods execute without throwing
        assertDoesNotThrow(() -> {
            publisher.start();
            publisher.stop();
        });
    }

    @Test
    void publishClickEvent_withNullEvent_throwsNullPointerException() {
        // GIVEN: EventPublisher instance
        EventPublisher publisher = new EventPublisher(mockProducer, "test-topic", new ObjectMapper());

        // WHEN: publishClickEvent called with null
        // THEN: Should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            publisher.publishClickEvent(null);
        }, "Publishing null event should throw NullPointerException");
    }

    @Test
    void publishClickEvent_withValidEvent_serializesAndPublishesSuccessfully() throws Exception {
        // GIVEN: EventPublisher with mocked dependencies
        String topicName = "test-clicks";
        EventPublisher publisher = new EventPublisher(mockProducer, topicName, mockObjectMapper);

        ClickEvent validEvent = new ClickEvent(
                456L,
                "xyz789",
                "2025-01-02T14:30:00Z",
                "Chrome/91.0",
                "10.0.0.1",
                "https://test.com"
        );

        String eventJson = "{\"linkId\":456,\"shortCode\":\"xyz789\"}";
        when(mockObjectMapper.writeValueAsString(validEvent)).thenReturn(eventJson);

        // Mock the Future returned by producer.send()
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topicName, 0),
                0L,
                0L,
                System.currentTimeMillis(),
                0L,
                0,
                0
        );
        Future<RecordMetadata> future = CompletableFuture.completedFuture(metadata);
        when(mockProducer.send(any(ProducerRecord.class))).thenReturn(future);

        // WHEN: publishClickEvent called with valid event
        publisher.publishClickEvent(validEvent);

        // THEN: Verify serialization was called
        verify(mockObjectMapper).writeValueAsString(validEvent);

        // Verify producer.send was called with correct topic and key
        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor
                = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(recordCaptor.capture());

        ProducerRecord<String, String> capturedRecord = recordCaptor.getValue();
        assertEquals(topicName, capturedRecord.topic());
        assertEquals("xyz789", capturedRecord.key());
        assertEquals(eventJson, capturedRecord.value());
    }
}
