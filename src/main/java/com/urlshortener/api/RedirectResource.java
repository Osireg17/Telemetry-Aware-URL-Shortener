package com.urlshortener.api;

import com.codahale.metrics.annotation.Timed;
import com.urlshortener.core.Link;
import com.urlshortener.manager.ClickManager;
import com.urlshortener.manager.LinkManager;
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

    private final LinkManager linkManager;
    private final ClickManager clickManager;

    public RedirectResource(LinkManager linkManager, ClickManager clickManager) {
        this.linkManager = linkManager;
        this.clickManager = clickManager;
    }

    @GET
    @Path("/{shortCode}")
    @Timed
    public Response redirect(@PathParam("shortCode") String shortCode, @Context HttpServletRequest request) {
        Optional<Link> optionalLink = linkManager.findByShortCode(shortCode);

        if (optionalLink.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Link link = optionalLink.get();

        CompletableFuture.runAsync(() -> clickManager.recordClick(link.getId(), request));
        logger.info("Redirecting short code {} to URL {}", shortCode, link.getLongUrl());

        return Response.status(Response.Status.FOUND)
                .location(URI.create(link.getLongUrl()))
                .build();
    }
}
