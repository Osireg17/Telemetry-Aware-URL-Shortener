package com.urlshortener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UrlShortenerConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty("application")
    private ApplicationConfiguration application = new ApplicationConfiguration();

    @Valid
    @NotNull
    @JsonProperty("kafka")
    private KafkaConfiguration kafka = new KafkaConfiguration();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    public ApplicationConfiguration getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfiguration application) {
        this.application = application;
    }

    public KafkaConfiguration getKafka() {
        return kafka;
    }

    public void setKafka(KafkaConfiguration kafka) {
        this.kafka = kafka;
    }

    public static class ApplicationConfiguration {

        @NotNull
        @NotBlank
        @JsonProperty("baseUrl")
        private String baseUrl = "http://localhost:8080";

        @NotNull
        @NotBlank
        @JsonProperty("version")
        private String version = "1.0.0-SNAPSHOT";

        @JsonProperty("name")
        private String name = "UrlShortener";

        // Future configuration options
        @JsonProperty("rateLimitRequestsPerMinute")
        private int rateLimitRequestsPerMinute = 20;

        @JsonProperty("maxCustomShortCodeLength")
        private int maxCustomShortCodeLength = 50;

        // Getters and setters
        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRateLimitRequestsPerMinute() {
            return rateLimitRequestsPerMinute;
        }

        public void setRateLimitRequestsPerMinute(int rateLimitRequestsPerMinute) {
            this.rateLimitRequestsPerMinute = rateLimitRequestsPerMinute;
        }

        public int getMaxCustomShortCodeLength() {
            return maxCustomShortCodeLength;
        }

        public void setMaxCustomShortCodeLength(int maxCustomShortCodeLength) {
            this.maxCustomShortCodeLength = maxCustomShortCodeLength;
        }
    }

    public static class KafkaConfiguration {

        @NotBlank
        @JsonProperty("bootstrapServers")
        private String bootstrapServers = "localhost:9092";

        @NotBlank
        @JsonProperty("topicName")
        private String topicName = "link_clicks";

        // === PSEUDOCODE: Change acknowledgment strategy ===
        // UPDATE default value from "1" to "all"
        // REASON: With idempotence enabled, "all" ensures replicas persist before ack
        // PREVENTS: Data loss during broker failover
        // ===================================================
        @NotBlank
        @JsonProperty("acks")
        private String acks = "all";

        // === PSEUDOCODE: Increase retry attempts ===
        // UPDATE retries from 3 to Integer.MAX_VALUE
        // REASON: With idempotence + acks="all", retries are safe and won't cause duplicates
        // ENSURES: Transient failures don't lose messages
        // ============================================
        @JsonProperty("retries")
        private int retries = Integer.MAX_VALUE;

        @JsonProperty("requestTimeoutMs")
        private int requestTimeoutMs = 30000;

        @JsonProperty("maxBlockMs")
        private int maxBlockMs = 60000;

        @JsonProperty("enableIdempotence")
        private boolean enableIdempotence = true;

        // === PSEUDOCODE: Add max in-flight requests limit ===
        // ADD new configuration field
        // SET default value to 5 (Kafka's recommended max for idempotence)
        // REASON: Prevents message reordering when retries occur
        // ENSURES: Messages are delivered in order even with retries
        // =====================================================
        @JsonProperty("maxInFlightRequestsPerConnection")
        private int maxInFlightRequestsPerConnection = 5;

        @JsonProperty("compressionType")
        private String compressionType = "lz4";

        // Getters and setters
        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getTopicName() {
            return topicName;
        }

        public void setTopicName(String topicName) {
            this.topicName = topicName;
        }

        public String getAcks() {
            return acks;
        }

        public void setAcks(String acks) {
            this.acks = acks;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public int getRequestTimeoutMs() {
            return requestTimeoutMs;
        }

        public void setRequestTimeoutMs(int requestTimeoutMs) {
            this.requestTimeoutMs = requestTimeoutMs;
        }

        public int getMaxBlockMs() {
            return maxBlockMs;
        }

        public void setMaxBlockMs(int maxBlockMs) {
            this.maxBlockMs = maxBlockMs;
        }

        public boolean isEnableIdempotence() {
            return enableIdempotence;
        }

        public void setEnableIdempotence(boolean enableIdempotence) {
            this.enableIdempotence = enableIdempotence;
        }

        // === PSEUDOCODE: Add getter and setter for maxInFlightRequestsPerConnection ===
        // ADD getter method that RETURNS the maxInFlightRequestsPerConnection value
        // ADD setter method that ACCEPTS an int parameter and SETS the field
        // ==============================================================================
        public int getMaxInFlightRequestsPerConnection() {
            return maxInFlightRequestsPerConnection;
        }

        public void setMaxInFlightRequestsPerConnection(int maxInFlightRequestsPerConnection) {
            this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection;
        }

        public String getCompressionType() {
            return compressionType;
        }

        public void setCompressionType(String compressionType) {
            this.compressionType = compressionType;
        }
    }
}
