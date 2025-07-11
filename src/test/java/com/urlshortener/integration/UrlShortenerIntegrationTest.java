package com.urlshortener.integration;

import com.urlshortener.UrlShortenerApplication;
import com.urlshortener.UrlShortenerConfiguration;
import com.urlshortener.api.LinksResource;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class UrlShortenerIntegrationTest {

    private static final DropwizardAppExtension<UrlShortenerConfiguration> APP = 
        new DropwizardAppExtension<>(
            UrlShortenerApplication.class,
            "src/test/resources/test-config.yml"
        );

    @AfterEach
    void cleanupDatabase() {
        // Clean up database after each test
        ((UrlShortenerApplication) APP.getApplication()).getJdbi().useHandle(handle -> 
            handle.execute("DELETE FROM links")
        );
    }

    private Response createLinkRequest(String longUrl, String customShortCode) {
        LinksResource.CreateLinkRequest request = new LinksResource.CreateLinkRequest();
        request.setLongUrl(longUrl);
        if (customShortCode != null) {
            request.setCustomShortCode(customShortCode);
        }
        
        return APP.client()
            .target("http://localhost:" + APP.getLocalPort() + "/api/v1/links")
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON));
    }

    private Response createLinkRequest(String longUrl) {
        return createLinkRequest(longUrl, null);
    }

    private Response redirectRequest(String shortCode) {
        return APP.client()
            .target("http://localhost:" + APP.getLocalPort() + "/" + shortCode)
            .request()
            .property("jersey.config.client.followRedirects", false)
            .get();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_andRedirect_happyPathE2E() {
        // Step 1: Create a short link
        String longUrl = "https://example.com/some/very/long/path";
        Response createResponse = createLinkRequest(longUrl);
        
        // Verify creation was successful
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        
        LinksResource.CreateLinkResponse createResponseBody = 
            createResponse.readEntity(LinksResource.CreateLinkResponse.class);
        assertNotNull(createResponseBody.getShortCode());
        assertNotNull(createResponseBody.getShortUrl());
        assertTrue(createResponseBody.getShortUrl().contains(createResponseBody.getShortCode()));
        
        String shortCode = createResponseBody.getShortCode();
        
        // Step 2: Use the short code to redirect
        Response redirectResponse = redirectRequest(shortCode);
        
        // Verify redirect works correctly
        assertEquals(Response.Status.FOUND.getStatusCode(), redirectResponse.getStatus());
        assertEquals(longUrl, redirectResponse.getLocation().toString());
        
        // Step 3: Verify data is persisted in database
        LinkDAO linkDAO = ((UrlShortenerApplication) APP.getApplication()).getJdbi().onDemand(LinkDAO.class);
        Optional<Link> savedLink = linkDAO.findByShortCode(shortCode);
        assertTrue(savedLink.isPresent());
        assertEquals(longUrl, savedLink.get().getLongUrl());
        assertEquals(shortCode, savedLink.get().getShortCode());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_withCustomCode_happyPath() {
        String longUrl = "https://example.com";
        String customCode = "my-custom-link";
        
        Response createResponse = createLinkRequest(longUrl, customCode);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        
        LinksResource.CreateLinkResponse responseBody = 
            createResponse.readEntity(LinksResource.CreateLinkResponse.class);
        assertEquals(customCode, responseBody.getShortCode());
        assertTrue(responseBody.getShortUrl().contains(customCode));
        
        // Test redirect works
        Response redirectResponse = redirectRequest(customCode);
        assertEquals(Response.Status.FOUND.getStatusCode(), redirectResponse.getStatus());
        assertEquals(longUrl, redirectResponse.getLocation().toString());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_withDuplicateCustomCode_returnsConflict() {
        String longUrl1 = "https://example1.com";
        String longUrl2 = "https://example2.com";
        String customCode = "duplicate-code";
        
        // First request should succeed
        Response firstResponse = createLinkRequest(longUrl1, customCode);
        assertEquals(Response.Status.CREATED.getStatusCode(), firstResponse.getStatus());
        
        // Second request with same custom code should fail
        Response secondResponse = createLinkRequest(longUrl2, customCode);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), secondResponse.getStatus());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_withInvalidUrl_returnsBadRequest() {
        Response response = createLinkRequest("not-a-valid-url");
        assertEquals(422, response.getStatus()); // Unprocessable Entity for validation errors
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_withNullUrl_returnsBadRequest() {
        Response response = createLinkRequest(null);
        assertEquals(422, response.getStatus());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createShortLink_withEmptyUrl_returnsBadRequest() {
        Response response = createLinkRequest("");
        assertEquals(422, response.getStatus());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void redirect_withNonExistentShortCode_returnsNotFound() {
        Response response = redirectRequest("non-existent-code");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void multipleLinksCreation_generatesUniqueShortCodes() {
        String url1 = "https://example1.com";
        String url2 = "https://example2.com";
        String url3 = "https://example3.com";
        
        Response response1 = createLinkRequest(url1);
        Response response2 = createLinkRequest(url2);
        Response response3 = createLinkRequest(url3);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());
        assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());
        assertEquals(Response.Status.CREATED.getStatusCode(), response3.getStatus());
        
        LinksResource.CreateLinkResponse body1 = response1.readEntity(LinksResource.CreateLinkResponse.class);
        LinksResource.CreateLinkResponse body2 = response2.readEntity(LinksResource.CreateLinkResponse.class);
        LinksResource.CreateLinkResponse body3 = response3.readEntity(LinksResource.CreateLinkResponse.class);
        
        // Verify all short codes are unique
        assertNotEquals(body1.getShortCode(), body2.getShortCode());
        assertNotEquals(body1.getShortCode(), body3.getShortCode());
        assertNotEquals(body2.getShortCode(), body3.getShortCode());
        
        // Verify all redirects work correctly
        assertEquals(Response.Status.FOUND.getStatusCode(), redirectRequest(body1.getShortCode()).getStatus());
        assertEquals(Response.Status.FOUND.getStatusCode(), redirectRequest(body2.getShortCode()).getStatus());
        assertEquals(Response.Status.FOUND.getStatusCode(), redirectRequest(body3.getShortCode()).getStatus());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void databasePersistence_verifyDataIntegrity() {
        String longUrl = "https://persistence-test.com";
        
        // Create link
        Response createResponse = createLinkRequest(longUrl);
        LinksResource.CreateLinkResponse responseBody = 
            createResponse.readEntity(LinksResource.CreateLinkResponse.class);
        String shortCode = responseBody.getShortCode();
        
        // Directly query database to verify persistence
        LinkDAO linkDAO = ((UrlShortenerApplication) APP.getApplication()).getJdbi().onDemand(LinkDAO.class);
        Optional<Link> savedLink = linkDAO.findByShortCode(shortCode);
        
        assertTrue(savedLink.isPresent());
        Link link = savedLink.get();
        assertEquals(longUrl, link.getLongUrl());
        assertEquals(shortCode, link.getShortCode());
        assertNotNull(link.getCreatedAt());
        assertTrue(link.getId() > 0);
    }
}