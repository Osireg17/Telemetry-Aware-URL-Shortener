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
}
