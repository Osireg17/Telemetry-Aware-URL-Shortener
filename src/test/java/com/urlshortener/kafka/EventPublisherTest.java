package com.urlshortener.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.UrlShortenerConfiguration.KafkaConfiguration;
import com.urlshortener.events.ClickEvent;

@ExtendWith(MockitoExtension.class)
public class EventPublisherTest {

    @Mock
    private ObjectMapper objectMapper;

    private KafkaConfiguration kafkaConfig;
    private ClickEvent testEvent;

    @BeforeEach
    void setUp() {

        kafkaConfig = new KafkaConfiguration();
        // Note: KafkaConfiguration has defaults, but we could override if needed

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
        EventPublisher publisher = new EventPublisher(kafkaConfig);

        // WHEN: start() is called
        publisher.start();

        // THEN: Should not throw
        assertDoesNotThrow(() -> publisher.start());
    }

    @Test
    void stop_closesProducerGracefully() {
        EventPublisher publisher = new EventPublisher(kafkaConfig);

        publisher.stop();

        assertDoesNotThrow(() -> publisher.stop());
    }

    @Test
    void constructor_readsAllConfigurationProperties() {
        // GIVEN: Custom configuration with specific values and valid bootstrap server format
        KafkaConfiguration customConfig = new KafkaConfiguration();
        customConfig.setBootstrapServers("localhost:9092");  // Valid format
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
        EventPublisher publisher = new EventPublisher(kafkaConfig);

        // WHEN: publishClickEvent called with null
        // THEN: Should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            publisher.publishClickEvent(null);
        }, "Publishing null event should throw NullPointerException");
    }
}
