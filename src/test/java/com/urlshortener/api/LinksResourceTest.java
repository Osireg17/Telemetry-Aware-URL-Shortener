package com.urlshortener.api;

import com.urlshortener.core.Base62Service;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class LinksResourceTest {

    private final LinkDAO linkDAO = mock(LinkDAO.class);
    private final Base62Service base62Service = mock(Base62Service.class);
    private final ResourceExtension resource = ResourceExtension.builder()
            .addResource(new LinksResource(linkDAO, base62Service))
            .build();


    @AfterEach
    void tearDown() {
        reset(linkDAO);
        reset(base62Service);
    }

    private Response createLinkRequest(String url) {
        LinksResource.CreateLinkRequest request = new LinksResource.CreateLinkRequest();
        request.setLongUrl(url);
        return resource.target("/api/v1/links").request().post(Entity.json(request));
    }

    @Test
    void createShortLink_happyPath() {
        // Arrange
        when(linkDAO.save(any(Link.class))).thenReturn(123L);
        when(base62Service.encode(123L)).thenReturn("C");

        // Act
        Response response = createLinkRequest("https://example.com");

        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        LinksResource.CreateLinkResponse responseBody = response.readEntity(LinksResource.CreateLinkResponse.class);
        assertEquals("http://localhost:8080/C", responseBody.getShortUrl());
        assertEquals("C", responseBody.getShortCode());

        verify(linkDAO).save(any(Link.class));
        verify(linkDAO).updateShortCode(123L, "C");
    }

    @Test
    void createShortLink_invalidUrl() {
        // Act
        Response response = createLinkRequest(null);

        // Assert
        assertEquals(422, response.getStatus());
    }

    @Test
    void createShortLink_emptyUrl() {
        // Act
        Response response = createLinkRequest("");

        // Assert
        assertEquals(422, response.getStatus());
    }

    @Test
    void createShortLink_invalidUrlFormat() {
        // Act
        Response response = createLinkRequest("invalid-url");

        // Assert
        assertEquals(422, response.getStatus());
    }
}