package com.urlshortener.core;

import java.time.Instant;

public class Click {
    private Long id;
    private Long linkId;
    private Instant clickTimestamp;
    private String userAgent;
    private String ipAddress;
    private String referer;

    public Click() {}

    public Click(Long linkId, String userAgent, String ipAddress, String referer) {
        this.linkId = linkId;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.referer = referer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Instant getClickTimestamp() {
        return clickTimestamp;
    }

    public void setClickTimestamp(Instant clickTimestamp) {
        this.clickTimestamp = clickTimestamp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}