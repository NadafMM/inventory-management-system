package com.inventorymanagement.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Generic API response wrapper for consistent response format across all endpoints. Provides a standard structure for both success and error
 * responses.
 *
 * @param <T> the type of data being returned
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;

    @JsonProperty("error_code")
    private String errorCode;

    private LocalDateTime timestamp;
    private String path;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, String path) {
        this(success, message, data);
        this.path = path;
    }

    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation completed successfully", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        return new ApiResponse<>(true, message, data, path);
    }

    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, null, null);
        response.error = message;
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = error(message);
        response.errorCode = errorCode;
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        ApiResponse<T> response = error(message, errorCode);
        response.path = path;
        return response;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ApiResponse<?> that = (ApiResponse<?>) obj;
        return success == that.success
                && Objects.equals(message, that.message)
                && Objects.equals(data, that.data)
                && Objects.equals(error, that.error)
                && Objects.equals(errorCode, that.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, data, error, errorCode);
    }

    @Override
    public String toString() {
        return "ApiResponse{"
                + "success="
                + success
                + ", message='"
                + message
                + '\''
                + ", data="
                + data
                + ", error='"
                + error
                + '\''
                + ", errorCode='"
                + errorCode
                + '\''
                + ", timestamp="
                + timestamp
                + ", path='"
                + path
                + '\''
                + '}';
    }
}
