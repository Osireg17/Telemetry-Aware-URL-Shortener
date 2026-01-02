package com.urlshortener.events;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

public class EventValidator {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public EventValidator(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public boolean isValidClickEvent(String jsonMessage) {
        try {
            parseAndValidate(jsonMessage);
            return true;
        } catch (ValidationException | JsonProcessingException e) {
            return false;
        }
    }

    public ClickEvent parseAndValidate(String jsonMessage) throws ValidationException, JsonProcessingException {
        if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
            throw new ValidationException("JSON message cannot be null or empty");
        }

        // Parse JSON to ClickEvent object
        ClickEvent clickEvent = objectMapper.readValue(jsonMessage, ClickEvent.class);

        // Validate using Jakarta validation annotations
        Set<ConstraintViolation<ClickEvent>> violations = validator.validate(clickEvent);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation failed: ");
            for (ConstraintViolation<ClickEvent> violation : violations) {
                sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            throw new ValidationException(sb.toString());
        }

        validateLinkId(clickEvent.getLinkId());
        validateShortCode(clickEvent.getShortCode());
        validateTimestamp(clickEvent.getTimestamp());

        return clickEvent;
    }

    private void validateLinkId(Long linkId) throws ValidationException {
        if (linkId == null) {
            throw new ValidationException("LinkId cannot be null");
        }
        if (linkId <= 0) {
            throw new ValidationException("LinkId must be a positive number, got: " + linkId);
        }
    }

    private void validateShortCode(String shortCode) throws ValidationException {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            throw new ValidationException("ShortCode cannot be null or empty");
        }
    }

    private void validateTimestamp(String timestamp) throws ValidationException {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new ValidationException("Timestamp cannot be null or empty");
        }

        try {
            // Validate ISO-8601 format - try multiple common formats
            OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e1) {
            if (timestamp.endsWith("Z")) {
                try {
                    OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_INSTANT);
                    return;
                } catch (Exception e2) {
                    // Fall through to error
                }
            }
            throw new ValidationException("Timestamp must be in ISO-8601 format, got: " + timestamp, e1);
        }
    }
}
