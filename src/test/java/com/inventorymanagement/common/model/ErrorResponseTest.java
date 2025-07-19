package com.inventorymanagement.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for ErrorResponse and ValidationError to achieve maximum code coverage
 */
@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ErrorResponse with default constructor")
        void shouldCreateWithDefaultConstructor() {
            ErrorResponse response = new ErrorResponse();

            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getValidationErrors()).isNotNull().isEmpty();
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getMessage()).isNull();
        }

        @Test
        @DisplayName("Should create ErrorResponse with error code and message")
        void shouldCreateWithErrorCodeAndMessage() {
            String errorCode = "TEST_ERROR";
            String message = "Test error message";
            ErrorResponse response = new ErrorResponse(errorCode, message);

            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getValidationErrors()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should create ErrorResponse with error code, message, and path")
        void shouldCreateWithErrorCodeMessageAndPath() {
            String errorCode = "TEST_ERROR";
            String message = "Test error message";
            String path = "/api/test";
            ErrorResponse response = new ErrorResponse(errorCode, message, path);

            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getPath()).isEqualTo(path);
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {

        @Test
        @DisplayName("Should create ErrorResponse with of method")
        void shouldCreateWithOfMethod() {
            String errorCode = "TEST_ERROR";
            String message = "Test message";
            ErrorResponse response = ErrorResponse.of(errorCode, message);

            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create ErrorResponse with of method including path")
        void shouldCreateWithOfMethodIncludingPath() {
            String errorCode = "TEST_ERROR";
            String message = "Test message";
            String path = "/api/test";
            ErrorResponse response = ErrorResponse.of(errorCode, message, path);

            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("Should create validation error")
        void shouldCreateValidationError() {
            String message = "Validation failed";
            ErrorResponse response = ErrorResponse.validationError(message);

            assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create business error")
        void shouldCreateBusinessError() {
            String code = "BUSINESS_RULE_VIOLATION";
            String message = "Business rule violated";
            ErrorResponse response = ErrorResponse.businessError(code, message);

            assertThat(response.getErrorCode()).isEqualTo(code);
            assertThat(response.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create system error")
        void shouldCreateSystemError() {
            String message = "System error occurred";
            ErrorResponse response = ErrorResponse.systemError(message);

            assertThat(response.getErrorCode()).isEqualTo("SYSTEM_ERROR");
            assertThat(response.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should create not found error")
        void shouldCreateNotFoundError() {
            String entity = "User";
            Long id = 123L;
            ErrorResponse response = ErrorResponse.notFound(entity, id);

            assertThat(response.getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
            assertThat(response.getMessage()).isEqualTo("User with ID 123 not found");
        }

        @Test
        @DisplayName("Should create not found error with string ID")
        void shouldCreateNotFoundErrorWithStringId() {
            String entity = "Product";
            String id = "ABC123";
            ErrorResponse response = ErrorResponse.notFound(entity, id);

            assertThat(response.getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
            assertThat(response.getMessage()).isEqualTo("Product with ID ABC123 not found");
        }
    }

    @Nested
    @DisplayName("Builder Methods Tests")
    class BuilderMethodsTests {

        @Test
        @DisplayName("Should build response with path")
        void shouldBuildResponseWithPath() {
            String path = "/api/test";
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withPath(path);

            assertThat(response.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("Should build response with method")
        void shouldBuildResponseWithMethod() {
            String method = "POST";
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withMethod(method);

            assertThat(response.getMethod()).isEqualTo(method);
        }

        @Test
        @DisplayName("Should build response with trace ID")
        void shouldBuildResponseWithTraceId() {
            String traceId = "trace-123";
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withTraceId(traceId);

            assertThat(response.getTraceId()).isEqualTo(traceId);
        }

        @Test
        @DisplayName("Should build response with span ID")
        void shouldBuildResponseWithSpanId() {
            String spanId = "span-456";
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withSpanId(spanId);

            assertThat(response.getSpanId()).isEqualTo(spanId);
        }

        @Test
        @DisplayName("Should build response with status")
        void shouldBuildResponseWithStatus() {
            Integer status = 400;
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withStatus(status);

            assertThat(response.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should build response with correlation ID")
        void shouldBuildResponseWithCorrelationId() {
            String correlationId = "corr-789";
            ErrorResponse response =
                    ErrorResponse.of("ERROR", "message").withCorrelationId(correlationId);

            assertThat(response.getCorrelationId()).isEqualTo(correlationId);
        }

        @Test
        @DisplayName("Should build response with error details")
        void shouldBuildResponseWithErrorDetails() {
            String errorDetails = "Detailed error information";
            ErrorResponse response = ErrorResponse.of("ERROR", "message").withErrorDetails(errorDetails);

            assertThat(response.getErrorDetails()).isEqualTo(errorDetails);
        }

        @Test
        @DisplayName("Should build response with additional info")
        void shouldBuildResponseWithAdditionalInfo() {
            Map<String, Object> additionalInfo = Map.of("key1", "value1", "key2", 123);
            ErrorResponse response =
                    ErrorResponse.of("ERROR", "message").withAdditionalInfo(additionalInfo);

            assertThat(response.getAdditionalInfo()).isEqualTo(additionalInfo);
        }

        @Test
        @DisplayName("Should chain multiple builder methods")
        void shouldChainMultipleBuilderMethods() {
            ErrorResponse response =
                    ErrorResponse.of("ERROR", "message")
                            .withPath("/api/test")
                            .withMethod("POST")
                            .withTraceId("trace-123")
                            .withSpanId("span-456")
                            .withStatus(400)
                            .withCorrelationId("corr-789");

            assertThat(response.getPath()).isEqualTo("/api/test");
            assertThat(response.getMethod()).isEqualTo("POST");
            assertThat(response.getTraceId()).isEqualTo("trace-123");
            assertThat(response.getSpanId()).isEqualTo("span-456");
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getCorrelationId()).isEqualTo("corr-789");
        }
    }

    @Nested
    @DisplayName("Validation Error Methods Tests")
    class ValidationErrorMethodsTests {

        @Test
        @DisplayName("Should add validation error with field, rejected value, and message")
        void shouldAddValidationErrorWithFieldRejectedValueAndMessage() {
            ErrorResponse response = new ErrorResponse();
            String field = "email";
            String rejectedValue = "invalid-email";
            String message = "Invalid email format";

            response.addValidationError(field, rejectedValue, message);

            assertThat(response.getValidationErrors()).hasSize(1);
            ErrorResponse.ValidationError validationError = response.getValidationErrors().get(0);
            assertThat(validationError.getField()).isEqualTo(field);
            assertThat(validationError.getRejectedValue()).isEqualTo(rejectedValue);
            assertThat(validationError.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should add validation error object")
        void shouldAddValidationErrorObject() {
            ErrorResponse response = new ErrorResponse();
            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError("name", null, "Name is required");

            response.addValidationError(validationError);

            assertThat(response.getValidationErrors()).hasSize(1);
            assertThat(response.getValidationErrors().get(0)).isEqualTo(validationError);
        }

        @Test
        @DisplayName("Should add multiple validation errors")
        void shouldAddMultipleValidationErrors() {
            ErrorResponse response = new ErrorResponse();

            response
                    .addValidationError("email", "invalid", "Invalid email")
                    .addValidationError("name", "", "Name cannot be empty");

            assertThat(response.getValidationErrors()).hasSize(2);
        }

        @Test
        @DisplayName("Should initialize validation errors list when null")
        void shouldInitializeValidationErrorsListWhenNull() {
            ErrorResponse response = new ErrorResponse();
            response.setValidationErrors(null);

            response.addValidationError("field", "value", "message");

            assertThat(response.getValidationErrors()).isNotNull().hasSize(1);
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            ErrorResponse response = new ErrorResponse();
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, Object> additionalInfo = Map.of("key", "value");

            response.setErrorCode("TEST_ERROR");
            response.setMessage("Test message");
            response.setErrorDetails("Error details");
            response.setTimestamp(timestamp);
            response.setPath("/api/test");
            response.setMethod("GET");
            response.setTraceId("trace-123");
            response.setSpanId("span-456");
            response.setCorrelationId("corr-789");
            response.setStatus(400);
            response.setAdditionalInfo(additionalInfo);

            assertThat(response.getErrorCode()).isEqualTo("TEST_ERROR");
            assertThat(response.getMessage()).isEqualTo("Test message");
            assertThat(response.getErrorDetails()).isEqualTo("Error details");
            assertThat(response.getTimestamp()).isEqualTo(timestamp);
            assertThat(response.getPath()).isEqualTo("/api/test");
            assertThat(response.getMethod()).isEqualTo("GET");
            assertThat(response.getTraceId()).isEqualTo("trace-123");
            assertThat(response.getSpanId()).isEqualTo("span-456");
            assertThat(response.getCorrelationId()).isEqualTo("corr-789");
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getAdditionalInfo()).isEqualTo(additionalInfo);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            ErrorResponse response = ErrorResponse.of("ERROR", "message");

            assertThat(response).isEqualTo(response);
            assertThat(response.hashCode()).isEqualTo(response.hashCode());
        }

        @Test
        @DisplayName("Should be equal to another instance with same values")
        void shouldBeEqualToAnotherInstanceWithSameValues() {
            ErrorResponse response1 =
                    ErrorResponse.of("ERROR", "message", "/api/test").withTraceId("trace-123");
            ErrorResponse response2 =
                    ErrorResponse.of("ERROR", "message", "/api/test").withTraceId("trace-123");

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ErrorResponse response = ErrorResponse.of("ERROR", "message");

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            ErrorResponse response = ErrorResponse.of("ERROR", "message");
            String differentObject = "different";

            assertThat(response).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("Should not be equal when error code differs")
        void shouldNotBeEqualWhenErrorCodeDiffers() {
            ErrorResponse response1 = ErrorResponse.of("ERROR1", "message");
            ErrorResponse response2 = ErrorResponse.of("ERROR2", "message");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when message differs")
        void shouldNotBeEqualWhenMessageDiffers() {
            ErrorResponse response1 = ErrorResponse.of("ERROR", "message1");
            ErrorResponse response2 = ErrorResponse.of("ERROR", "message2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when path differs")
        void shouldNotBeEqualWhenPathDiffers() {
            ErrorResponse response1 = ErrorResponse.of("ERROR", "message", "/path1");
            ErrorResponse response2 = ErrorResponse.of("ERROR", "message", "/path2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when trace ID differs")
        void shouldNotBeEqualWhenTraceIdDiffers() {
            ErrorResponse response1 = ErrorResponse.of("ERROR", "message").withTraceId("trace1");
            ErrorResponse response2 = ErrorResponse.of("ERROR", "message").withTraceId("trace2");

            assertThat(response1).isNotEqualTo(response2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString with all fields")
        void shouldGenerateToStringWithAllFields() {
            ErrorResponse response =
                    ErrorResponse.of("ERROR_CODE", "Error message", "/api/test").withTraceId("trace-123");
            String toString = response.toString();

            assertThat(toString).contains("ErrorResponse{");
            assertThat(toString).contains("errorCode='ERROR_CODE'");
            assertThat(toString).contains("message='Error message'");
            assertThat(toString).contains("path='/api/test'");
            assertThat(toString).contains("traceId='trace-123'");
            assertThat(toString).contains("timestamp=");
        }
    }

    @Nested
    @DisplayName("ValidationError Inner Class Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should create ValidationError with default constructor")
        void shouldCreateValidationErrorWithDefaultConstructor() {
            ErrorResponse.ValidationError validationError = new ErrorResponse.ValidationError();

            assertThat(validationError.getField()).isNull();
            assertThat(validationError.getRejectedValue()).isNull();
            assertThat(validationError.getMessage()).isNull();
        }

        @Test
        @DisplayName("Should create ValidationError with all parameters")
        void shouldCreateValidationErrorWithAllParameters() {
            String field = "email";
            String rejectedValue = "invalid@";
            String message = "Invalid email format";

            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError(field, rejectedValue, message);

            assertThat(validationError.getField()).isEqualTo(field);
            assertThat(validationError.getRejectedValue()).isEqualTo(rejectedValue);
            assertThat(validationError.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should set and get all ValidationError properties")
        void shouldSetAndGetAllValidationErrorProperties() {
            ErrorResponse.ValidationError validationError = new ErrorResponse.ValidationError();

            validationError.setField("name");
            validationError.setRejectedValue("");
            validationError.setMessage("Name cannot be empty");

            assertThat(validationError.getField()).isEqualTo("name");
            assertThat(validationError.getRejectedValue()).isEqualTo("");
            assertThat(validationError.getMessage()).isEqualTo("Name cannot be empty");
        }

        @Test
        @DisplayName("Should handle different rejected value types")
        void shouldHandleDifferentRejectedValueTypes() {
            ErrorResponse.ValidationError stringError =
                    new ErrorResponse.ValidationError("field1", "string", "message");
            ErrorResponse.ValidationError intError =
                    new ErrorResponse.ValidationError("field2", 123, "message");
            ErrorResponse.ValidationError nullError =
                    new ErrorResponse.ValidationError("field3", null, "message");

            assertThat(stringError.getRejectedValue()).isEqualTo("string");
            assertThat(intError.getRejectedValue()).isEqualTo(123);
            assertThat(nullError.getRejectedValue()).isNull();
        }

        @Test
        @DisplayName("ValidationError should be equal to itself")
        void validationErrorShouldBeEqualToItself() {
            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError("field", "value", "message");

            assertThat(validationError).isEqualTo(validationError);
            assertThat(validationError.hashCode()).isEqualTo(validationError.hashCode());
        }

        @Test
        @DisplayName("ValidationError should be equal to another with same values")
        void validationErrorShouldBeEqualToAnotherWithSameValues() {
            ErrorResponse.ValidationError error1 =
                    new ErrorResponse.ValidationError("field", "value", "message");
            ErrorResponse.ValidationError error2 =
                    new ErrorResponse.ValidationError("field", "value", "message");

            assertThat(error1).isEqualTo(error2);
            assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
        }

        @Test
        @DisplayName("ValidationError should not be equal to null")
        void validationErrorShouldNotBeEqualToNull() {
            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError("field", "value", "message");

            assertThat(validationError).isNotEqualTo(null);
        }

        @Test
        @DisplayName("ValidationError should not be equal to different class")
        void validationErrorShouldNotBeEqualToDifferentClass() {
            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError("field", "value", "message");
            String differentObject = "different";

            assertThat(validationError).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("ValidationError should not be equal when field differs")
        void validationErrorShouldNotBeEqualWhenFieldDiffers() {
            ErrorResponse.ValidationError error1 =
                    new ErrorResponse.ValidationError("field1", "value", "message");
            ErrorResponse.ValidationError error2 =
                    new ErrorResponse.ValidationError("field2", "value", "message");

            assertThat(error1).isNotEqualTo(error2);
        }

        @Test
        @DisplayName("ValidationError should not be equal when rejected value differs")
        void validationErrorShouldNotBeEqualWhenRejectedValueDiffers() {
            ErrorResponse.ValidationError error1 =
                    new ErrorResponse.ValidationError("field", "value1", "message");
            ErrorResponse.ValidationError error2 =
                    new ErrorResponse.ValidationError("field", "value2", "message");

            assertThat(error1).isNotEqualTo(error2);
        }

        @Test
        @DisplayName("ValidationError should not be equal when message differs")
        void validationErrorShouldNotBeEqualWhenMessageDiffers() {
            ErrorResponse.ValidationError error1 =
                    new ErrorResponse.ValidationError("field", "value", "message1");
            ErrorResponse.ValidationError error2 =
                    new ErrorResponse.ValidationError("field", "value", "message2");

            assertThat(error1).isNotEqualTo(error2);
        }

        @Test
        @DisplayName("ValidationError should generate toString")
        void validationErrorShouldGenerateToString() {
            ErrorResponse.ValidationError validationError =
                    new ErrorResponse.ValidationError("email", "invalid@", "Invalid email");
            String toString = validationError.toString();

            assertThat(toString).contains("ValidationError{");
            assertThat(toString).contains("field='email'");
            assertThat(toString).contains("rejectedValue=invalid@");
            assertThat(toString).contains("message='Invalid email'");
        }

        @Test
        @DisplayName("ValidationError should handle null values in equals")
        void validationErrorShouldHandleNullValuesInEquals() {
            ErrorResponse.ValidationError error1 = new ErrorResponse.ValidationError();
            ErrorResponse.ValidationError error2 = new ErrorResponse.ValidationError();

            assertThat(error1).isEqualTo(error2);
            assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
        }
    }
}
