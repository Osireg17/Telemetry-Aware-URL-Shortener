package com.urlshortener.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.urlshortener.core.Link;
import com.urlshortener.events.ClickEvent;
import com.urlshortener.kafka.EventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class ClickManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClickManager.class);

    private final EventPublisher eventPublisher;

    public ClickManager(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void recordClick(Link link, HttpServletRequest request) throws JsonProcessingException, ExecutionException, InterruptedException {
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String ipAddress = getClientIpAddress(request);
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ClickEvent event = new ClickEvent(
                link.getId(),
                link.getShortCode(),
                timestamp,
                userAgent,
                ipAddress,
                referer
        );

        LOGGER.debug("Publishing click event: linkId={}, shortCode={}", link.getId(), link.getShortCode());
        eventPublisher.publishClickEvent(event);
        LOGGER.info("Published click event for link ID {}: shortCode={}, IP={}, User-Agent={}",
                event.getLinkId(), event.getShortCode(), event.getIpAddress(), event.getUserAgent());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
