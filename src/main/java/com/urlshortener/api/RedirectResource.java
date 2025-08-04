package com.urlshortener.api;

import com.codahale.metrics.annotation.Timed;
import com.urlshortener.core.Click;
import com.urlshortener.core.Link;
import com.urlshortener.db.ClickDAO;
import com.urlshortener.db.LinkDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Path("/")
public class RedirectResource {
    
    private static final Logger logger = LoggerFactory.getLogger(RedirectResource.class);
    
    private final LinkDAO linkDAO;
    private final ClickDAO clickDAO;
    
    public RedirectResource(LinkDAO linkDAO, ClickDAO clickDAO) {
        this.linkDAO = linkDAO;
        this.clickDAO = clickDAO;
    }
    
    @GET
    @Path("/{shortCode}")
    @Timed
    public Response redirect(@PathParam("shortCode") String shortCode, @Context HttpServletRequest request) {
        Optional<Link> optionalLink = linkDAO.findByShortCode(shortCode);
        
        if (optionalLink.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Link link = optionalLink.get();
        
        // Capture telemetry asynchronously to ensure fast redirect
        CompletableFuture.runAsync(() -> captureTelemetry(link, request));
        
        return Response.status(Response.Status.FOUND)
                .location(URI.create(link.getLongUrl()))
                .build();
    }
    
    private void captureTelemetry(Link link, HttpServletRequest request) {
        try {
            // Extract telemetry data from request
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String ipAddress = getClientIpAddress(request);
            
            // Create and save click record
            Click click = new Click(link.getId(), userAgent, ipAddress, referer);
            clickDAO.save(click);
            
            // Increment click count
            linkDAO.incrementClickCount(link.getId());
            
        } catch (Exception e) {
            // Log error but don't let it affect the redirect
            logger.error("Failed to capture telemetry for link ID: " + link.getId(), e);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for X-Forwarded-For header (common in load balancers/proxies)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check for X-Real-IP header (another common proxy header)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
}