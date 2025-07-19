package com.inventorymanagement.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * Bulk operation request wrapper for batch operations. Supports bulk create, update, and delete operations.
 *
 * @param <T> the type of data being processed
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkOperationRequest<T> {

    @NotEmpty(message = "Items list cannot be empty")
    @Size(max = 100, message = "Maximum 100 items allowed per bulk operation")
    @Valid
    private List<T> items;

    private BulkOperationOptions options;

    // Constructors
    public BulkOperationRequest() {}

    public BulkOperationRequest(List<T> items) {
        this.items = items;
    }

    public BulkOperationRequest(List<T> items, BulkOperationOptions options) {
        this.items = items;
        this.options = options;
    }

    // Getters and setters
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public BulkOperationOptions getOptions() {
        return options;
    }

    public void setOptions(BulkOperationOptions options) {
        this.options = options;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BulkOperationRequest<?> that = (BulkOperationRequest<?>) obj;
        return Objects.equals(items, that.items) && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, options);
    }

    @Override
    public String toString() {
        return "BulkOperationRequest{" + "items=" + items + ", options=" + options + '}';
    }

    /**
     * Options for bulk operations.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BulkOperationOptions {

        private boolean continueOnError = false;
        private boolean validateAll = true;
        private boolean returnDetails = false;
        private Integer batchSize = 10;

        public BulkOperationOptions() {}

        public BulkOperationOptions(
                boolean continueOnError, boolean validateAll, boolean returnDetails, Integer batchSize) {
            this.continueOnError = continueOnError;
            this.validateAll = validateAll;
            this.returnDetails = returnDetails;
            this.batchSize = batchSize;
        }

        // Getters and setters
        public boolean isContinueOnError() {
            return continueOnError;
        }

        public void setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
        }

        public boolean isValidateAll() {
            return validateAll;
        }

        public void setValidateAll(boolean validateAll) {
            this.validateAll = validateAll;
        }

        public boolean isReturnDetails() {
            return returnDetails;
        }

        public void setReturnDetails(boolean returnDetails) {
            this.returnDetails = returnDetails;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(Integer batchSize) {
            this.batchSize = batchSize;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BulkOperationOptions that = (BulkOperationOptions) obj;
            return continueOnError == that.continueOnError
                    && validateAll == that.validateAll
                    && returnDetails == that.returnDetails
                    && Objects.equals(batchSize, that.batchSize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(continueOnError, validateAll, returnDetails, batchSize);
        }

        @Override
        public String toString() {
            return "BulkOperationOptions{"
                    + "continueOnError="
                    + continueOnError
                    + ", validateAll="
                    + validateAll
                    + ", returnDetails="
                    + returnDetails
                    + ", batchSize="
                    + batchSize
                    + '}';
        }
    }
}
