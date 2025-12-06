package com.urlshortener.models;

public class LinkCreationResult {
    private final boolean success;
    private final String shortUrl;
    private final String shortCode;
    private final LinkCreationError error;
    private final String errorMessage;

    private LinkCreationResult(boolean success, String shortUrl, String shortCode,
                               LinkCreationError error, String errorMessage) {
        this.success = success;
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public static LinkCreationResult success(String shortUrl, String shortCode) {
        return new LinkCreationResult(true, shortUrl, shortCode, null, null);
    }

    public static LinkCreationResult error(LinkCreationError error, String errorMessage) {
        return new LinkCreationResult(false, null, null, error, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public LinkCreationError getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}