package com.urlshortener.models;

public class CreateLinkResponse {
    private String shortUrl;
    private String shortCode;

    private CreateLinkResponse() {
    }

    public CreateLinkResponse(String shortUrl, String shortCode) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }
}