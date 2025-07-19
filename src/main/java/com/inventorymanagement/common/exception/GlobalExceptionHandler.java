package com.inventorymanagement.common.exception;

import com.inventorymanagement.common.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the Inventory Management System.
 *
 * <p>This class provides centralized exception handling across the entire application, ensuring
 * consistent error responses and proper logging for all types of exceptions.
 *
 * <p>Key features: - Comprehensive exception coverage (validation, business, system) - Structured
 * error responses with trace information - Contextual logging with MDC for correlation
 *
 * <p>The handler follows industry best practices for error handling and observability, providing
 * detailed information for debugging while maintaining security by not exposing sensitive information in production.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred";

    /**
     * Handles validation exceptions from Bean Validation (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError("Validation failed for one or more fields")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        // Add field-specific validation errors
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(
                    fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
        }

        addTraceInfo(errorResponse);

        logger.warn(
                "Validation error occurred: {} validation errors for request to {}",
                ex.getBindingResult().getErrorCount(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles constraint violation exceptions from method-level validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError("Constraint validation failed")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        // Add constraint violations
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            errorResponse.addValidationError(
                    fieldName, violation.getInvalidValue(), violation.getMessage());
        }

        addTraceInfo(errorResponse);

        logger.warn(
                "Constraint violation error: {} violations for request to {}",
                violations.size(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles validation errors for request parameters and path variables.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError("Validation failed for one or more parameters")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        // Process parameter validation errors
        for (var parameterError : ex.getAllErrors()) {
            String fieldName =
                    parameterError.getCodes() != null && parameterError.getCodes().length > 0
                            ? parameterError.getCodes()[0]
                            : "parameter";
            Object rejectedValue =
                    parameterError.getArguments() != null && parameterError.getArguments().length > 0
                            ? parameterError.getArguments()[0]
                            : null;
            String message = parameterError.getDefaultMessage();

            errorResponse.addValidationError(fieldName, rejectedValue, message);
        }

        addTraceInfo(errorResponse);

        logger.warn(
                "Parameter validation error occurred: {} validation errors for request to {}",
                ex.getAllErrors().size(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles custom validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomValidationException(
            ValidationException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError(ex.getMessage())
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        // Add field errors if available
        if (ex.hasFieldErrors()) {
            ex.getFieldErrors()
                    .forEach((field, message) -> errorResponse.addValidationError(field, null, message));
        }

        addTraceInfo(errorResponse);

        logger.warn(
                "Custom validation error: {} for request to {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles entity not found exceptions.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.notFound(ex.getEntityType(), ex.getEntityId())
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn("Entity not found: {} for request to {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles insufficient stock exceptions.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.businessError("INSUFFICIENT_STOCK", ex.getMessage())
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.CONFLICT.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Insufficient stock error: {} for request to {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles general business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.businessError(ex.getErrorCode(), ex.getMessage())
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Business exception: {} for request to {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * Handles HTTP method not supported exceptions.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.of(
                                "METHOD_NOT_SUPPORTED",
                                String.format(
                                        "HTTP method '%s' is not supported for this endpoint", ex.getMethod()))
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.METHOD_NOT_ALLOWED.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Method not supported: {} for request to {}", ex.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handles HTTP media type not supported exceptions.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.of(
                                "UNSUPPORTED_MEDIA_TYPE",
                                String.format("Content-Type '%s' is not supported", ex.getContentType()))
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Unsupported media type: {} for request to {}",
                ex.getContentType(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    /**
     * Handles missing request parameter exceptions.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError(
                                String.format("Required parameter '%s' is missing", ex.getParameterName()))
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Missing parameter: {} for request to {}", ex.getParameterName(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError(
                                String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()))
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Type mismatch: {} for parameter {} in request to {}",
                ex.getValue(),
                ex.getName(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles HTTP message not readable exceptions.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.validationError("Invalid request body format")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn("Invalid request body for request to {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles database access exceptions.
     */
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            Exception ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.systemError("Database operation failed")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.error(
                "Database error for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles data integrity violation exceptions.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.businessError(
                                "DATA_INTEGRITY_VIOLATION", "Data integrity constraint violation")
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.CONFLICT.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.warn(
                "Data integrity violation for request to {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles all other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String correlationId = setupErrorContext(request, ex);

        ErrorResponse errorResponse =
                ErrorResponse.systemError(DEFAULT_ERROR_MESSAGE)
                        .withPath(request.getRequestURI())
                        .withMethod(request.getMethod())
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withCorrelationId(correlationId);

        addTraceInfo(errorResponse);

        logger.error(
                "Unhandled exception for request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Sets up error context for logging and tracing.
     */
    private String setupErrorContext(HttpServletRequest request, Exception ex) {
        String correlationId = getOrCreateCorrelationId(request);

        // Set up MDC for structured logging
        MDC.put("correlationId", correlationId);
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        MDC.put("exceptionType", ex.getClass().getSimpleName());

        if (request.getRemoteUser() != null) {
            MDC.put("userId", request.getRemoteUser());
        }

        return correlationId;
    }

    /**
     * Adds trace information to the error response.
     */
    private void addTraceInfo(ErrorResponse errorResponse) {
        // Removed tracer and trace context as they are no longer imported
    }

    /**
     * Gets or creates a correlation ID for the request.
     */
    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
