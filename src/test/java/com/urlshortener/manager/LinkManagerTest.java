package com.urlshortener.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.urlshortener.UrlShortenerConfiguration;
import com.urlshortener.core.Base62Service;
import com.urlshortener.core.Link;
import com.urlshortener.db.LinkDAO;
import com.urlshortener.models.LinkCreationError;
import com.urlshortener.models.LinkCreationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LinkManagerTest {

    private LinkManager linkManager;

    @Mock
    private LinkDAO linkDAO;

    @Mock
    private Base62Service base62Service;

    @Mock
    private UrlShortenerConfiguration.ApplicationConfiguration appConfig;

    @BeforeEach
    void setUp() {
        linkManager = new LinkManager(linkDAO, base62Service, appConfig);
    }

    @Test
    void itCanCreateLink() {
        // Given
        String longUrl = "https://example.com/very/long/url";
        long generatedId = 12345L;
        String expectedShortCode = "abc123";

        when(appConfig.getBaseUrl()).thenReturn("http://short.url");
        when(linkDAO.save(any(Link.class))).thenReturn(generatedId);
        when(base62Service.encode(generatedId)).thenReturn(expectedShortCode);

        // When
        LinkCreationResult result = linkManager.createLink(longUrl, null);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("http://short.url/" + expectedShortCode, result.getShortUrl());
        assertEquals(expectedShortCode, result.getShortCode());
        verify(linkDAO, times(1)).save(any(Link.class));
        verify(linkDAO, times(1)).updateShortCode(generatedId, expectedShortCode);
    }

    @Test
    void itCanCreateLinkWithCustomShortCode() {
        // Given
        String longUrl = "https://example.com/very/long/url";
        String customShortCode = "mycode";

        when(appConfig.getBaseUrl()).thenReturn("http://short.url");
        when(appConfig.getMaxCustomShortCodeLength()).thenReturn(20);
        when(linkDAO.findByShortCode(customShortCode)).thenReturn(Optional.empty());
        when(linkDAO.save(any(Link.class))).thenReturn(1L);

        // When
        LinkCreationResult result = linkManager.createLink(longUrl, customShortCode);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("http://short.url/" + customShortCode, result.getShortUrl());
        assertEquals(customShortCode, result.getShortCode());
        verify(linkDAO, times(1)).save(any(Link.class));
        verify(linkDAO, never()).updateShortCode(anyLong(), anyString());
    }

    @Test
    void itRejectsCustomShortCodeThatIsTooLong() {
        // Given
        String longUrl = "https://example.com/very/long/url";
        String customShortCode = "thiscodeiswaylongerthantwentycharacters";

        when(appConfig.getMaxCustomShortCodeLength()).thenReturn(20);

        // When
        LinkCreationResult result = linkManager.createLink(longUrl, customShortCode);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(LinkCreationError.CUSTOM_CODE_TOO_LONG, result.getError());
        assertTrue(result.getErrorMessage().contains("exceeds maximum length"));
        verify(linkDAO, never()).save(any(Link.class));
    }

    @Test
    void itRejectsCustomShortCodeThatAlreadyExists() {
        // Given
        String longUrl = "https://example.com/very/long/url";
        String customShortCode = "existing";
        Link existingLink = new Link("https://other.com", customShortCode);

        when(appConfig.getMaxCustomShortCodeLength()).thenReturn(20);
        when(linkDAO.findByShortCode(customShortCode)).thenReturn(Optional.of(existingLink));

        // When
        LinkCreationResult result = linkManager.createLink(longUrl, customShortCode);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(LinkCreationError.CUSTOM_CODE_ALREADY_EXISTS, result.getError());
        assertEquals("Custom URL is already taken.", result.getErrorMessage());
        verify(linkDAO, never()).save(any(Link.class));
    }

    @Test
    void itCanFindByShortCode() {
        // Given
        String shortCode = "abc123";
        Link expectedLink = new Link("https://example.com", shortCode);
        when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.of(expectedLink));

        // When
        Optional<Link> result = linkManager.findByShortCode(shortCode);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedLink, result.get());
        verify(linkDAO, times(1)).findByShortCode(shortCode);
    }

    @Test
    void itReturnsEmptyWhenShortCodeNotFound() {
        // Given
        String shortCode = "nonexistent";
        when(linkDAO.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // When
        Optional<Link> result = linkManager.findByShortCode(shortCode);

        // Then
        assertFalse(result.isPresent());
        verify(linkDAO, times(1)).findByShortCode(shortCode);
    }

    @Test
    void itCanIncrementClickCount() {
        // Given
        long linkId = 12345L;

        // When
        linkManager.incrementClickCount(linkId);

        // Then
        verify(linkDAO, times(1)).incrementClickCount(linkId);
    }
}
