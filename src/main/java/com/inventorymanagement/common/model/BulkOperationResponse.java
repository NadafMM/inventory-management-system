package com.inventorymanagement.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bulk operation response wrapper for batch operation results. Provides detailed information about success and failure counts.
 *
 * @param <T> the type of data being processed
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkOperationResponse<T> {

    @JsonProperty("total_items")
    private int totalItems;

    @JsonProperty("successful_items")
    private int successfulItems;

    @JsonProperty("failed_items")
    private int failedItems;

    @JsonProperty("success_rate")
    private double successRate;

    private List<T> results;

    private List<BulkOperationError> errors;

    @JsonProperty("execution_time_ms")
    private long executionTimeMs;

    // Constructors
    public BulkOperationResponse() {
        this.results = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public BulkOperationResponse(int totalItems, int successfulItems, int failedItems) {
        this();
        this.totalItems = totalItems;
        this.successfulItems = successfulItems;
        this.failedItems = failedItems;
        this.successRate = totalItems > 0 ? (double) successfulItems / totalItems * 100 : 0.0;
    }

    // Static factory methods
    public static <T> BulkOperationResponse<T> success(List<T> results) {
        BulkOperationResponse<T> response =
                new BulkOperationResponse<>(results.size(), results.size(), 0);
        response.results = results;
        return response;
    }

    public static <T> BulkOperationResponse<T> partial(
            List<T> results, List<BulkOperationError> errors) {
        BulkOperationResponse<T> response =
                new BulkOperationResponse<>(results.size() + errors.size(), results.size(), errors.size());
        response.results = results;
        response.errors = errors;
        return response;
    }

    public static <T> BulkOperationResponse<T> failure(List<BulkOperationError> errors) {
        BulkOperationResponse<T> response =
                new BulkOperationResponse<>(errors.size(), 0, errors.size());
        response.errors = errors;
        return response;
    }

    // Helper methods
    public void addResult(T result) {
        this.results.add(result);
        this.successfulItems++;
        this.totalItems++;
        recalculateSuccessRate();
    }

    public void addError(BulkOperationError error) {
        this.errors.add(error);
        this.failedItems++;
        this.totalItems++;
        recalculateSuccessRate();
    }

    private void recalculateSuccessRate() {
        this.successRate = totalItems > 0 ? (double) successfulItems / totalItems * 100 : 0.0;
    }

    // Getters and setters
    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getSuccessfulItems() {
        return successfulItems;
    }

    public void setSuccessfulItems(int successfulItems) {
        this.successfulItems = successfulItems;
    }

    public int getFailedItems() {
        return failedItems;
    }

    public void setFailedItems(int failedItems) {
        this.failedItems = failedItems;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public List<BulkOperationError> getErrors() {
        return errors;
    }

    public void setErrors(List<BulkOperationError> errors) {
        this.errors = errors;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BulkOperationResponse<?> that = (BulkOperationResponse<?>) obj;
        return totalItems == that.totalItems
                && successfulItems == that.successfulItems
                && failedItems == that.failedItems
                && Double.compare(that.successRate, successRate) == 0
                && Objects.equals(results, that.results)
                && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalItems, successfulItems, failedItems, successRate, results, errors);
    }

    @Override
    public String toString() {
        return "BulkOperationResponse{"
                + "totalItems="
                + totalItems
                + ", successfulItems="
                + successfulItems
                + ", failedItems="
                + failedItems
                + ", successRate="
                + successRate
                + ", results="
                + results
                + ", errors="
                + errors
                + ", executionTimeMs="
                + executionTimeMs
                + '}';
    }

    /**
     * Represents an error that occurred during bulk operation processing.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BulkOperationError {

        private int index;
        private String message;

        @JsonProperty("error_code")
        private String errorCode;

        private Object item;

        public BulkOperationError() {}

        public BulkOperationError(int index, String message, String errorCode, Object item) {
            this.index = index;
            this.message = message;
            this.errorCode = errorCode;
            this.item = item;
        }

        // Getters and setters
        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
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

        public Object getItem() {
            return item;
        }

        public void setItem(Object item) {
            this.item = item;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BulkOperationError that = (BulkOperationError) obj;
            return index == that.index
                    && Objects.equals(message, that.message)
                    && Objects.equals(errorCode, that.errorCode)
                    && Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, message, errorCode, item);
        }

        @Override
        public String toString() {
            return "BulkOperationError{"
                    + "index="
                    + index
                    + ", message='"
                    + message
                    + '\''
                    + ", errorCode='"
                    + errorCode
                    + '\''
                    + ", item="
                    + item
                    + '}';
        }
    }
}
