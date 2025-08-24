package com.urlshortener.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.Set;

public class EventValidator {
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public EventValidator() {
        this.objectMapper = new ObjectMapper();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public EventValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
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

        try {
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

            // Additional custom validations
            validateLinkId(clickEvent.getLinkId());
            validateShortCode(clickEvent.getShortCode());
            validateTimestamp(clickEvent.getTimestamp());

            return clickEvent;

        } catch (Exception e) {
            if (e instanceof ValidationException) {
              try {
                throw e;
              } catch (JsonProcessingException | ValidationException ex) {
                throw new RuntimeException(ex);
              }
            }
            throw new ValidationException("Failed to parse JSON message: " + e.getMessage(), e);
        }
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
            // Validate ISO-8601 format
            OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            try {
                // Try parsing as ISO_LOCAL_DATE_TIME with 'Z' suffix
                if (timestamp.endsWith("Z")) {
                    OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_INSTANT);
                } else {
                    // Try parsing as local date time
                    OffsetDateTime.parse(timestamp + "Z", DateTimeFormatter.ISO_INSTANT);
                }
            } catch (Exception e2) {
                throw new ValidationException("Timestamp must be in ISO-8601 format, got: " + timestamp, e);
            }
        }
    }
}
