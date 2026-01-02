package com.urlshortener.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.UrlShortenerConfiguration.KafkaConfiguration;
import com.urlshortener.events.ClickEvent;

import io.dropwizard.lifecycle.Managed;

/**
 * EventPublisher handles publishing click events to Kafka. Implements
 * Dropwizard's Managed interface for proper lifecycle management.
 */
public class EventPublisher implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaProducer<String, String> producer;
    private final String topicName;
    private final ObjectMapper objectMapper;

    public EventPublisher(KafkaConfiguration kafkaConfig) {
        this(kafkaConfig, new ObjectMapper());
    }

    public EventPublisher(KafkaConfiguration kafkaConfig, ObjectMapper objectMapper) {
        this(createProducer(kafkaConfig), kafkaConfig.getTopicName(), objectMapper);
    }

    /**
     * Constructor for testing that accepts a KafkaProducer. Allows injection of
     * mock producers for unit testing.
     */
    public EventPublisher(KafkaProducer<String, String> producer, String topicName, ObjectMapper objectMapper) {
        this.producer = producer;
        this.topicName = topicName;
        this.objectMapper = objectMapper;

        LOGGER.info("EventPublisher initialized with topic: {}", topicName);
    }

    private static KafkaProducer<String, String> createProducer(KafkaConfiguration config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, config.getRetries());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getRequestTimeoutMs());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, config.getMaxBlockMs());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, config.isEnableIdempotence());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, config.getMaxInFlightRequestsPerConnection());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.getCompressionType());

        return new KafkaProducer<>(props);
    }

    /**
     * Publishes a click event to Kafka. Errors are propagated to the caller for
     * handling.
     *
     * @param event The click event to publish
     * @throws JsonProcessingException if event serialization fails
     * @throws ExecutionException if Kafka send fails
     * @throws InterruptedException if thread is interrupted
     */
    public void publishClickEvent(ClickEvent event) throws JsonProcessingException, ExecutionException, InterruptedException {
        String eventJson = objectMapper.writeValueAsString(event);
        String key = event.getShortCode(); // Use shortCode as partition key for ordering

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, eventJson);

        LOGGER.debug("Publishing click event: linkId={}, shortCode={}", event.getLinkId(), event.getShortCode());

        // Synchronous send - blocks until confirmation or throws exception
        RecordMetadata metadata = producer.send(record).get();

        LOGGER.info("Successfully published click event to partition {} offset {}: linkId={}, shortCode={}",
                metadata.partition(), metadata.offset(), event.getLinkId(), event.getShortCode());
    }

    @Override
    public void start() {
        LOGGER.info("EventPublisher started");
    }

    @Override
    public void stop() {
        LOGGER.info("Closing Kafka producer...");
        if (producer != null) {
            producer.close();
            LOGGER.info("Kafka producer closed successfully");
        }
    }
}
