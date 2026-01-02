package com.urlshortener.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TelemetryConsumerConfiguration extends Configuration {

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
    private KafkaConsumerConfiguration kafka = new KafkaConsumerConfiguration();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

    public ApplicationConfiguration getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfiguration application) {
        this.application = application;
    }

    public KafkaConsumerConfiguration getKafka() {
        return kafka;
    }

    public void setKafka(KafkaConsumerConfiguration kafka) {
        this.kafka = kafka;
    }

    public static class ApplicationConfiguration {

        @NotNull
        @NotBlank
        @JsonProperty("name")
        private String name = "TelemetryConsumer";

        @NotNull
        @NotBlank
        @JsonProperty("version")
        private String version = "1.0.0-SNAPSHOT";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class KafkaConsumerConfiguration {

        @NotBlank
        @JsonProperty("bootstrapServers")
        private String bootstrapServers = "localhost:9092";

        @NotBlank
        @JsonProperty("topicName")
        private String topicName = "link_clicks";

        @NotBlank
        @JsonProperty("groupId")
        private String groupId = "telemetry-consumer-group";

        @NotBlank
        @JsonProperty("autoOffsetReset")
        private String autoOffsetReset = "earliest";

        @JsonProperty("enableAutoCommit")
        private boolean enableAutoCommit = false;

        @JsonProperty("maxPollRecords")
        private int maxPollRecords = 500;

        @JsonProperty("sessionTimeoutMs")
        private int sessionTimeoutMs = 30000;

        @JsonProperty("maxPollIntervalMs")
        private int maxPollIntervalMs = 300000;

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

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getAutoOffsetReset() {
            return autoOffsetReset;
        }

        public void setAutoOffsetReset(String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

        public boolean isEnableAutoCommit() {
            return enableAutoCommit;
        }

        public void setEnableAutoCommit(boolean enableAutoCommit) {
            this.enableAutoCommit = enableAutoCommit;
        }

        public int getMaxPollRecords() {
            return maxPollRecords;
        }

        public void setMaxPollRecords(int maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }

        public int getSessionTimeoutMs() {
            return sessionTimeoutMs;
        }

        public void setSessionTimeoutMs(int sessionTimeoutMs) {
            this.sessionTimeoutMs = sessionTimeoutMs;
        }

        public int getMaxPollIntervalMs() {
            return maxPollIntervalMs;
        }

        public void setMaxPollIntervalMs(int maxPollIntervalMs) {
            this.maxPollIntervalMs = maxPollIntervalMs;
        }
    }
}
