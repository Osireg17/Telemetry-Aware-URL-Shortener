package com.urlshortener.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class EventValidatorTest {

    private EventValidator validator;
    private ObjectMapper objectMapper;
    private Validator validatorInstance;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validatorInstance = factory.getValidator();
        validator = new EventValidator(objectMapper, validatorInstance);
    }

    @Test
    void testValidClickEvent() throws ValidationException, JsonProcessingException {
        String validJson = """
            {
                "linkId": 123,
                "shortCode": "abc123",
                "timestamp": "2025-08-24T10:30:00Z",
                "userAgent": "Mozilla/5.0",
                "ipAddress": "192.168.1.1",
                "referer": "https://example.com"
            }
            """;

        assertTrue(validator.isValidClickEvent(validJson));
        ClickEvent event = validator.parseAndValidate(validJson);
        assertEquals(123L, event.getLinkId());
        assertEquals("abc123", event.getShortCode());
        assertEquals("2025-08-24T10:30:00Z", event.getTimestamp());
    }

    @Test
    void testInvalidLinkId() {
        String invalidJson = """
            {
                "linkId": -1,
                "shortCode": "abc123",
                "timestamp": "2025-08-24T10:30:00Z"
            }
            """;

        assertFalse(validator.isValidClickEvent(invalidJson));
        assertThrows(ValidationException.class, () -> validator.parseAndValidate(invalidJson));
    }

    @Test
    void testMissingShortCode() {
        String invalidJson = """
            {
                "linkId": 123,
                "timestamp": "2025-08-24T10:30:00Z"
            }
            """;

        assertFalse(validator.isValidClickEvent(invalidJson));
        assertThrows(ValidationException.class, () -> validator.parseAndValidate(invalidJson));
    }

    @Test
    void testInvalidTimestamp() {
        String invalidJson = """
            {
                "linkId": 123,
                "shortCode": "abc123",
                "timestamp": "invalid-timestamp"
            }
            """;

        assertFalse(validator.isValidClickEvent(invalidJson));
        assertThrows(ValidationException.class, () -> validator.parseAndValidate(invalidJson));
    }
}
