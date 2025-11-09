package com.urlshortener.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.urlshortener.core.Click;
import com.urlshortener.db.ClickDAO;

import jakarta.servlet.http.HttpServletRequest;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClickManagerTest {

    @Mock
    private ClickDAO clickDAO;
    private LinkManager linkManager;
    private ClickManager clickManager;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        clickDAO = mock(ClickDAO.class);
        linkManager = mock(LinkManager.class);
        clickManager = new ClickManager(clickDAO, linkManager);
        request = mock(HttpServletRequest.class);
    }

    @Test
    void recordClick_successfullyRecordsClickAndIncrementsCount() {
        // Arrange
        long linkId = 123L;
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getHeader("Referer")).thenReturn("https://example.com");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        // Act
        clickManager.recordClick(linkId, request);

        // Assert
        ArgumentCaptor<Click> clickCaptor = ArgumentCaptor.forClass(Click.class);
        verify(clickDAO).save(clickCaptor.capture());

        Click capturedClick = clickCaptor.getValue();
        assertEquals(linkId, capturedClick.getLinkId());
        assertEquals("Mozilla/5.0", capturedClick.getUserAgent());
        assertEquals("192.168.1.1", capturedClick.getIpAddress());
        assertEquals("https://example.com", capturedClick.getReferer());

        verify(linkManager).incrementClickCount(linkId);
    }

    @Test
    void recordClick_whenDAOSaveFails_doesNotIncrementClickCount() {
        // Arrange
        long linkId = 456L;
        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        doThrow(new RuntimeException("Database error")).when(clickDAO).save(any(Click.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            clickManager.recordClick(linkId, request);
        });

        verify(clickDAO).save(any(Click.class));
        verify(linkManager, never()).incrementClickCount(anyLong());
    }
}
