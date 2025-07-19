package com.inventorymanagement.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BulkOperationRequest Unit Tests")
class BulkOperationRequestTest {

    private List<String> testItems;
    private BulkOperationRequest.BulkOperationOptions testOptions;

    @BeforeEach
    void setUp() {
        testItems = Arrays.asList("item1", "item2", "item3");
        testOptions = new BulkOperationRequest.BulkOperationOptions();
        testOptions.setContinueOnError(true);
        testOptions.setValidateAll(false);
        testOptions.setReturnDetails(true);
        testOptions.setBatchSize(5);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void defaultConstructorShouldCreateEmptyInstance() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>();

            assertThat(request.getItems()).isNull();
            assertThat(request.getOptions()).isNull();
        }

        @Test
        @DisplayName("Should create instance with items only")
        void constructorWithItemsShouldSetItems() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems);

            assertThat(request.getItems()).isEqualTo(testItems);
            assertThat(request.getOptions()).isNull();
        }

        @Test
        @DisplayName("Should create instance with items and options")
        void constructorWithItemsAndOptionsShouldSetBoth() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems, testOptions);

            assertThat(request.getItems()).isEqualTo(testItems);
            assertThat(request.getOptions()).isEqualTo(testOptions);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get items correctly")
        void setGetItemsShouldWorkCorrectly() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>();
            List<String> newItems = Arrays.asList("newItem1", "newItem2");

            request.setItems(newItems);

            assertThat(request.getItems()).isEqualTo(newItems);
        }

        @Test
        @DisplayName("Should set and get options correctly")
        void setGetOptionsShouldWorkCorrectly() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>();
            BulkOperationRequest.BulkOperationOptions newOptions =
                    new BulkOperationRequest.BulkOperationOptions();
            newOptions.setContinueOnError(false);

            request.setOptions(newOptions);

            assertThat(request.getOptions()).isEqualTo(newOptions);
        }

        @Test
        @DisplayName("Should handle null items")
        void setItemsWithNullShouldAcceptNull() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems);

            request.setItems(null);

            assertThat(request.getItems()).isNull();
        }

        @Test
        @DisplayName("Should handle null options")
        void setOptionsWithNullShouldAcceptNull() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems, testOptions);

            request.setOptions(null);

            assertThat(request.getOptions()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should handle empty items list")
        void shouldHandleEmptyItemsList() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(Collections.emptyList());

            assertThat(request.getItems()).isEmpty();
            assertThat(request.getItems()).hasSize(0);
        }

        @Test
        @DisplayName("Should handle items list with nulls")
        void shouldHandleItemsListWithNulls() {
            List<String> itemsWithNulls = Arrays.asList("item1", null, "item3");
            BulkOperationRequest<String> request = new BulkOperationRequest<>(itemsWithNulls);

            assertThat(request.getItems()).hasSize(3);
            assertThat(request.getItems().get(0)).isEqualTo("item1");
            assertThat(request.getItems().get(1)).isNull();
            assertThat(request.getItems().get(2)).isEqualTo("item3");
        }

        @Test
        @DisplayName("Should handle very large items list")
        void shouldHandleVeryLargeItemsList() {
            List<String> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeList.add("item" + i);
            }

            BulkOperationRequest<String> request = new BulkOperationRequest<>(largeList);

            assertThat(request.getItems()).hasSize(1000);
            assertThat(request.getItems().get(0)).isEqualTo("item0");
            assertThat(request.getItems().get(999)).isEqualTo("item999");
        }

        @Test
        @DisplayName("Should handle single item in list")
        void shouldHandleSingleItemInList() {
            List<String> singleItem = List.of("onlyItem");
            BulkOperationRequest<String> request = new BulkOperationRequest<>(singleItem);

            assertThat(request.getItems()).hasSize(1);
            assertThat(request.getItems().get(0)).isEqualTo("onlyItem");
        }
    }

    @Nested
    @DisplayName("BulkOperationOptions Tests")
    class BulkOperationOptionsTests {

        @Test
        @DisplayName("Should create options with default constructor")
        void shouldCreateOptionsWithDefaultConstructor() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            // Verify default values if they exist, otherwise verify creation
            assertThat(options).isNotNull();
        }

        @Test
        @DisplayName("Should set and get continueOnError")
        void shouldSetAndGetContinueOnError() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setContinueOnError(true);
            assertThat(options.isContinueOnError()).isTrue();

            options.setContinueOnError(false);
            assertThat(options.isContinueOnError()).isFalse();
        }

        @Test
        @DisplayName("Should set and get validateAll")
        void shouldSetAndGetValidateAll() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setValidateAll(true);
            assertThat(options.isValidateAll()).isTrue();

            options.setValidateAll(false);
            assertThat(options.isValidateAll()).isFalse();
        }

        @Test
        @DisplayName("Should set and get returnDetails")
        void shouldSetAndGetReturnDetails() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setReturnDetails(true);
            assertThat(options.isReturnDetails()).isTrue();

            options.setReturnDetails(false);
            assertThat(options.isReturnDetails()).isFalse();
        }

        @Test
        @DisplayName("Should set and get batchSize")
        void shouldSetAndGetBatchSize() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setBatchSize(10);
            assertThat(options.getBatchSize()).isEqualTo(10);

            options.setBatchSize(100);
            assertThat(options.getBatchSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle zero batch size")
        void shouldHandleZeroBatchSize() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setBatchSize(0);
            assertThat(options.getBatchSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle negative batch size")
        void shouldHandleNegativeBatchSize() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setBatchSize(-1);
            assertThat(options.getBatchSize()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle very large batch size")
        void shouldHandleVeryLargeBatchSize() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            options.setBatchSize(Integer.MAX_VALUE);
            assertThat(options.getBatchSize()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should create options with all combinations of boolean values")
        void shouldCreateOptionsWithAllBooleanCombinations() {
            BulkOperationRequest.BulkOperationOptions options =
                    new BulkOperationRequest.BulkOperationOptions();

            // Test all combinations of boolean values
            boolean[] values = {true, false};

            for (boolean continueOnError : values) {
                for (boolean validateAll : values) {
                    for (boolean returnDetails : values) {
                        options.setContinueOnError(continueOnError);
                        options.setValidateAll(validateAll);
                        options.setReturnDetails(returnDetails);

                        assertThat(options.isContinueOnError()).isEqualTo(continueOnError);
                        assertThat(options.isValidateAll()).isEqualTo(validateAll);
                        assertThat(options.isReturnDetails()).isEqualTo(returnDetails);
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems, testOptions);

            assertThat(request).isEqualTo(request);
            assertThat(request.hashCode()).isEqualTo(request.hashCode());
        }

        @Test
        @DisplayName("Should be equal to another instance with same values")
        void shouldBeEqualToAnotherInstanceWithSameValues() {
            BulkOperationRequest<String> request1 = new BulkOperationRequest<>(testItems, testOptions);
            BulkOperationRequest<String> request2 = new BulkOperationRequest<>(testItems, testOptions);

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems);

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems);

            assertThat(request).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should not be equal when items differ")
        void shouldNotBeEqualWhenItemsDiffer() {
            BulkOperationRequest<String> request1 = new BulkOperationRequest<>(testItems);
            BulkOperationRequest<String> request2 =
                    new BulkOperationRequest<>(Arrays.asList("different", "items"));

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal when options differ")
        void shouldNotBeEqualWhenOptionsDiffer() {
            BulkOperationRequest.BulkOperationOptions differentOptions =
                    new BulkOperationRequest.BulkOperationOptions();
            differentOptions.setContinueOnError(false);
            differentOptions.setValidateAll(true);

            BulkOperationRequest<String> request1 = new BulkOperationRequest<>(testItems, testOptions);
            BulkOperationRequest<String> request2 =
                    new BulkOperationRequest<>(testItems, differentOptions);

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should handle null values in equals")
        void shouldHandleNullValuesInEquals() {
            BulkOperationRequest<String> request1 = new BulkOperationRequest<>(null, null);
            BulkOperationRequest<String> request2 = new BulkOperationRequest<>(null, null);
            BulkOperationRequest<String> request3 = new BulkOperationRequest<>(testItems, null);

            assertThat(request1).isEqualTo(request2);
            assertThat(request1).isNotEqualTo(request3);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include key fields in toString")
        void shouldIncludeKeyFieldsInToString() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(testItems, testOptions);

            String result = request.toString();

            assertThat(result).contains("BulkOperationRequest");
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(null, null);

            String result = request.toString();

            assertThat(result).isNotNull();
            assertThat(result).contains("BulkOperationRequest");
        }

        @Test
        @DisplayName("Should handle empty items in toString")
        void shouldHandleEmptyItemsInToString() {
            BulkOperationRequest<String> request = new BulkOperationRequest<>(Collections.emptyList());

            String result = request.toString();

            assertThat(result).isNotNull();
            assertThat(result).contains("BulkOperationRequest");
        }
    }

    @Nested
    @DisplayName("Type Safety Tests")
    class TypeSafetyTests {

        @Test
        @DisplayName("Should handle different generic types")
        void shouldHandleDifferentGenericTypes() {
            BulkOperationRequest<Integer> intRequest = new BulkOperationRequest<>(Arrays.asList(1, 2, 3));
            BulkOperationRequest<Double> doubleRequest =
                    new BulkOperationRequest<>(Arrays.asList(1.1, 2.2, 3.3));

            assertThat(intRequest.getItems()).containsExactly(1, 2, 3);
            assertThat(doubleRequest.getItems()).containsExactly(1.1, 2.2, 3.3);
        }

        @Test
        @DisplayName("Should handle complex object types")
        void shouldHandleComplexObjectTypes() {
            List<Map<String, Object>> complexObjects =
                    Arrays.asList(Map.of("id", 1, "name", "object1"), Map.of("id", 2, "name", "object2"));

            BulkOperationRequest<Map<String, Object>> request =
                    new BulkOperationRequest<>(complexObjects);

            assertThat(request.getItems()).hasSize(2);
            assertThat(request.getItems().get(0).get("id")).isEqualTo(1);
            assertThat(request.getItems().get(1).get("name")).isEqualTo("object2");
        }
    }
}
