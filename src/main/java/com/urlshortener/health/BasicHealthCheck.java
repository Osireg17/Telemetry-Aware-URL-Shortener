
package com.urlshortener.health;

import com.codahale.metrics.health.HealthCheck;

public class BasicHealthCheck extends HealthCheck {

    private final String version;

    public BasicHealthCheck(String version) {
        this.version = version;
    }

    @Override
    protected Result check() throws Exception {
        // Add any basic application checks here
        if (version != null && !version.isEmpty()) {
            return Result.healthy("URL Shortener API is running. Version: " + version);
        } else {
            return Result.unhealthy("Version information not available");
        }
    }
}