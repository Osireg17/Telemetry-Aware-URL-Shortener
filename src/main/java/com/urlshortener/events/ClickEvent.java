package com.urlshortener.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClickEvent {
    @NotNull
    private Long linkId;        // Required: Database ID of the link

    @NotBlank
    private String shortCode;   // Required: The short code that was clicked

    @NotBlank
    private String timestamp;   // Required: ISO-8601 format timestamp

    private String userAgent;   // Optional: Browser/client information
    private String ipAddress;   // Optional: Client IP (consider privacy)
    private String referer;     // Optional: Referring URL

    // Default constructor
    public ClickEvent() {
    }

    // Constructor with required fields
    public ClickEvent(Long linkId, String shortCode, String timestamp) {
        this.linkId = linkId;
        this.shortCode = shortCode;
        this.timestamp = timestamp;
    }

    // Constructor with all fields
    public ClickEvent(Long linkId, String shortCode, String timestamp,
                     String userAgent, String ipAddress, String referer) {
        this.linkId = linkId;
        this.shortCode = shortCode;
        this.timestamp = timestamp;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.referer = referer;
    }

    // Getters
    public Long getLinkId() {
        return linkId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getReferer() {
        return referer;
    }

    // Setters
    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public String toString() {
        return "ClickEvent{" +
                "linkId=" + linkId +
                ", shortCode='" + shortCode + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", referer='" + referer + '\'' +
                '}';
    }

    // === PSEUDOCODE: Make equals() null-safe ===
    // KEEP the identity check (this == o)
    // KEEP the null/class check
    // REPLACE direct .equals() calls with Objects.equals(field, that.field)
    // USE pattern matching for instanceof (modern Java)
    // REASON: Objects.equals() handles null safely - prevents NPE if fields are null
    // ===========================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClickEvent that)) return false;
        return java.util.Objects.equals(linkId, that.linkId)
                && java.util.Objects.equals(shortCode, that.shortCode)
                && java.util.Objects.equals(timestamp, that.timestamp);
    }

    // === PSEUDOCODE: Make hashCode() null-safe ===
    // REPLACE manual hash calculation with Objects.hash(fields...)
    // PASS all fields used in equals() to Objects.hash()
    // REASON: Objects.hash() handles nulls gracefully (treats null as 0)
    // =============================================
    @Override
    public int hashCode() {
        return java.util.Objects.hash(linkId, shortCode, timestamp);
    }
}
