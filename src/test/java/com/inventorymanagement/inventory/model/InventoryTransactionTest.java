package com.inventorymanagement.inventory.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventorymanagement.common.testdata.TestDataFactory;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for InventoryTransaction entity
 */
@DisplayName("InventoryTransaction Tests")
class InventoryTransactionTest {

    private Sku testSku;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testSku =
                TestDataFactory.sku().withId(1L).withSkuCode("TEST-SKU-001").withStockQuantity(100).build();
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty transaction")
        void defaultConstructor_Success() {
            InventoryTransaction transaction = new InventoryTransaction();

            assertThat(transaction.getId()).isNull();
            assertThat(transaction.getSku()).isNull();
            assertThat(transaction.getTransactionType()).isNull();
            assertThat(transaction.getQuantity()).isNull();
            assertThat(transaction.getReferenceId()).isNull();
            assertThat(transaction.getReferenceType()).isNull();
            assertThat(transaction.getReason()).isNull();
            assertThat(transaction.getPerformedBy()).isNull();
            assertThat(transaction.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should create transaction with basic parameters")
        void basicConstructor_Success() {
            InventoryTransaction transaction =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 50);

            assertThat(transaction.getSku()).isEqualTo(testSku);
            assertThat(transaction.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.IN);
            assertThat(transaction.getQuantity()).isEqualTo(50);
            assertThat(transaction.getReferenceId()).isNull();
            assertThat(transaction.getReferenceType()).isNull();
            assertThat(transaction.getReason()).isNull();
            assertThat(transaction.getPerformedBy()).isNull();
        }

        @Test
        @DisplayName("Should create transaction with all parameters")
        void fullConstructor_Success() {
            InventoryTransaction transaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.OUT,
                            25,
                            "REF-123",
                            "ORDER",
                            "Customer order fulfillment",
                            "admin");

            assertThat(transaction.getSku()).isEqualTo(testSku);
            assertThat(transaction.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.OUT);
            assertThat(transaction.getQuantity()).isEqualTo(25);
            assertThat(transaction.getReferenceId()).isEqualTo("REF-123");
            assertThat(transaction.getReferenceType()).isEqualTo("ORDER");
            assertThat(transaction.getReason()).isEqualTo("Customer order fulfillment");
            assertThat(transaction.getPerformedBy()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should identify stock increase transactions")
        void isStockIncrease_VariousScenarios() {
            // Stock IN transaction
            InventoryTransaction stockIn =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 10);
            assertThat(stockIn.isStockIncrease()).isTrue();

            // Positive adjustment
            InventoryTransaction positiveAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 5);
            assertThat(positiveAdjustment.isStockIncrease()).isTrue();

            // Zero adjustment
            InventoryTransaction zeroAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 0);
            assertThat(zeroAdjustment.isStockIncrease()).isFalse();

