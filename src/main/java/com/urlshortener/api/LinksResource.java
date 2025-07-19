package com.urlshortener.api;

import com.urlshortener.UrlShortenerConfiguration;
import com.urlshortener.core.Base62Service;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinksResource {

    private final LinkDAO linkDAO;
    private final Base62Service base62Service;
    private final UrlShortenerConfiguration.ApplicationConfiguration appConfig;

    public LinksResource(LinkDAO linkDAO, Base62Service base62Service,
                         UrlShortenerConfiguration.ApplicationConfiguration appConfig) {
        this.linkDAO = linkDAO;
        this.base62Service = base62Service;
        this.appConfig = appConfig;
    }

    @POST
    public Response createShortLink(@Valid CreateLinkRequest request) {
        String shortCode;
        if (request.getCustomShortCode() != null && !request.getCustomShortCode().isEmpty()) {
            // Validate custom short code length
            if (request.getCustomShortCode().length() > appConfig.getMaxCustomShortCodeLength()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Custom short code exceeds maximum length of " + appConfig.getMaxCustomShortCodeLength())
                        .build();
            }

            shortCode = request.getCustomShortCode();
            if (linkDAO.findByShortCode(shortCode).isPresent()) {
                return Response.status(Response.Status.CONFLICT).entity("Custom URL is already taken.").build();
            }
            Link link = new Link(request.getLongUrl(), shortCode);
            linkDAO.save(link);
        } else {
            // Generate a unique temporary short code to avoid collisions
            String tempShortCode = "temp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            Link link = new Link(request.getLongUrl(), tempShortCode);
            long generatedId = linkDAO.save(link);
            shortCode = base62Service.encode(generatedId);
            linkDAO.updateShortCode(generatedId, shortCode);
        }

        // Use configured base URL instead of hardcoded value
        String fullShortUrl = buildShortUrl(shortCode);
        CreateLinkResponse response = new CreateLinkResponse(fullShortUrl, shortCode);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    private String buildShortUrl(String shortCode) {
        String baseUrl = appConfig.getBaseUrl();
        // Ensure base URL doesn't end with slash and short code doesn't start with slash
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + shortCode;
    }

    public static class CreateLinkRequest {
        @NotNull
        @NotBlank
        @URL
        private String longUrl;

        private String customShortCode;

        public String getLongUrl() { return longUrl; }
        public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

        public String getCustomShortCode() { return customShortCode; }
        public void setCustomShortCode(String customShortCode) { this.customShortCode = customShortCode; }
    }

    public static class CreateLinkResponse {
        private String shortUrl;
        private String shortCode;

        private CreateLinkResponse() { /* Jackson deserialization */ }

        public CreateLinkResponse(String shortUrl, String shortCode) {
            this.shortUrl = shortUrl;
            this.shortCode = shortCode;
        }

        public String getShortUrl() { return shortUrl; }
        public String getShortCode() { return shortCode; }
    }
}