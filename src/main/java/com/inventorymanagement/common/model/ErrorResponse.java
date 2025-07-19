package com.inventorymanagement.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Standardized error response model for consistent error handling across the application.
 *
 * <p>This model provides a comprehensive structure for error responses that includes: - Standard
 * error information (code, message, timestamp) - Contextual information (path, method, trace ID) - Detailed validation errors - Structured error
 * details for complex scenarios
 *
 * <p>The response format follows industry standards and supports both simple and complex error
 * scenarios, making it suitable for API consumers and debugging purposes.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success = false;

    @JsonProperty("error_code")
    private String errorCode;

    private String message;

    @JsonProperty("error_details")
    private String errorDetails;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    private String path;

    private String method;

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("span_id")
    private String spanId;

    @JsonProperty("validation_errors")
    private List<ValidationError> validationErrors;

    @JsonProperty("additional_info")
    private Map<String, Object> additionalInfo;

    @JsonProperty("correlation_id")
    private String correlationId;

    private Integer status;

    // Constructors
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.validationErrors = new ArrayList<>();
    }

    public ErrorResponse(String errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorResponse(String errorCode, String message, String path) {
        this(errorCode, message);
        this.path = path;
    }

    // Static factory methods
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(errorCode, message, path);
    }

    public static ErrorResponse validationError(String message) {
        return new ErrorResponse("VALIDATION_ERROR", message);
    }

    public static ErrorResponse businessError(String code, String message) {
        return new ErrorResponse(code, message);
    }

    public static ErrorResponse systemError(String message) {
        return new ErrorResponse("SYSTEM_ERROR", message);
    }

    public static ErrorResponse notFound(String entity, Object id) {
        return new ErrorResponse(
                "ENTITY_NOT_FOUND", String.format("%s with ID %s not found", entity, id));
    }

    // Builder methods for fluent API
    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse withMethod(String method) {
        this.method = method;
        return this;
    }

    public ErrorResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ErrorResponse withSpanId(String spanId) {
        this.spanId = spanId;
        return this;
    }

    public ErrorResponse withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public ErrorResponse withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public ErrorResponse withErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    public ErrorResponse withAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public ErrorResponse addValidationError(String field, Object rejectedValue, String message) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(new ValidationError(field, rejectedValue, message));
        return this;
    }

    public ErrorResponse addValidationError(ValidationError validationError) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(validationError);
        return this;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ErrorResponse that = (ErrorResponse) obj;
        return Objects.equals(errorCode, that.errorCode)
                && Objects.equals(message, that.message)
                && Objects.equals(path, that.path)
                && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, message, path, traceId);
    }

    @Override
    public String toString() {
        return "ErrorResponse{"
                + "errorCode='"
                + errorCode
                + '\''
                + ", message='"
                + message
                + '\''
                + ", timestamp="
                + timestamp
                + ", path='"
                + path
                + '\''
                + ", traceId='"
                + traceId
                + '\''
                + '}';
    }

    /**
     * Represents a validation error for a specific field.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {

        private String field;

        @JsonProperty("rejected_value")
        private Object rejectedValue;

        private String message;

        public ValidationError() {}

        public ValidationError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        // Getters and setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ValidationError that = (ValidationError) obj;
            return Objects.equals(field, that.field)
                    && Objects.equals(rejectedValue, that.rejectedValue)
                    && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, rejectedValue, message);
        }

        @Override
        public String toString() {
            return "ValidationError{"
                    + "field='"
                    + field
                    + '\''
                    + ", rejectedValue="
                    + rejectedValue
                    + ", message='"
                    + message
                    + '\''
                    + '}';
        }
    }
}
