package com.urlshortener.manager;

import com.urlshortener.core.Click;
import com.urlshortener.db.ClickDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClickManager {

    private static final Logger logger = LoggerFactory.getLogger(ClickManager.class);

    private final ClickDAO clickDAO;
    private final LinkManager linkManager;

    public ClickManager(ClickDAO clickDAO, LinkManager linkManager) {
        this.clickDAO = clickDAO;
        this.linkManager = linkManager;
    }

    public void recordClick(long linkId, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String ipAddress = getClientIpAddress(request);

        Click click = new Click(linkId, userAgent, ipAddress, referer);
        clickDAO.save(click);

        linkManager.incrementClickCount(linkId);

        logger.info("Captured click for link ID {}: IP={}, User-Agent={}, Referer={}",
                linkId, ipAddress, userAgent, referer);
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
