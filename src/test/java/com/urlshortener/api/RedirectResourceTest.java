package com.urlshortener.api;

import com.urlshortener.core.Link;
import com.urlshortener.db.ClickDAO;
import com.urlshortener.db.LinkDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RedirectResourceTest {

	private final LinkDAO linkDAO = mock(LinkDAO.class);
	private final ClickDAO clickDAO = mock(ClickDAO.class);
	private final HttpServletRequest request = mock(HttpServletRequest.class);
	private final RedirectResource redirectResource = new RedirectResource(linkDAO, clickDAO);

	@Test
	void redirect_happyPath() {
		// Arrange
		String shortCode = "C";
		String longUrl = "https://example.com";
		Link link = new Link(longUrl, shortCode);
		link.setId(1L);
		when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.of(link));
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
		when(request.getHeader("Referer")).thenReturn("https://referer.com");
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		// Act
		Response response = redirectResource.redirect(shortCode, request);

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
		Response response = redirectResource.redirect(shortCode, request);

		// Assert
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	void redirect_capturesTelemetryWithXForwardedFor() {
		// Arrange
		String shortCode = "C";
		String longUrl = "https://example.com";
		Link link = new Link(longUrl, shortCode);
		link.setId(1L);
		when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.of(link));
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
		when(request.getHeader("Referer")).thenReturn("https://referer.com");
		when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		// Act
		Response response = redirectResource.redirect(shortCode, request);

		// Assert
		assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatus());
		assertEquals(longUrl, response.getLocation().toString());
		
		// Allow time for async telemetry processing
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		// Verify telemetry was captured (may need verification after async completion)
		verify(clickDAO, timeout(1000)).save(any());
		verify(linkDAO, timeout(1000)).incrementClickCount(1L);
	}

	@Test
	void redirect_capturesTelemetryWithXRealIP() {
		// Arrange
		String shortCode = "C";
		String longUrl = "https://example.com";
		Link link = new Link(longUrl, shortCode);
		link.setId(1L);
		when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.of(link));
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
		when(request.getHeader("Referer")).thenReturn("https://referer.com");
		when(request.getHeader("X-Forwarded-For")).thenReturn(null);
		when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		// Act
		Response response = redirectResource.redirect(shortCode, request);

		// Assert
		assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatus());
		assertEquals(longUrl, response.getLocation().toString());
		
		// Verify telemetry was captured
		verify(clickDAO, timeout(1000)).save(any());
		verify(linkDAO, timeout(1000)).incrementClickCount(1L);
	}
}
