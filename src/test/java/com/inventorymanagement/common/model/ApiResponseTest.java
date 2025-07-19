package com.inventorymanagement.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for ApiResponse to achieve maximum code coverage
 */
@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ApiResponse with default constructor")
        void shouldCreateWithDefaultConstructor() {
            ApiResponse<String> response = new ApiResponse<>();

            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isNull();
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getPath()).isNull();
        }

        @Test
        @DisplayName("Should create ApiResponse with success, message, and data")
        void shouldCreateWithSuccessMessageAndData() {
            String testData = "test data";
            ApiResponse<String> response = new ApiResponse<>(true, "Success message", testData);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Success message");
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getPath()).isNull();
        }

        @Test
        @DisplayName("Should create ApiResponse with success, message, data, and path")
        void shouldCreateWithSuccessMessageDataAndPath() {
            String testData = "test data";
            String testPath = "/api/test";
            ApiResponse<String> response = new ApiResponse<>(true, "Success message", testData, testPath);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Success message");
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getPath()).isEqualTo(testPath);
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Success Factory Methods Tests")
    class SuccessFactoryMethodsTests {

        @Test
        @DisplayName("Should create success response with data only")
        void shouldCreateSuccessWithDataOnly() {
            String testData = "test data";
            ApiResponse<String> response = ApiResponse.success(testData);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getError()).isNull();
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should create success response with custom message and data")
        void shouldCreateSuccessWithMessageAndData() {
            String testData = "test data";
            String customMessage = "Custom success message";
            ApiResponse<String> response = ApiResponse.success(customMessage, testData);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(customMessage);
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getError()).isNull();
            assertThat(response.getErrorCode()).isNull();
        }

        @Test
        @DisplayName("Should create success response with message, data, and path")
        void shouldCreateSuccessWithMessageDataAndPath() {
            String testData = "test data";
            String customMessage = "Custom success message";
            String testPath = "/api/test";
            ApiResponse<String> response = ApiResponse.success(customMessage, testData, testPath);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(customMessage);
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getPath()).isEqualTo(testPath);
            assertThat(response.getError()).isNull();
            assertThat(response.getErrorCode()).isNull();
        }

        @Test
        @DisplayName("Should create success response with null data")
        void shouldCreateSuccessWithNullData() {
            ApiResponse<String> response = ApiResponse.success(null);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("Should create success response with complex data types")
        void shouldCreateSuccessWithComplexDataTypes() {
            Map<String, Object> complexData = Map.of("key1", "value1", "key2", 123, "key3", true);
            ApiResponse<Map<String, Object>> response = ApiResponse.success(complexData);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(complexData);
            assertThat(response.getData()).containsEntry("key1", "value1");
            assertThat(response.getData()).containsEntry("key2", 123);
            assertThat(response.getData()).containsEntry("key3", true);
        }
    }

    @Nested
    @DisplayName("Error Factory Methods Tests")
    class ErrorFactoryMethodsTests {

        @Test
        @DisplayName("Should create error response with message only")
        void shouldCreateErrorWithMessageOnly() {
            String errorMessage = "Something went wrong";
            ApiResponse<String> response = ApiResponse.error(errorMessage);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isEqualTo(errorMessage);
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should create error response with message and error code")
        void shouldCreateErrorWithMessageAndCode() {
            String errorMessage = "Validation failed";
            String errorCode = "VALIDATION_ERROR";
            ApiResponse<String> response = ApiResponse.error(errorMessage, errorCode);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isEqualTo(errorMessage);
            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getPath()).isNull();
        }

        @Test
        @DisplayName("Should create error response with message, error code, and path")
        void shouldCreateErrorWithMessageCodeAndPath() {
            String errorMessage = "Resource not found";
            String errorCode = "NOT_FOUND";
            String testPath = "/api/resource/123";
            ApiResponse<String> response = ApiResponse.error(errorMessage, errorCode, testPath);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isEqualTo(errorMessage);
            assertThat(response.getErrorCode()).isEqualTo(errorCode);
            assertThat(response.getPath()).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Should create error response with null values")
        void shouldCreateErrorWithNullValues() {
            ApiResponse<String> response = ApiResponse.error(null);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getError()).isNull();
            assertThat(response.getErrorCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should set and get success")
        void shouldSetAndGetSuccess() {
            ApiResponse<String> response = new ApiResponse<>();

            response.setSuccess(true);
            assertThat(response.isSuccess()).isTrue();

            response.setSuccess(false);
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should set and get message")
        void shouldSetAndGetMessage() {
            ApiResponse<String> response = new ApiResponse<>();
            String testMessage = "Test message";

            response.setMessage(testMessage);
            assertThat(response.getMessage()).isEqualTo(testMessage);

            response.setMessage(null);
            assertThat(response.getMessage()).isNull();
        }

        @Test
        @DisplayName("Should set and get data")
        void shouldSetAndGetData() {
            ApiResponse<String> response = new ApiResponse<>();
            String testData = "Test data";

            response.setData(testData);
            assertThat(response.getData()).isEqualTo(testData);

            response.setData(null);
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("Should set and get error")
        void shouldSetAndGetError() {
            ApiResponse<String> response = new ApiResponse<>();
            String testError = "Test error";

            response.setError(testError);
            assertThat(response.getError()).isEqualTo(testError);

            response.setError(null);
            assertThat(response.getError()).isNull();
        }

        @Test
        @DisplayName("Should set and get error code")
        void shouldSetAndGetErrorCode() {
            ApiResponse<String> response = new ApiResponse<>();
            String testErrorCode = "TEST_ERROR";

            response.setErrorCode(testErrorCode);
            assertThat(response.getErrorCode()).isEqualTo(testErrorCode);

            response.setErrorCode(null);
            assertThat(response.getErrorCode()).isNull();
        }

        @Test
        @DisplayName("Should set and get timestamp")
        void shouldSetAndGetTimestamp() {
            ApiResponse<String> response = new ApiResponse<>();
            LocalDateTime testTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

            response.setTimestamp(testTimestamp);
            assertThat(response.getTimestamp()).isEqualTo(testTimestamp);

            response.setTimestamp(null);
            assertThat(response.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Should set and get path")
        void shouldSetAndGetPath() {
            ApiResponse<String> response = new ApiResponse<>();
            String testPath = "/api/test";

            response.setPath(testPath);
            assertThat(response.getPath()).isEqualTo(testPath);

            response.setPath(null);
            assertThat(response.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            ApiResponse<String> response = ApiResponse.success("test data");

            assertThat(response).isEqualTo(response);
            assertThat(response.hashCode()).isEqualTo(response.hashCode());
        }

        @Test
        @DisplayName("Should be equal to another instance with same values")
        void shouldBeEqualToAnotherInstanceWithSameValues() {
            ApiResponse<String> response1 = ApiResponse.success("Success message", "test data");
            ApiResponse<String> response2 = ApiResponse.success("Success message", "test data");

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ApiResponse<String> response = ApiResponse.success("test data");

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            ApiResponse<String> response = ApiResponse.success("test data");
            String differentObject = "different";

            assertThat(response).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("Should not be equal when success differs")
        void shouldNotBeEqualWhenSuccessDiffers() {
            ApiResponse<String> success = ApiResponse.success("test data");
            ApiResponse<String> error = ApiResponse.error("error message");

            assertThat(success).isNotEqualTo(error);
            assertThat(success.hashCode()).isNotEqualTo(error.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when message differs")
        void shouldNotBeEqualWhenMessageDiffers() {
            ApiResponse<String> response1 = ApiResponse.success("message1", "test data");
            ApiResponse<String> response2 = ApiResponse.success("message2", "test data");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when data differs")
        void shouldNotBeEqualWhenDataDiffers() {
            ApiResponse<String> response1 = ApiResponse.success("data1");
            ApiResponse<String> response2 = ApiResponse.success("data2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when error differs")
        void shouldNotBeEqualWhenErrorDiffers() {
            ApiResponse<String> response1 = ApiResponse.error("error1");
            ApiResponse<String> response2 = ApiResponse.error("error2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal when error code differs")
        void shouldNotBeEqualWhenErrorCodeDiffers() {
            ApiResponse<String> response1 = ApiResponse.error("error", "CODE1");
            ApiResponse<String> response2 = ApiResponse.error("error", "CODE2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should handle null values in equals")
        void shouldHandleNullValuesInEquals() {
            ApiResponse<String> response1 = new ApiResponse<>();
            ApiResponse<String> response2 = new ApiResponse<>();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should handle mixed null and non-null values in equals")
        void shouldHandleMixedNullAndNonNullValuesInEquals() {
            ApiResponse<String> responseWithData = ApiResponse.success("data");
            ApiResponse<String> responseWithoutData = new ApiResponse<>();

            assertThat(responseWithData).isNotEqualTo(responseWithoutData);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString for success response")
        void shouldGenerateToStringForSuccessResponse() {
            ApiResponse<String> response = ApiResponse.success("Success message", "test data");
            String toString = response.toString();

            assertThat(toString).contains("ApiResponse{");
            assertThat(toString).contains("success=true");
            assertThat(toString).contains("message='Success message'");
            assertThat(toString).contains("data=test data");
            assertThat(toString).contains("error='null'");
            assertThat(toString).contains("errorCode='null'");
            assertThat(toString).contains("timestamp=");
            assertThat(toString).contains("path='null'");
        }

        @Test
        @DisplayName("Should generate toString for error response")
        void shouldGenerateToStringForErrorResponse() {
            ApiResponse<String> response = ApiResponse.error("Error message", "ERROR_CODE", "/api/path");
            String toString = response.toString();

            assertThat(toString).contains("ApiResponse{");
            assertThat(toString).contains("success=false");
            assertThat(toString).contains("message='null'");
            assertThat(toString).contains("data=null");
            assertThat(toString).contains("error='Error message'");
            assertThat(toString).contains("errorCode='ERROR_CODE'");
            assertThat(toString).contains("path='/api/path'");
        }

        @Test
        @DisplayName("Should generate toString with null values")
        void shouldGenerateToStringWithNullValues() {
            ApiResponse<String> response = new ApiResponse<>();
            String toString = response.toString();

            assertThat(toString).contains("ApiResponse{");
            assertThat(toString).contains("success=false");
            assertThat(toString).contains("message='null'");
            assertThat(toString).contains("data=null");
            assertThat(toString).contains("error='null'");
            assertThat(toString).contains("errorCode='null'");
        }

        @Test
        @DisplayName("Should generate toString for complex data types")
        void shouldGenerateToStringForComplexDataTypes() {
            Map<String, Object> complexData = Map.of("key", "value");
            ApiResponse<Map<String, Object>> response = ApiResponse.success(complexData);
            String toString = response.toString();

            assertThat(toString).contains("data={key=value}");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Type Safety Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle different generic types")
        void shouldHandleDifferentGenericTypes() {
            ApiResponse<Integer> intResponse = ApiResponse.success(42);
            ApiResponse<Boolean> boolResponse = ApiResponse.success(true);
            ApiResponse<Double> doubleResponse = ApiResponse.success(3.14);

            assertThat(intResponse.getData()).isEqualTo(42);
            assertThat(boolResponse.getData()).isTrue();
            assertThat(doubleResponse.getData()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            ApiResponse<String> response = ApiResponse.success("", "");
            response.setError("");
            response.setErrorCode("");
            response.setPath("");

            assertThat(response.getMessage()).isEmpty();
            assertThat(response.getData()).isEmpty();
            assertThat(response.getError()).isEmpty();
            assertThat(response.getErrorCode()).isEmpty();
            assertThat(response.getPath()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve timestamp precision")
        void shouldPreserveTimestampPrecision() {
            LocalDateTime before = LocalDateTime.now();
            ApiResponse<String> response = new ApiResponse<>();
            LocalDateTime after = LocalDateTime.now();

            assertThat(response.getTimestamp()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            String longString = "a".repeat(10000);
            ApiResponse<String> response = ApiResponse.success(longString, longString);
            response.setError(longString);
            response.setErrorCode(longString);
            response.setPath(longString);

            assertThat(response.getMessage()).hasSize(10000);
            assertThat(response.getData()).hasSize(10000);
            assertThat(response.getError()).hasSize(10000);
            assertThat(response.getErrorCode()).hasSize(10000);
            assertThat(response.getPath()).hasSize(10000);
        }
    }
}