            // Stock OUT transaction
            InventoryTransaction stockOut =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.OUT, 10);
            assertThat(stockOut.isStockIncrease()).isFalse();

            // Negative adjustment
            InventoryTransaction negativeAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, -5);
            assertThat(negativeAdjustment.isStockIncrease()).isFalse();

            // Reserved transaction
            InventoryTransaction reserved =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RESERVED, 10);
            assertThat(reserved.isStockIncrease()).isFalse();

            // Released transaction
            InventoryTransaction released =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RELEASED, 10);
            assertThat(released.isStockIncrease()).isFalse();
        }

        @Test
        @DisplayName("Should identify stock decrease transactions")
        void isStockDecrease_VariousScenarios() {
            // Stock OUT transaction
            InventoryTransaction stockOut =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.OUT, 10);
            assertThat(stockOut.isStockDecrease()).isTrue();

            // Negative adjustment
            InventoryTransaction negativeAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, -5);
            assertThat(negativeAdjustment.isStockDecrease()).isTrue();

            // Zero adjustment
            InventoryTransaction zeroAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 0);
            assertThat(zeroAdjustment.isStockDecrease()).isFalse();

            // Stock IN transaction
            InventoryTransaction stockIn =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 10);
            assertThat(stockIn.isStockDecrease()).isFalse();

            // Positive adjustment
            InventoryTransaction positiveAdjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 5);
            assertThat(positiveAdjustment.isStockDecrease()).isFalse();

            // Reserved transaction
            InventoryTransaction reserved =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RESERVED, 10);
            assertThat(reserved.isStockDecrease()).isFalse();

            // Released transaction
            InventoryTransaction released =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RELEASED, 10);
            assertThat(released.isStockDecrease()).isFalse();
        }

        @Test
        @DisplayName("Should identify reservation transactions")
        void isReservation_VariousTypes() {
            InventoryTransaction reserved =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RESERVED, 10);
            assertThat(reserved.isReservation()).isTrue();

            InventoryTransaction stockIn =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 10);
            assertThat(stockIn.isReservation()).isFalse();

            InventoryTransaction stockOut =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.OUT, 10);
            assertThat(stockOut.isReservation()).isFalse();

            InventoryTransaction adjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 10);
            assertThat(adjustment.isReservation()).isFalse();

            InventoryTransaction released =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RELEASED, 10);
            assertThat(released.isReservation()).isFalse();
        }

        @Test
        @DisplayName("Should identify release transactions")
        void isRelease_VariousTypes() {
            InventoryTransaction released =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RELEASED, 10);
            assertThat(released.isRelease()).isTrue();

            InventoryTransaction stockIn =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 10);
            assertThat(stockIn.isRelease()).isFalse();

            InventoryTransaction stockOut =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.OUT, 10);
            assertThat(stockOut.isRelease()).isFalse();

            InventoryTransaction adjustment =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 10);
            assertThat(adjustment.isRelease()).isFalse();

            InventoryTransaction reserved =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.RESERVED, 10);
            assertThat(reserved.isRelease()).isFalse();
        }

        @Test
        @DisplayName("Should return absolute quantity")
        void getAbsoluteQuantity_PositiveAndNegative() {
            InventoryTransaction positiveTransaction =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.IN, 15);
            assertThat(positiveTransaction.getAbsoluteQuantity()).isEqualTo(15);

            InventoryTransaction negativeTransaction =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, -20);
            assertThat(negativeTransaction.getAbsoluteQuantity()).isEqualTo(20);

            InventoryTransaction zeroTransaction =
                    new InventoryTransaction(testSku, InventoryTransaction.TransactionType.ADJUSTMENT, 0);
            assertThat(zeroTransaction.getAbsoluteQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should get and set all properties correctly")
        void gettersAndSetters_Success() {
            InventoryTransaction transaction = new InventoryTransaction();

            // Test ID
            transaction.setId(100L);
            assertThat(transaction.getId()).isEqualTo(100L);

            // Test SKU
            transaction.setSku(testSku);
            assertThat(transaction.getSku()).isEqualTo(testSku);

            // Test transaction type
            transaction.setTransactionType(InventoryTransaction.TransactionType.IN);
            assertThat(transaction.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.IN);

            // Test quantity
            transaction.setQuantity(50);
            assertThat(transaction.getQuantity()).isEqualTo(50);

            // Test reference ID
            transaction.setReferenceId("REF-999");
            assertThat(transaction.getReferenceId()).isEqualTo("REF-999");

            // Test reference type
            transaction.setReferenceType("PURCHASE_ORDER");
            assertThat(transaction.getReferenceType()).isEqualTo("PURCHASE_ORDER");

            // Test reason
            transaction.setReason("Test reason");
            assertThat(transaction.getReason()).isEqualTo("Test reason");

            // Test performed by
            transaction.setPerformedBy("test_user");
            assertThat(transaction.getPerformedBy()).isEqualTo("test_user");

            // Test created at
            transaction.setCreatedAt(testTime);
            assertThat(transaction.getCreatedAt()).isEqualTo(testTime);
        }
    }

    @Nested
    @DisplayName("PrePersist Tests")
    class PrePersistTests {

        @Test
        @DisplayName("Should set created at on create")
        void onCreate_SetsCreatedAt() {
            InventoryTransaction transaction = new InventoryTransaction();
            assertThat(transaction.getCreatedAt()).isNull();

            // Call the PrePersist method directly
            transaction.onCreate();

            assertThat(transaction.getCreatedAt()).isNotNull();
            assertThat(transaction.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when id, sku, and createdAt are same")
        void equals_SameValues_ReturnsTrue() {
            InventoryTransaction transaction1 = new InventoryTransaction();
            transaction1.setId(1L);
            transaction1.setSku(testSku);
            transaction1.setCreatedAt(testTime);

            InventoryTransaction transaction2 = new InventoryTransaction();
            transaction2.setId(1L);
            transaction2.setSku(testSku);
            transaction2.setCreatedAt(testTime);

            assertThat(transaction1).isEqualTo(transaction2);
            assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when id differs")
        void equals_DifferentId_ReturnsFalse() {
            InventoryTransaction transaction1 = new InventoryTransaction();
            transaction1.setId(1L);
            transaction1.setSku(testSku);
            transaction1.setCreatedAt(testTime);

            InventoryTransaction transaction2 = new InventoryTransaction();
            transaction2.setId(2L);
            transaction2.setSku(testSku);
            transaction2.setCreatedAt(testTime);

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should not be equal when sku differs")
        void equals_DifferentSku_ReturnsFalse() {
            Sku differentSku = TestDataFactory.sku().withId(2L).withSkuCode("DIFFERENT-SKU").build();

            InventoryTransaction transaction1 = new InventoryTransaction();
            transaction1.setId(1L);
            transaction1.setSku(testSku);
            transaction1.setCreatedAt(testTime);

            InventoryTransaction transaction2 = new InventoryTransaction();
            transaction2.setId(1L);
            transaction2.setSku(differentSku);
            transaction2.setCreatedAt(testTime);

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should not be equal when createdAt differs")
        void equals_DifferentCreatedAt_ReturnsFalse() {
            InventoryTransaction transaction1 = new InventoryTransaction();
            transaction1.setId(1L);
            transaction1.setSku(testSku);
            transaction1.setCreatedAt(testTime);

            InventoryTransaction transaction2 = new InventoryTransaction();
            transaction2.setId(1L);
            transaction2.setSku(testSku);
            transaction2.setCreatedAt(testTime.plusMinutes(1));

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should handle null comparisons")
        void equals_NullComparisons() {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(1L);

            // Should not equal null
            assertThat(transaction).isNotEqualTo(null);

            // Should equal itself
            assertThat(transaction).isEqualTo(transaction);

            // Should not equal different class
            assertThat(transaction).isNotEqualTo("not a transaction");
        }

        @Test
        @DisplayName("Should handle null fields in equals")
        void equals_NullFields() {
            InventoryTransaction transaction1 = new InventoryTransaction();
            InventoryTransaction transaction2 = new InventoryTransaction();

            // Both have all null fields
            assertThat(transaction1).isEqualTo(transaction2);

            // One has id, other doesn't
            transaction1.setId(1L);
            assertThat(transaction1).isNotEqualTo(transaction2);

            // Both have same id
            transaction2.setId(1L);
            assertThat(transaction1).isEqualTo(transaction2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include all key fields in toString")
        void toString_IncludesKeyFields() {
            InventoryTransaction transaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.IN,
                            25,
                            "REF-123",
                            "ORDER",
                            "Test transaction",
                            "admin");
            transaction.setId(1L);
            transaction.setCreatedAt(testTime);

            String result = transaction.toString();

            assertThat(result).contains("InventoryTransaction{");
            assertThat(result).contains("id=1");
            assertThat(result).contains("sku=TEST-SKU-001");
            assertThat(result).contains("transactionType=IN");
            assertThat(result).contains("quantity=25");
            assertThat(result).contains("referenceId='REF-123'");
            assertThat(result).contains("createdAt=" + testTime);
        }

        @Test
        @DisplayName("Should handle null sku in toString")
        void toString_NullSku() {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(1L);
            transaction.setTransactionType(InventoryTransaction.TransactionType.IN);
            transaction.setQuantity(25);
            transaction.setCreatedAt(testTime);

            String result = transaction.toString();

            assertThat(result).contains("sku=null");
        }
    }

    @Nested
    @DisplayName("TransactionType Enum Tests")
    class TransactionTypeTests {

        @Test
        @DisplayName("Should have correct description for each type")
        void getDescription_AllTypes() {
            assertThat(InventoryTransaction.TransactionType.IN.getDescription()).isEqualTo("Stock In");
            assertThat(InventoryTransaction.TransactionType.OUT.getDescription()).isEqualTo("Stock Out");
            assertThat(InventoryTransaction.TransactionType.ADJUSTMENT.getDescription())
                    .isEqualTo("Adjustment");
            assertThat(InventoryTransaction.TransactionType.RESERVED.getDescription())
                    .isEqualTo("Reserved");
            assertThat(InventoryTransaction.TransactionType.RELEASED.getDescription())
                    .isEqualTo("Released");
        }

        @Test
        @DisplayName("Should have all expected enum values")
        void valueOf_AllTypes() {
            // Test that all expected enum values exist
            assertThat(InventoryTransaction.TransactionType.valueOf("IN"))
                    .isEqualTo(InventoryTransaction.TransactionType.IN);
            assertThat(InventoryTransaction.TransactionType.valueOf("OUT"))
                    .isEqualTo(InventoryTransaction.TransactionType.OUT);
            assertThat(InventoryTransaction.TransactionType.valueOf("ADJUSTMENT"))
                    .isEqualTo(InventoryTransaction.TransactionType.ADJUSTMENT);
            assertThat(InventoryTransaction.TransactionType.valueOf("RESERVED"))
                    .isEqualTo(InventoryTransaction.TransactionType.RESERVED);
            assertThat(InventoryTransaction.TransactionType.valueOf("RELEASED"))
                    .isEqualTo(InventoryTransaction.TransactionType.RELEASED);
        }

        @Test
        @DisplayName("Should have correct number of enum values")
        void values_CorrectCount() {
            InventoryTransaction.TransactionType[] values = InventoryTransaction.TransactionType.values();
            assertThat(values).hasSize(5);
        }
    }
}
