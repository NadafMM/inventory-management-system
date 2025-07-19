package com.inventorymanagement.common.exception;

/**
 * Base exception class for business logic violations and domain-specific errors. This exception should be thrown when business rules are violated or
 * when domain-specific conditions are not met.
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] parameters;

    /**
     * Constructs a new business exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
        this.parameters = null;
    }

    /**
     * Constructs a new business exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.parameters = null;
    }

    /**
     * Constructs a new business exception with the specified error code and message.
     *
     * @param errorCode the error code
     * @param message   the detail message
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = null;
    }

    /**
     * Constructs a new business exception with the specified error code, message, and parameters.
     *
     * @param errorCode  the error code
     * @param message    the detail message
     * @param parameters the parameters for message formatting
     */
    public BusinessException(String errorCode, String message, Object... parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    /**
     * Constructs a new business exception with the specified error code, message, cause, and parameters.
     *
     * @param errorCode  the error code
     * @param message    the detail message
     * @param cause      the cause of the exception
     * @param parameters the parameters for message formatting
     */
    public BusinessException(
            String errorCode, String message, Throwable cause, Object... parameters) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    /**
     * Gets the error code associated with this exception.
     *
     * @return the error code, or null if not set
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the parameters associated with this exception.
     *
     * @return the parameters, or null if not set
     */
    public Object[] getParameters() {
        return parameters;
    }
}
