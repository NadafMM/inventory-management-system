package com.inventorymanagement.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Documentation models for OpenAPI specification.
 *
 * <p>This class contains example models and schemas used for API documentation to provide clear
 * examples of request/response structures in Swagger UI.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
public class ApiDocumentationModels {

    /**
     * Example model for successful API responses.
     */
    @Schema(description = "Standard success response wrapper")
    public static class SuccessResponse<T> {

        @Schema(description = "Response status", example = "success")
        private String status;

        @Schema(description = "Success message", example = "Operation completed successfully")
        private String message;

        @Schema(description = "Response data")
        private T data;

        @Schema(description = "Request timestamp", example = "2025-01-15T10:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "Request path", example = "/api/v1/categories")
        private String path;

        @Schema(description = "HTTP method", example = "GET")
        private String method;

        @Schema(description = "Correlation ID for request tracking", example = "abc123-def456-ghi789")
        private String correlationId;

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
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

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }
    }

    /**
     * Example model for error responses.
     */
    @Schema(description = "Standard error response")
    public static class ErrorResponseModel {

        @Schema(description = "Error status", example = "error")
        private String status;

        @Schema(description = "Error message", example = "Validation failed")
        private String message;

        @Schema(description = "Error code", example = "VALIDATION_ERROR")
        private String errorCode;

        @Schema(description = "Error timestamp", example = "2025-01-15T10:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "Request path", example = "/api/v1/categories")
        private String path;

        @Schema(description = "HTTP method", example = "POST")
        private String method;

        @Schema(description = "HTTP status code", example = "400")
        private int status_code;

        @Schema(description = "Correlation ID for request tracking", example = "abc123-def456-ghi789")
        private String correlationId;

        @Schema(description = "Trace ID for distributed tracing", example = "trace123456")
        private String traceId;

        @Schema(description = "Span ID for distributed tracing", example = "span789012")
        private String spanId;

        @Schema(description = "Validation errors (if applicable)")
        private List<ValidationError> validationErrors;

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
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

        public int getStatus_code() {
            return status_code;
        }

        public void setStatus_code(int status_code) {
            this.status_code = status_code;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
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
    }

    /**
     * Example model for validation errors.
     */
    @Schema(description = "Validation error details")
    public static class ValidationError {

        @Schema(description = "Field name that failed validation", example = "name")
        private String field;

        @Schema(description = "Rejected value", example = "")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "Name cannot be empty")
        private String message;

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
    }

    /**
     * Example model for paginated responses.
     */
    @Schema(description = "Paginated response wrapper")
    public static class PaginatedResponse<T> {

        @Schema(description = "List of items")
        private List<T> content;

        @Schema(description = "Current page number (0-based)", example = "0")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total number of elements", example = "150")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "8")
        private int totalPages;

        @Schema(description = "Whether this is the first page", example = "true")
        private boolean first;

        @Schema(description = "Whether this is the last page", example = "false")
        private boolean last;

        @Schema(description = "Number of elements in current page", example = "20")
        private int numberOfElements;

        @Schema(description = "Whether the page is empty", example = "false")
        private boolean empty;

        @Schema(description = "Sort information")
        private Sort sort;

        // Getters and setters
        public List<T> getContent() {
            return content;
        }

        public void setContent(List<T> content) {
            this.content = content;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }

        public int getNumberOfElements() {
            return numberOfElements;
        }

        public void setNumberOfElements(int numberOfElements) {
            this.numberOfElements = numberOfElements;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }

        public Sort getSort() {
            return sort;
        }

        public void setSort(Sort sort) {
            this.sort = sort;
        }
    }

    /**
     * Example model for sort information.
     */
    @Schema(description = "Sort information")
    public static class Sort {

        @Schema(description = "Whether the result is sorted", example = "true")
        private boolean sorted;

        @Schema(description = "Whether the result is unsorted", example = "false")
        private boolean unsorted;

        @Schema(description = "Whether the result is empty", example = "false")
        private boolean empty;

        // Getters and setters
        public boolean isSorted() {
            return sorted;
        }

        public void setSorted(boolean sorted) {
            this.sorted = sorted;
        }

        public boolean isUnsorted() {
            return unsorted;
        }

        public void setUnsorted(boolean unsorted) {
            this.unsorted = unsorted;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }
    }

    /**
     * Example model for bulk operation responses.
     */
    @Schema(description = "Bulk operation response")
    public static class BulkOperationResponseModel {

        @Schema(description = "Total number of items processed", example = "100")
        private int totalProcessed;

        @Schema(description = "Number of successful operations", example = "95")
        private int successCount;

        @Schema(description = "Number of failed operations", example = "5")
        private int failureCount;

        @Schema(description = "List of successful item IDs")
        private List<Long> successfulIds;

        @Schema(description = "List of failed operations with error details")
        private List<BulkOperationError> errors;

        @Schema(description = "Operation duration in milliseconds", example = "1250")
        private long durationMs;

        // Getters and setters
        public int getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(int totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public List<Long> getSuccessfulIds() {
            return successfulIds;
        }

        public void setSuccessfulIds(List<Long> successfulIds) {
            this.successfulIds = successfulIds;
        }

        public List<BulkOperationError> getErrors() {
            return errors;
        }

        public void setErrors(List<BulkOperationError> errors) {
            this.errors = errors;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }
    }

    /**
     * Example model for bulk operation errors.
     */
    @Schema(description = "Bulk operation error details")
    public static class BulkOperationError {

        @Schema(description = "Item ID that failed", example = "123")
        private Long itemId;

        @Schema(description = "Error message", example = "Category name already exists")
        private String error;

        @Schema(description = "Error code", example = "DUPLICATE_NAME")
        private String errorCode;

        // Getters and setters
        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }
}
