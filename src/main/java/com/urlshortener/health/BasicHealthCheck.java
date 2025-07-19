package com.urlshortener.health;

import com.codahale.metrics.health.HealthCheck;

public class BasicHealthCheck extends HealthCheck {

    private final String version;
    private final String applicationName;

    public BasicHealthCheck(String version) {
        this(version, "URL Shortener API");
    }

    public BasicHealthCheck(String version, String applicationName) {
        this.version = version;
        this.applicationName = applicationName;
    }

    @Override
    protected Result check() throws Exception {
        // Add any basic application checks here
        if (version != null && !version.isEmpty()) {
            return Result.healthy(applicationName + " is running. Version: " + version);
        } else {
            return Result.unhealthy("Version information not available");
        }
    }
}