package com.urlshortener.api;

import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

@Path("/")
public class RedirectResource {
    
    private final LinkDAO linkDAO;
    
    public RedirectResource(LinkDAO linkDAO) {
        this.linkDAO = linkDAO;
    }
    
    @GET
    @Path("/{shortCode}")
    public Response redirect(@PathParam("shortCode") String shortCode) {
        Optional<Link> optionalLink = linkDAO.findByShortCode(shortCode);
        
        if (optionalLink.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Link link = optionalLink.get();
        return Response.status(Response.Status.FOUND)
                .location(URI.create(link.getLongUrl()))
                .build();
    }
}