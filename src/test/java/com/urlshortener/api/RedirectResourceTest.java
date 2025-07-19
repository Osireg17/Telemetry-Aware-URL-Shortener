package com.urlshortener.api;


import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RedirectResourceTest {

	private final LinkDAO linkDAO = mock(LinkDAO.class);
	private final RedirectResource redirectResource = new RedirectResource(linkDAO);

	@Test
	void redirect_happyPath() {
		// Arrange
		String shortCode = "C";
		String longUrl = "https://example.com";
		Link link = new Link(longUrl, shortCode);
		when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.of(link));

		// Act
		Response response = redirectResource.redirect(shortCode);

		// Assert
		assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatus());
		assertEquals(longUrl, response.getLocation().toString());
	}

	@Test
	void redirect_notFound() {
		// Arrange
		String shortCode = "non-existent";
		when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.empty());

		// Act
		Response response = redirectResource.redirect(shortCode);

		// Assert
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
}
