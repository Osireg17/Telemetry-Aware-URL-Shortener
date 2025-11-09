package com.urlshortener.manager;

import com.urlshortener.UrlShortenerConfiguration;
import com.urlshortener.core.Base62Service;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import com.urlshortener.models.LinkCreationError;
import com.urlshortener.models.LinkCreationResult;

import java.util.Optional;
import java.util.UUID;

public class LinkManager {

    private final LinkDAO linkDAO;
    private final Base62Service base62Service;
    private final UrlShortenerConfiguration.ApplicationConfiguration appConfig;

    public LinkManager(LinkDAO linkDAO, Base62Service base62Service,
                       UrlShortenerConfiguration.ApplicationConfiguration appConfig) {
        this.linkDAO = linkDAO;
        this.base62Service = base62Service;
        this.appConfig = appConfig;
    }

    public Optional<Link> findByShortCode(String shortCode) {
        return linkDAO.findByShortCode(shortCode);
    }

    public LinkCreationResult createLink(String longUrl, String customShortCode) {
        String shortCode;

        if (customShortCode != null && !customShortCode.isEmpty()) {
            if (customShortCode.length() > appConfig.getMaxCustomShortCodeLength()) {
                return LinkCreationResult.error(
                    LinkCreationError.CUSTOM_CODE_TOO_LONG,
                    "Custom short code exceeds maximum length of " + appConfig.getMaxCustomShortCodeLength()
                );
            }

            if (linkDAO.findByShortCode(customShortCode).isPresent()) {
                return LinkCreationResult.error(
                    LinkCreationError.CUSTOM_CODE_ALREADY_EXISTS,
                    "Custom URL is already taken."
                );
            }

            shortCode = customShortCode;
            Link link = new Link(longUrl, shortCode);
            linkDAO.save(link);
        } else {
            String tempShortCode = "temp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            Link link = new Link(longUrl, tempShortCode);
            long generatedId = linkDAO.save(link);
            shortCode = base62Service.encode(generatedId);
            linkDAO.updateShortCode(generatedId, shortCode);
        }

        String fullShortUrl = buildShortUrl(shortCode);
        return LinkCreationResult.success(fullShortUrl, shortCode);
    }

    public void incrementClickCount(long linkId) {
        linkDAO.incrementClickCount(linkId);
    }

    private String buildShortUrl(String shortCode) {
        String baseUrl = appConfig.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + shortCode;
    }
}
