package com.inventorymanagement.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BulkOperationResponse Unit Tests")
class BulkOperationResponseTest {

    private List<String> testResults;
    private List<BulkOperationResponse.BulkOperationError> testErrors;
    private BulkOperationResponse.BulkOperationError testError;

    @BeforeEach
    void setUp() {
        testResults = Arrays.asList("result1", "result2", "result3");

        testError =
                new BulkOperationResponse.BulkOperationError(
                        0, "Test error message", "TEST_ERROR", "errorData");

        testErrors = List.of(testError);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void defaultConstructorShouldCreateEmptyInstance() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            assertThat(response.getTotalItems()).isEqualTo(0);
            assertThat(response.getSuccessfulItems()).isEqualTo(0);
            assertThat(response.getFailedItems()).isEqualTo(0);
            assertThat(response.getSuccessRate()).isEqualTo(0.0);
            assertThat(response.getResults()).isEmpty();
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getExecutionTimeMs()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void parameterizedConstructorShouldSetFields() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>(5, 3, 2);

            assertThat(response.getTotalItems()).isEqualTo(5);
            assertThat(response.getSuccessfulItems()).isEqualTo(3);
            assertThat(response.getFailedItems()).isEqualTo(2);
            assertThat(response.getSuccessRate()).isEqualTo(60.0);
            assertThat(response.getResults()).isEmpty();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should calculate success rate correctly for zero total")
        void parameterizedConstructorZeroTotalShouldHaveZeroSuccessRate() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>(0, 0, 0);

            assertThat(response.getSuccessRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Static Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create successful response")
        void successWithResultsShouldCreateSuccessfulResponse() {
            BulkOperationResponse<String> response = BulkOperationResponse.success(testResults);

            assertThat(response.getTotalItems()).isEqualTo(3);
            assertThat(response.getSuccessfulItems()).isEqualTo(3);
            assertThat(response.getFailedItems()).isEqualTo(0);
            assertThat(response.getSuccessRate()).isEqualTo(100.0);
            assertThat(response.getResults()).isEqualTo(testResults);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should create partial response")
        void partialWithResultsAndErrorsShouldCreatePartialResponse() {
            BulkOperationResponse<String> response =
                    BulkOperationResponse.partial(testResults, testErrors);

            assertThat(response.getTotalItems()).isEqualTo(4); // 3 results + 1 error
            assertThat(response.getSuccessfulItems()).isEqualTo(3);
            assertThat(response.getFailedItems()).isEqualTo(1);
            assertThat(response.getSuccessRate()).isEqualTo(75.0);
            assertThat(response.getResults()).isEqualTo(testResults);
            assertThat(response.getErrors()).isEqualTo(testErrors);
        }

        @Test
        @DisplayName("Should create failure response")
        void failureWithErrorsShouldCreateFailureResponse() {
            BulkOperationResponse<String> response = BulkOperationResponse.failure(testErrors);

            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getSuccessfulItems()).isEqualTo(0);
            assertThat(response.getFailedItems()).isEqualTo(1);
            assertThat(response.getSuccessRate()).isEqualTo(0.0);
            assertThat(response.getResults()).isEmpty();
            assertThat(response.getErrors()).isEqualTo(testErrors);
        }

        @Test
        @DisplayName("Should handle empty results in success")
        void successEmptyResultsShouldCreateEmptySuccessResponse() {
            BulkOperationResponse<String> response =
                    BulkOperationResponse.success(Collections.emptyList());

            assertThat(response.getTotalItems()).isEqualTo(0);
            assertThat(response.getSuccessfulItems()).isEqualTo(0);
            assertThat(response.getFailedItems()).isEqualTo(0);
            assertThat(response.getSuccessRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle empty errors in failure")
        void failureEmptyErrorsShouldCreateEmptyFailureResponse() {
            BulkOperationResponse<String> response =
                    BulkOperationResponse.failure(Collections.emptyList());

            assertThat(response.getTotalItems()).isEqualTo(0);
            assertThat(response.getSuccessfulItems()).isEqualTo(0);
            assertThat(response.getFailedItems()).isEqualTo(0);
            assertThat(response.getSuccessRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should add result and update counters")
        void addResultShouldUpdateCountersAndRecalculateRate() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.addResult("newResult");

            assertThat(response.getResults()).containsExactly("newResult");
            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getSuccessfulItems()).isEqualTo(1);
            assertThat(response.getFailedItems()).isEqualTo(0);
            assertThat(response.getSuccessRate()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should add multiple results")
        void addResultMultipleShouldAccumulateResults() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.addResult("result1");
            response.addResult("result2");

            assertThat(response.getResults()).containsExactly("result1", "result2");
            assertThat(response.getTotalItems()).isEqualTo(2);
            assertThat(response.getSuccessfulItems()).isEqualTo(2);
            assertThat(response.getSuccessRate()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should add error and update counters")
        void addErrorShouldUpdateCountersAndRecalculateRate() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.addError(testError);

            assertThat(response.getErrors()).containsExactly(testError);
            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getSuccessfulItems()).isEqualTo(0);
            assertThat(response.getFailedItems()).isEqualTo(1);
            assertThat(response.getSuccessRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should add mixed results and errors")
        void addMixedShouldCalculateCorrectRates() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.addResult("success1");
            response.addResult("success2");
            response.addError(testError);

            assertThat(response.getTotalItems()).isEqualTo(3);
            assertThat(response.getSuccessfulItems()).isEqualTo(2);
            assertThat(response.getFailedItems()).isEqualTo(1);
            assertThat(response.getSuccessRate()).isEqualTo(66.66666666666666); // 2/3 * 100
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get totalItems")
        void setGetTotalItems_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setTotalItems(10);

            assertThat(response.getTotalItems()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should set and get successfulItems")
        void setGetSuccessfulItems_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setSuccessfulItems(7);

            assertThat(response.getSuccessfulItems()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should set and get failedItems")
        void setGetFailedItems_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setFailedItems(3);

            assertThat(response.getFailedItems()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should set and get successRate")
        void setGetSuccessRate_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setSuccessRate(85.5);

            assertThat(response.getSuccessRate()).isEqualTo(85.5);
        }

        @Test
        @DisplayName("Should set and get results")
        void setGetResults_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setResults(testResults);

            assertThat(response.getResults()).isEqualTo(testResults);
        }

        @Test
        @DisplayName("Should set and get errors")
        void setGetErrors_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setErrors(testErrors);

            assertThat(response.getErrors()).isEqualTo(testErrors);
        }

        @Test
        @DisplayName("Should set and get executionTimeMs")
        void setGetExecutionTimeMs_ShouldWork() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setExecutionTimeMs(1500L);

            assertThat(response.getExecutionTimeMs()).isEqualTo(1500L);
        }
    }

    @Nested
    @DisplayName("BulkOperationError Tests")
    class BulkOperationErrorTests {

        @Test
        @DisplayName("Should create error with all fields")
        void errorConstructorShouldSetAllFields() {
            int index = 5;
            String message = "Error message";
            String errorCode = "ERR_001";
            String data = "error data";

            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError(
                            index, message,
                            errorCode, data);

            assertThat(error.getIndex()).isEqualTo(index);
            assertThat(error.getMessage()).isEqualTo(message);
            assertThat(error.getErrorCode()).isEqualTo(errorCode);
            assertThat(error.getItem()).isEqualTo(data);
        }

        @Test
        @DisplayName("Should set and get index")
        void error_SetGetIndex_ShouldWork() {
            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError();

            error.setIndex(42);

            assertThat(error.getIndex()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should set and get message")
        void error_SetGetMessage_ShouldWork() {
            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError();

            error.setMessage("Custom error message");

            assertThat(error.getMessage()).isEqualTo("Custom error message");
        }

        @Test
        @DisplayName("Should set and get errorCode")
        void error_SetGetErrorCode_ShouldWork() {
            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError();

            error.setErrorCode("CUSTOM_ERROR");

            assertThat(error.getErrorCode()).isEqualTo("CUSTOM_ERROR");
        }

        @Test
        @DisplayName("Should set and get item")
        void error_SetGetItem_ShouldWork() {
            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError();

            error.setItem("Custom item");

            assertThat(error.getItem()).isEqualTo("Custom item");
        }

        @Test
        @DisplayName("Should handle null item")
        void error_WithNullItem_ShouldAcceptNull() {
            BulkOperationResponse.BulkOperationError error =
                    new BulkOperationResponse.BulkOperationError(1, "message", "code", null);

            assertThat(error.getItem()).isNull();
        }

        @Test
        @DisplayName("Should be equal when all fields match")
        void error_Equals_SameFields_ShouldReturnTrue() {
            BulkOperationResponse.BulkOperationError error1 =
                    new BulkOperationResponse.BulkOperationError(1, "msg", "code", "data");
            BulkOperationResponse.BulkOperationError error2 =
                    new BulkOperationResponse.BulkOperationError(1, "msg", "code", "data");

            assertThat(error1).isEqualTo(error2);
            assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void error_Equals_DifferentFields_ShouldReturnFalse() {
            BulkOperationResponse.BulkOperationError error1 =
                    new BulkOperationResponse.BulkOperationError(1, "msg", "code", "data");
            BulkOperationResponse.BulkOperationError error2 =
                    new BulkOperationResponse.BulkOperationError(2, "msg", "code", "data");

            assertThat(error1).isNotEqualTo(error2);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large numbers")
        void largeNumbersShouldCalculateCorrectly() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>(1000000, 999999, 1);

            assertThat(response.getSuccessRate()).isEqualTo(99.9999);
        }

        @Test
        @DisplayName("Should handle zero division in success rate")
        void zeroTotalShouldNotThrowException() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>(0, 5, 3);

            assertThat(response.getSuccessRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should maintain consistency after manual counter changes")
        void manualCounterChanges_ShouldNotAffectRate() {
            BulkOperationResponse<String> response = new BulkOperationResponse<>();

            response.setTotalItems(10);
            response.setSuccessfulItems(7);
            response.setFailedItems(3);
            response.setSuccessRate(70.0);

            assertThat(response.getSuccessRate()).isEqualTo(70.0);
        }
    }
}
