package com.urlshortener.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.urlshortener.core.Link;
import com.urlshortener.events.ClickEvent;
import com.urlshortener.kafka.EventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClickManagerTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private HttpServletRequest request;

    private ClickManager clickManager;

    @BeforeEach
    void setUp() {
        clickManager = new ClickManager(eventPublisher);
    }

    @Test
    void recordClick_successfullyPublishesClickEvent() throws Exception {
        // Given
        Link link = new Link("https://example.com/long-url", "abc123");
        link.setId(123L);

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getHeader("Referer")).thenReturn("https://referrer.com");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        // When
        clickManager.recordClick(link, request);

        // Then
        ArgumentCaptor<ClickEvent> eventCaptor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(eventPublisher).publishClickEvent(eventCaptor.capture());

        ClickEvent capturedEvent = eventCaptor.getValue();
        assertEquals(123L, capturedEvent.getLinkId());
        assertEquals("abc123", capturedEvent.getShortCode());
        assertEquals("Mozilla/5.0", capturedEvent.getUserAgent());
        assertEquals("192.168.1.1", capturedEvent.getIpAddress());
        assertEquals("https://referrer.com", capturedEvent.getReferer());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void recordClick_extractsIpFromXRealIp() throws Exception {
        // Given
        Link link = new Link("https://example.com", "xyz789");
        link.setId(456L);

        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.1");

        // When
        clickManager.recordClick(link, request);

        // Then
        ArgumentCaptor<ClickEvent> eventCaptor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(eventPublisher).publishClickEvent(eventCaptor.capture());

        ClickEvent capturedEvent = eventCaptor.getValue();
        assertEquals("203.0.113.1", capturedEvent.getIpAddress());
    }

    @Test
    void recordClick_extractsIpFromRemoteAddr() throws Exception {
        // Given
        Link link = new Link("https://example.com", "def456");
        link.setId(789L);

        when(request.getHeader("User-Agent")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("198.51.100.1");

        // When
        clickManager.recordClick(link, request);

        // Then
        ArgumentCaptor<ClickEvent> eventCaptor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(eventPublisher).publishClickEvent(eventCaptor.capture());

        ClickEvent capturedEvent = eventCaptor.getValue();
        assertEquals("198.51.100.1", capturedEvent.getIpAddress());
    }

    @Test
    void recordClick_whenEventPublisherFails_propagatesException() throws Exception {
        // Given
        Link link = new Link("https://example.com", "err123");
        link.setId(999L);

        when(request.getHeader("User-Agent")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        doThrow(new RuntimeException("Kafka is down")).when(eventPublisher).publishClickEvent(any(ClickEvent.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            clickManager.recordClick(link, request);
        });

        verify(eventPublisher).publishClickEvent(any(ClickEvent.class));
    }

    @Test
    void recordClick_handlesNullHeaders() throws Exception {
        // Given
        Link link = new Link("https://example.com", "null123");
        link.setId(111L);

        when(request.getHeader("User-Agent")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        clickManager.recordClick(link, request);

        // Then
        ArgumentCaptor<ClickEvent> eventCaptor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(eventPublisher).publishClickEvent(eventCaptor.capture());

        ClickEvent capturedEvent = eventCaptor.getValue();
        assertNull(capturedEvent.getUserAgent());
        assertNull(capturedEvent.getReferer());
        assertEquals("127.0.0.1", capturedEvent.getIpAddress());
    }
}
