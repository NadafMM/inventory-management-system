package com.inventorymanagement.common.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation of business rules fails. This exception can carry multiple validation errors with field-specific messages.
 */
public class ValidationException extends BusinessException {

    private final Map<String, String> fieldErrors;

    /**
     * Constructs a new validation exception with the specified message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Constructs a new validation exception with the specified field and error message.
     *
     * @param field   the field that failed validation
     * @param message the error message for the field
     */
    public ValidationException(String field, String message) {
        super(
                "VALIDATION_ERROR", String.format("Validation failed for field '%s': %s", field, message));
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, message);
    }

    /**
     * Constructs a new validation exception with the specified field errors.
     *
     * @param fieldErrors map of field names to error messages
     */
    public ValidationException(Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", "Validation failed for multiple fields");
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    /**
     * Adds a field error to this validation exception.
     *
     * @param field   the field name
     * @param message the error message
     */
    public void addFieldError(String field, String message) {
        this.fieldErrors.put(field, message);
    }

    /**
     * Gets the field errors associated with this validation exception.
     *
     * @return map of field names to error messages
     */
    public Map<String, String> getFieldErrors() {
        return new HashMap<>(fieldErrors);
    }

    /**
     * Checks if this validation exception has any field errors.
     *
     * @return true if there are field errors, false otherwise
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
