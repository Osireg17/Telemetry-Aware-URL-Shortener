package com.urlshortener.api;

import com.urlshortener.core.Base62Service;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinksResource {
    
    private final LinkDAO linkDAO;
    private final Base62Service base62Service;
    
    public LinksResource(LinkDAO linkDAO, Base62Service base62Service) {
        this.linkDAO = linkDAO;
        this.base62Service = base62Service;
    }
    
    @POST
    public Response createShortLink(@Valid CreateLinkRequest request) {
        Link link = new Link(request.getLongUrl(), "temp");
        long generatedId = linkDAO.save(link);
        
        String shortCode = base62Service.encode(generatedId);
        linkDAO.updateShortCode(generatedId, shortCode);
        
        CreateLinkResponse response = new CreateLinkResponse(
            "http://localhost:8080/" + shortCode,
            shortCode
        );
        
        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }
    
    public static class CreateLinkRequest {
        @NotNull
        private String longUrl;
        
        public String getLongUrl() { return longUrl; }
        public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    }
    
    public static class CreateLinkResponse {
        private String shortUrl;
        private String shortCode;
        
        public CreateLinkResponse(String shortUrl, String shortCode) {
            this.shortUrl = shortUrl;
            this.shortCode = shortCode;
        }
        
        public String getShortUrl() { return shortUrl; }
        public String getShortCode() { return shortCode; }
    }
}
