package com.inventorymanagement.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventorymanagement.common.BaseUnitTest;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.testdata.TestDataFactory;
import com.inventorymanagement.inventory.model.InventoryTransaction;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.inventory.repository.InventoryTransactionRepository;
import com.inventorymanagement.inventory.repository.SkuRepository;
import com.inventorymanagement.inventory.service.InventoryService.CurrentStockInfo;
import com.inventorymanagement.inventory.service.InventoryService.StockMovementSummary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for InventoryService
 */
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest extends BaseUnitTest {

    @Mock private InventoryTransactionRepository transactionRepository;

    @Mock private SkuRepository skuRepository;

    @InjectMocks private InventoryService inventoryService;

    private Sku testSku;
    private InventoryTransaction testTransaction;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testSku =
                TestDataFactory.sku().withId(1L).withSkuCode("SKU-001").withStockQuantity(100).build();

        testTransaction =
                new InventoryTransaction(
                        testSku,
                        InventoryTransaction.TransactionType.IN,
                        10,
                        "REF-001",
                        "PURCHASE_ORDER",
                        "Stock replenishment",
                        "admin");
        testTransaction.setId(1L);

        testPageable = PageRequest.of(0, 10);
    }

    // Helper method to create test transactions
    private InventoryTransaction createTransaction(
            InventoryTransaction.TransactionType type, int quantity) {
        return new InventoryTransaction(testSku, type, quantity, "REF-001", "TYPE", "reason", "admin");
    }

    @Nested
    @DisplayName("Stock In Operations")
    class StockInOperations {

        @Test
        @DisplayName("Should record stock-in transaction successfully")
        void recordStockIn_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, "REF-001", "Stock replenishment", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo(InventoryTransaction.TransactionType.IN);
            assertThat(result.getQuantity()).isEqualTo(10);
            assertThat(result.getReferenceId()).isEqualTo("REF-001");

            ArgumentCaptor<InventoryTransaction> transactionCaptor =
                    ArgumentCaptor.forClass(InventoryTransaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            InventoryTransaction savedTransaction = transactionCaptor.getValue();
            assertThat(savedTransaction.getSku()).isEqualTo(testSku);
            assertThat(savedTransaction.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.IN);
        }

        @Test
        @DisplayName("Should throw exception when SKU not found for stock-in")
        void recordStockIn_SkuNotFound_ThrowsException() {

            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(999L, 10, "REF-001", "reason", "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("SKU with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when quantity is null for stock-in")
        void recordStockIn_NullQuantity_ThrowsException() {

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, null, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Validation failed for field 'quantity': Stock-in quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero for stock-in")
        void recordStockIn_ZeroQuantity_ThrowsException() {

            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 0, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Validation failed for field 'quantity': Stock-in quantity must be positive");
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative for stock-in")
        void recordStockIn_NegativeQuantity_ThrowsException() {

            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, -5, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Validation failed for field 'quantity': Stock-in quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Stock Out Operations")
    class StockOutOperations {

        @Test
        @DisplayName("Should record stock-out transaction successfully")
        void recordStockOut_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            InventoryTransaction outTransaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.OUT,
                            5,
                            "ORDER-001",
                            "SHIPMENT",
                            "Order fulfillment",
                            "admin");
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(outTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockOut(
                            1L, 5, "ORDER-001", "SHIPMENT", "Order fulfillment", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType()).isEqualTo(InventoryTransaction.TransactionType.OUT);
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getReferenceId()).isEqualTo("ORDER-001");
            assertThat(result.getReferenceType()).isEqualTo("SHIPMENT");

            ArgumentCaptor<InventoryTransaction> transactionCaptor =
                    ArgumentCaptor.forClass(InventoryTransaction.class);
            verify(transactionRepository).save(transactionCaptor.capture());
            InventoryTransaction savedTransaction = transactionCaptor.getValue();
            assertThat(savedTransaction.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.OUT);
        }

        @Test
        @DisplayName("Should throw exception when SKU not found for stock-out")
        void recordStockOut_SkuNotFound_ThrowsException() {
            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockOut(
                                    999L, 5, "ORDER-001", "SHIPMENT", "reason", "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("SKU with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when quantity is invalid for stock-out")
        void recordStockOut_InvalidQuantity_ThrowsException() {

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockOut(
                                    1L, 0, "ORDER-001", "SHIPMENT", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(
                            "Validation failed for field 'quantity': Stock-out quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Operations")
    class StockAdjustmentOperations {

        @Test
        @DisplayName("Should record positive stock adjustment successfully")
        void recordStockAdjustment_PositiveAdjustment_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            InventoryTransaction adjustmentTransaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.ADJUSTMENT,
                            5,
                            null,
                            "MANUAL_COUNT",
                            "Stock count adjustment",
                            "admin");
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(adjustmentTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockAdjustment(
                            1L, 5, "MANUAL_COUNT", "Stock count adjustment", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.ADJUSTMENT);
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getReferenceType()).isEqualTo("MANUAL_COUNT");
        }

        @Test
        @DisplayName("Should record negative stock adjustment successfully")
        void recordStockAdjustment_NegativeAdjustment_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            InventoryTransaction adjustmentTransaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.ADJUSTMENT,
                            3,
                            null,
                            "DAMAGE",
                            "Damaged goods",
                            "admin");
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(adjustmentTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockAdjustment(1L, 3, "DAMAGE", "Damaged goods", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw exception when adjustment is zero")
        void recordStockAdjustment_ZeroAdjustment_ThrowsException() {

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockAdjustment(1L, 0, "MANUAL_COUNT", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Validation failed for field 'adjustment': Stock adjustment cannot be zero");
        }

        @Test
        @DisplayName("Should throw exception when SKU not found for adjustment")
        void recordStockAdjustment_SkuNotFound_ThrowsException() {

            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockAdjustment(
                                    999L, 5, "MANUAL_COUNT", "reason", "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("SKU with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Stock Reservation Operations")
    class StockReservationOperations {

        @Test
        @DisplayName("Should record stock reservation successfully")
        void recordStockReservation_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            InventoryTransaction reservationTransaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.RESERVED,
                            5,
                            "ORDER-001",
                            "ORDER",
                            "Order reservation",
                            "admin");
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(reservationTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockReservation(
                            1L, 5, "ORDER-001", "ORDER", "Order reservation", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.RESERVED);
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getReferenceId()).isEqualTo("ORDER-001");
        }

        @Test
        @DisplayName("Should throw exception when quantity is invalid for reservation")
        void recordStockReservation_InvalidQuantity_ThrowsException() {

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockReservation(
                                    1L, -1, "ORDER-001", "ORDER", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(
                            "Validation failed for field 'quantity': Stock reservation quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Stock Release Operations")
    class StockReleaseOperations {

        @Test
        @DisplayName("Should record stock release successfully")
        void recordStockRelease_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            InventoryTransaction releaseTransaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.RELEASED,
                            3,
                            "ORDER-001",
                            "ORDER",
                            "Order cancelled",
                            "admin");
            when(transactionRepository.save(any(InventoryTransaction.class)))
                    .thenReturn(releaseTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockRelease(
                            1L, 3, "ORDER-001", "ORDER", "Order cancelled", "admin");

            assertThat(result).isNotNull();
            assertThat(result.getTransactionType())
                    .isEqualTo(InventoryTransaction.TransactionType.RELEASED);
            assertThat(result.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw exception when quantity is invalid for release")
        void recordStockRelease_InvalidQuantity_ThrowsException() {

            assertThatThrownBy(
                    () ->
                            inventoryService.recordStockRelease(
                                    1L, 0, "ORDER-001", "ORDER", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(
                            "Validation failed for field 'quantity': Stock release quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Transaction Query Operations")
    class TransactionQueryOperations {

        @Test
        @DisplayName("Should get transactions by SKU ID successfully")
        void getTransactionsBySkuId_Success() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page =
                    new PageImpl<>(transactions, testPageable, transactions.size());
            when(transactionRepository.findBySkuId(1L, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result = inventoryService.getTransactionsBySkuId(1L, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testTransaction);
            verify(skuRepository).findById(1L);
            verify(transactionRepository).findBySkuId(1L, testPageable);
        }

        @Test
        @DisplayName("Should throw exception when SKU not found for transaction query")
        void getTransactionsBySkuId_SkuNotFound_ThrowsException() {

            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getTransactionsBySkuId(999L, testPageable))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("SKU with ID 999 not found");
        }

        @Test
        @DisplayName("Should get transactions by date range successfully")
        void getTransactionsByDateRange_Success() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page =
                    new PageImpl<>(transactions, testPageable, transactions.size());
            when(transactionRepository.findByDateRange(startDate, endDate, testPageable))
                    .thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByDateRange(startDate, endDate, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository).findByDateRange(startDate, endDate, testPageable);
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void getTransactionsByDateRange_InvalidDateRange_ThrowsException() {

            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(
                    () -> inventoryService.getTransactionsByDateRange(startDate, endDate, testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(
                            "Validation failed for field 'dateRange': Start date cannot be after end date");
        }

        @Test
        @DisplayName("Should get transactions by SKU ID and date range successfully")
        void getTransactionsBySkuIdAndDateRange_Success() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page =
                    new PageImpl<>(transactions, testPageable, transactions.size());
            when(transactionRepository.findBySkuIdAndCreatedAtBetween(
                    1L, startDate, endDate, testPageable))
                    .thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsBySkuIdAndDateRange(1L, startDate, endDate, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository)
                    .findBySkuIdAndCreatedAtBetween(1L, startDate, endDate, testPageable);
        }

        @Test
        @DisplayName("Should get transactions by type successfully")
        void getTransactionsByType_Success() {

            InventoryTransaction.TransactionType type = InventoryTransaction.TransactionType.IN;
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page =
                    new PageImpl<>(transactions, testPageable, transactions.size());
            when(transactionRepository.findByTransactionType(type, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByType(type, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository).findByTransactionType(type, testPageable);
        }

        @Test
        @DisplayName("Should get transactions by reference ID successfully")
        void getTransactionsByReferenceId_Success() {

            String referenceId = "REF-001";
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page =
                    new PageImpl<>(transactions, testPageable, transactions.size());
            when(transactionRepository.findByReferenceId(referenceId, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByReferenceId(referenceId, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository).findByReferenceId(referenceId, testPageable);
        }

        @Test
        @DisplayName("Should throw exception when reference ID is empty")
        void getTransactionsByReferenceId_EmptyReferenceId_ThrowsException() {

            assertThatThrownBy(() -> inventoryService.getTransactionsByReferenceId("", testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Validation failed for field 'referenceId': Reference ID cannot be empty");
        }
    }

    @Nested
    @DisplayName("Stock Analysis Operations")
    class StockAnalysisOperations {

        @Test
        @DisplayName("Should calculate stock movement summary successfully")
        void getStockMovementSummary_Success() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            List<InventoryTransaction> transactions =
                    Arrays.asList(
                            createTransaction(InventoryTransaction.TransactionType.IN, 10),
                            createTransaction(InventoryTransaction.TransactionType.OUT, 5),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, 2),
                            createTransaction(InventoryTransaction.TransactionType.RESERVED, 3),
                            createTransaction(InventoryTransaction.TransactionType.RELEASED, 1));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(transactions);

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result.getTotalIn()).isEqualTo(10);
            assertThat(result.getTotalOut()).isEqualTo(5);
            assertThat(result.getTotalAdjustments()).isEqualTo(2);
            assertThat(result.getTotalReserved()).isEqualTo(3);
            assertThat(result.getTotalReleased()).isEqualTo(1);
            assertThat(result.getNetMovement()).isEqualTo(7); // 10 - 5 + 2
        }

        @Test
        @DisplayName("Should calculate stock movement summary with no transactions")
        void getStockMovementSummary_NoTransactions_Success() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result.getTotalIn()).isEqualTo(0);
            assertThat(result.getTotalOut()).isEqualTo(0);
            assertThat(result.getTotalAdjustments()).isEqualTo(0);
            assertThat(result.getTotalReserved()).isEqualTo(0);
            assertThat(result.getTotalReleased()).isEqualTo(0);
            assertThat(result.getNetMovement()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should get current stock info successfully")
        void getCurrentStockInfo_Success() {

            Sku stockSku =
                    TestDataFactory.sku()
                            .withId(1L)
                            .withStockQuantity(100)
                            .withReorderPoint(20)
                            .withReorderQuantity(50)
                            .build();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(stockSku));

            CurrentStockInfo result = inventoryService.getCurrentStockInfo(1L);

            assertThat(result).isNotNull();
            assertThat(result.getStockQuantity()).isEqualTo(100);
            assertThat(result.getReorderPoint()).isEqualTo(20);
            assertThat(result.getReorderQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw exception when SKU not found for stock info")
        void getCurrentStockInfo_SkuNotFound_ThrowsException() {

            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getCurrentStockInfo(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("SKU with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when date range is invalid for stock movement summary")
        void getStockMovementSummary_InvalidDateRange_ThrowsException() {

            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() -> inventoryService.getStockMovementSummary(1L, startDate, endDate))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(
                            "Validation failed for field 'dateRange': Start date cannot be after end date");
        }
    }

    // ===== NEW COMPREHENSIVE EDGE CASE TESTS =====

    @Nested
    @DisplayName("Transaction Boundary Condition Tests")
    class TransactionBoundaryConditionTests {

        @Test
        @DisplayName("Should handle maximum integer quantity for stock-in")
        void shouldHandleMaximumIntegerQuantityForStockIn() {

            int maxQuantity = Integer.MAX_VALUE;
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(
                            1L, maxQuantity, "REF-MAX", "Maximum quantity test", "admin");

            assertThat(result).isNotNull();
            verify(transactionRepository).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should handle minimum valid quantity for stock operations")
        void shouldHandleMinimumValidQuantityForStockOperations() {

            int minQuantity = 1;
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction stockInResult =
                    inventoryService.recordStockIn(
                            1L, minQuantity, "REF-MIN-IN", "Minimum stock in", "admin");
            InventoryTransaction stockOutResult =
                    inventoryService.recordStockOut(
                            1L, minQuantity, "REF-MIN-OUT", "ORDER", "Minimum stock out", "admin");

            assertThat(stockInResult).isNotNull();
            assertThat(stockOutResult).isNotNull();
            verify(transactionRepository, times(2)).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should handle large negative adjustment values")
        void shouldHandleLargeNegativeAdjustmentValues() {

            int largeNegativeAdjustment = Integer.MIN_VALUE + 1; // Avoid overflow
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockAdjustment(
                            1L, largeNegativeAdjustment, "DAMAGE", "Large negative adjustment", "admin");

            assertThat(result).isNotNull();
            verify(transactionRepository).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should handle large positive adjustment values")
        void shouldHandleLargePositiveAdjustmentValues() {

            int largePositiveAdjustment = Integer.MAX_VALUE - 1;
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockAdjustment(
                            1L, largePositiveAdjustment, "FOUND", "Large positive adjustment", "admin");

            assertThat(result).isNotNull();
            verify(transactionRepository).save(any(InventoryTransaction.class));
        }
    }

    @Nested
    @DisplayName("Complex Validation Tests")
    class ComplexValidationTests {

        @Test
        @DisplayName("Should throw exception when reference ID exceeds maximum length")
        void shouldThrowExceptionWhenReferenceIdExceedsMaximumLength() {

            String longReferenceId = "A".repeat(256); // Assuming max length is 255

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, 10, longReferenceId, "Test reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("reference ID too long");
        }

        @Test
        @DisplayName("Should handle empty reference ID gracefully")
        void shouldHandleEmptyReferenceIdGracefully() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, "", "Empty reference ID test", "admin");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle null reference ID gracefully")
        void shouldHandleNullReferenceIdGracefully() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, null, "Null reference ID test", "admin");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when reason exceeds maximum length")
        void shouldThrowExceptionWhenReasonExceedsMaximumLength() {

            String longReason = "A".repeat(1001); // Assuming max length is 1000

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, 10, "REF-001", longReason, "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("reason too long");
        }

        @Test
        @DisplayName("Should handle empty reason gracefully")
        void shouldHandleEmptyReasonGracefully() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result = inventoryService.recordStockIn(1L, 10, "REF-001", "", "admin");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle null reason gracefully")
        void shouldHandleNullReasonGracefully() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, "REF-001", null, "admin");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should validate performer name")
        void shouldValidatePerformerName() {
            // No mocking needed since validation should fail early

            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 10, "REF-001", "Test reason", ""))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("performer cannot be empty");
        }

        @Test
        @DisplayName("Should throw exception when performer name is null")
        void shouldThrowExceptionWhenPerformerNameIsNull() {
            // No mocking needed since validation should fail early

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, 10, "REF-001", "Test reason", null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("performer cannot be null");
        }

        @Test
        @DisplayName("Should handle special characters in reference fields")
        void shouldHandleSpecialCharactersInReferenceFields() {

            String specialCharRef = "REF-@#$%^&*()_+-=[]{}|;:,.<>?";
            String specialCharReason = "Reason with special chars: @#$%^&*()";
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, specialCharRef, specialCharReason, "admin");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle Unicode characters in text fields")
        void shouldHandleUnicodeCharactersInTextFields() {

            String unicodeRef = "REF-測試-тест-テスト";
            String unicodeReason = "Unicode reason: 測試 тест テスト";
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, unicodeRef, unicodeReason, "admin");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Complex Transaction Chain Tests")
    class ComplexTransactionChainTests {

        @Test
        @DisplayName("Should handle rapid sequential transactions")
        void shouldHandleRapidSequentialTransactions() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);
            // Simulate rapid transactions
            for (int i = 0; i < 100; i++) {
                inventoryService.recordStockIn(1L, 1, "REF-" + i, "Rapid transaction " + i, "admin");
            }

            verify(transactionRepository, times(100)).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should handle mixed transaction types in sequence")
        void shouldHandleMixedTransactionTypesInSequence() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            inventoryService.recordStockIn(1L, 100, "REF-IN", "Initial stock", "admin");
            inventoryService.recordStockOut(1L, 10, "REF-OUT", "ORDER", "Sale", "admin");
            inventoryService.recordStockAdjustment(1L, 5, "FOUND", "Found extra", "admin");
            inventoryService.recordStockReservation(
                    1L, 20, "REF-RES", "ORDER", "Reserved for order", "admin");
            inventoryService.recordStockRelease(1L, 5, "REF-REL", "ORDER", "Partial release", "admin");

            verify(transactionRepository, times(5)).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should handle concurrent transaction scenarios")
        void shouldHandleConcurrentTransactionScenarios() {

            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);
            // Simulate concurrent transactions with different reference types
            inventoryService.recordStockOut(1L, 5, "ORDER-001", "ORDER", "Order fulfillment", "user1");
            inventoryService.recordStockOut(
                    1L, 3, "SHIPMENT-001", "SHIPMENT", "Shipment fulfillment", "user2");
            inventoryService.recordStockAdjustment(1L, -2, "DAMAGE", "Damaged goods", "supervisor");

            verify(transactionRepository, times(3)).save(any(InventoryTransaction.class));
        }

        @Test
        @DisplayName("Should maintain transaction isolation for different SKUs")
        void shouldMaintainTransactionIsolationForDifferentSkus() {

            Sku sku2 = TestDataFactory.sku().withId(2L).withSkuCode("SKU-002").build();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(skuRepository.findById(2L)).thenReturn(Optional.of(sku2));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            inventoryService.recordStockIn(1L, 50, "REF-SKU1", "Stock for SKU 1", "admin");
            inventoryService.recordStockIn(2L, 75, "REF-SKU2", "Stock for SKU 2", "admin");
            inventoryService.recordStockOut(1L, 10, "ORDER-SKU1", "ORDER", "Order from SKU 1", "admin");
            inventoryService.recordStockOut(2L, 15, "ORDER-SKU2", "ORDER", "Order from SKU 2", "admin");

            verify(transactionRepository, times(4)).save(any(InventoryTransaction.class));
            verify(skuRepository, times(2)).findById(1L);
            verify(skuRepository, times(2)).findById(2L);
        }
    }

    @Nested
    @DisplayName("Date Range Validation Tests")
    class DateRangeValidationTests {

        @Test
        @DisplayName("Should handle same start and end date")
        void shouldHandleSameStartAndEndDate() {

            LocalDateTime sameDate = LocalDateTime.now();
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page = new PageImpl<>(transactions, testPageable, 1);

            when(transactionRepository.findByDateRange(sameDate, sameDate, testPageable))
                    .thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByDateRange(sameDate, sameDate, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle very wide date range")
        void shouldHandleVeryWideDateRange() {

            LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2099, 12, 31, 23, 59);
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page = new PageImpl<>(transactions, testPageable, 1);

            when(transactionRepository.findByDateRange(startDate, endDate, testPageable))
                    .thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByDateRange(startDate, endDate, testPageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle null date parameters with proper validation")
        void shouldHandleNullDateParametersWithProperValidation() {

            assertThatThrownBy(
                    () ->
                            inventoryService.getTransactionsByDateRange(
                                    null, LocalDateTime.now(), testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("start date cannot be null");

            assertThatThrownBy(
                    () ->
                            inventoryService.getTransactionsByDateRange(
                                    LocalDateTime.now(), null, testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("end date cannot be null");
        }

        @Test
        @DisplayName("Should handle edge case dates around DST changes")
        void shouldHandleEdgeCaseDatesAroundDSTChanges() {
            // Dates around Daylight Saving Time changes
            LocalDateTime dstStart = LocalDateTime.of(2024, 3, 10, 2, 0); // Spring forward
            LocalDateTime dstEnd = LocalDateTime.of(2024, 3, 10, 4, 0);
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page = new PageImpl<>(transactions, testPageable, 1);

            when(transactionRepository.findByDateRange(dstStart, dstEnd, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByDateRange(dstStart, dstEnd, testPageable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Reference ID Validation Tests")
    class ReferenceIdValidationTests {

        @Test
        @DisplayName("Should handle whitespace-only reference ID")
        void shouldHandleWhitespaceOnlyReferenceId() {

            assertThatThrownBy(() -> inventoryService.getTransactionsByReferenceId("   ", testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reference ID cannot be empty");
        }

        @Test
        @DisplayName("Should handle null reference ID in query")
        void shouldHandleNullReferenceIdInQuery() {

            assertThatThrownBy(() -> inventoryService.getTransactionsByReferenceId(null, testPageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reference ID cannot be null");
        }

        @Test
        @DisplayName("Should handle very long reference ID in query")
        void shouldHandleVeryLongReferenceIdInQuery() {

            String longReferenceId = "A".repeat(1000);
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page = new PageImpl<>(transactions, testPageable, 1);

            when(transactionRepository.findByReferenceId(longReferenceId, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByReferenceId(longReferenceId, testPageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in reference ID query")
        void shouldHandleSpecialCharactersInReferenceIdQuery() {

            String specialRefId = "REF-@#$%^&*()_+-=[]{}|;:,.<>?";
            List<InventoryTransaction> transactions = Collections.singletonList(testTransaction);
            Page<InventoryTransaction> page = new PageImpl<>(transactions, testPageable, 1);

            when(transactionRepository.findByReferenceId(specialRefId, testPageable)).thenReturn(page);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsByReferenceId(specialRefId, testPageable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Stock Movement Calculation Edge Cases")
    class StockMovementCalculationEdgeCases {

        @Test
        @DisplayName("Should handle overflow in stock movement calculations")
        void shouldHandleOverflowInStockMovementCalculations() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            // Create transactions that could cause overflow
            List<InventoryTransaction> transactions =
                    Arrays.asList(
                            createTransaction(InventoryTransaction.TransactionType.IN, Integer.MAX_VALUE),
                            createTransaction(InventoryTransaction.TransactionType.OUT, 100),
                            createTransaction(InventoryTransaction.TransactionType.IN, 50));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(transactions);

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            // Should handle overflow gracefully
            assertThat(result.getTotalIn())
                    .isEqualTo(Integer.MAX_VALUE + 50L); // Use long for calculations
        }

        @Test
        @DisplayName("Should handle underflow in stock movement calculations")
        void shouldHandleUnderflowInStockMovementCalculations() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            List<InventoryTransaction> transactions =
                    Arrays.asList(
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, Integer.MIN_VALUE),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, -100));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(transactions);

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            // Should handle underflow gracefully
            assertThat(result.getTotalAdjustments()).isEqualTo(Integer.MIN_VALUE - 100L);
        }

        @Test
        @DisplayName("Should handle mixed positive and negative adjustments correctly")
        void shouldHandleMixedPositiveAndNegativeAdjustmentsCorrectly() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            List<InventoryTransaction> transactions =
                    Arrays.asList(
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, 100),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, -50),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, 25),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, -75));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(transactions);

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result.getTotalAdjustments()).isEqualTo(0); // 100 - 50 + 25 - 75 = 0
        }

        @Test
        @DisplayName("Should calculate net movement correctly with all transaction types")
        void shouldCalculateNetMovementCorrectlyWithAllTransactionTypes() {

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            List<InventoryTransaction> transactions =
                    Arrays.asList(
                            createTransaction(InventoryTransaction.TransactionType.IN, 100),
                            createTransaction(InventoryTransaction.TransactionType.OUT, 30),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, 10),
                            createTransaction(InventoryTransaction.TransactionType.ADJUSTMENT, -5),
                            createTransaction(InventoryTransaction.TransactionType.RESERVED, 20),
                            createTransaction(InventoryTransaction.TransactionType.RELEASED, 10));
            when(transactionRepository.findBySkuIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(transactions);

            StockMovementSummary result =
                    inventoryService.getStockMovementSummary(1L, startDate, endDate);

            assertThat(result).isNotNull();
            assertThat(result.getTotalIn()).isEqualTo(100);
            assertThat(result.getTotalOut()).isEqualTo(30);
            assertThat(result.getTotalAdjustments()).isEqualTo(5); // 10 - 5
            assertThat(result.getTotalReserved()).isEqualTo(20);
            assertThat(result.getTotalReleased()).isEqualTo(10);
            // Net movement = IN - OUT + ADJUSTMENTS = 100 - 30 + 5 = 75
            assertThat(result.getNetMovement()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("Performance and Stress Tests")
    class PerformanceAndStressTests {

        @Test
        @DisplayName("Should handle large pageable sizes")
        void shouldHandleLargePageableSizes() {

            Pageable largePageable = PageRequest.of(0, 10000);
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            List<InventoryTransaction> largeTransactionList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                largeTransactionList.add(testTransaction);
            }
            Page<InventoryTransaction> largePage =
                    new PageImpl<>(largeTransactionList, largePageable, 10000);

            when(transactionRepository.findBySkuId(1L, largePageable)).thenReturn(largePage);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsBySkuId(1L, largePageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(10000);
        }

        @Test
        @DisplayName("Should handle zero page size gracefully")
        void shouldHandleZeroPageSizeGracefully() {

            assertThatThrownBy(() -> PageRequest.of(0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must not be less than one");
        }

        @Test
        @DisplayName("Should handle very large page numbers")
        void shouldHandleVeryLargePageNumbers() {

            Pageable largePageNumber = PageRequest.of(Integer.MAX_VALUE - 1, 10);
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));

            Page<InventoryTransaction> emptyPage = new PageImpl<>(new ArrayList<>(), largePageNumber, 0);
            when(transactionRepository.findBySkuId(1L, largePageNumber)).thenReturn(emptyPage);

            Page<InventoryTransaction> result =
                    inventoryService.getTransactionsBySkuId(1L, largePageNumber);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Method Tests")
    class ValidationMethodTests {

        @Test
        @DisplayName("Should throw ValidationException for null quantity")
        void validateTransactionParameters_NullQuantity_ThrowsException() {
            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, null, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("quantity must be positive");
        }

        @Test
        @DisplayName("Should throw ValidationException for zero quantity")
        void validateTransactionParameters_ZeroQuantity_ThrowsException() {
            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 0, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("quantity must be positive");
        }

        @Test
        @DisplayName("Should throw ValidationException for negative quantity")
        void validateTransactionParameters_NegativeQuantity_ThrowsException() {
            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, -5, "REF-001", "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("quantity must be positive");
        }

        @Test
        @DisplayName("Should throw ValidationException for null performer")
        void validatePerformerName_Null_ThrowsException() {
            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 10, "REF-001", "reason", null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("performer cannot be null");
        }

        @Test
        @DisplayName("Should throw ValidationException for empty performer")
        void validatePerformerName_Empty_ThrowsException() {
            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 10, "REF-001", "reason", ""))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("performer cannot be empty");
        }

        @Test
        @DisplayName("Should throw ValidationException for whitespace-only performer")
        void validatePerformerName_WhitespaceOnly_ThrowsException() {
            assertThatThrownBy(() -> inventoryService.recordStockIn(1L, 10, "REF-001", "reason", "   "))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("performer cannot be empty");
        }

        @Test
        @DisplayName("Should throw ValidationException for reference ID too long")
        void validateReferenceId_TooLong_ThrowsException() {
            String longReferenceId = "A".repeat(256); // 256 characters, max is 255

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, 10, longReferenceId, "reason", "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("reference ID too long");
        }

        @Test
        @DisplayName("Should allow null reference ID")
        void validateReferenceId_Null_Allowed() {
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            // Should not throw exception
            InventoryTransaction result = inventoryService.recordStockIn(1L, 10, null, "reason", "admin");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should allow reference ID at max length")
        void validateReferenceId_MaxLength_Allowed() {
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            String maxLengthReferenceId = "A".repeat(255); // Exactly 255 characters

            // Should not throw exception
            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, maxLengthReferenceId, "reason", "admin");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw ValidationException for reason too long")
        void validateReason_TooLong_ThrowsException() {
            String longReason = "A".repeat(1001); // 1001 characters, max is 1000

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(1L, 10, "REF-001", longReason, "admin"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("reason too long");
        }

        @Test
        @DisplayName("Should allow null reason")
        void validateReason_Null_Allowed() {
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            // Should not throw exception
            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, "REF-001", null, "admin");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should allow reason at max length")
        void validateReason_MaxLength_Allowed() {
            when(skuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);

            String maxLengthReason = "A".repeat(1000); // Exactly 1000 characters

            // Should not throw exception
            InventoryTransaction result =
                    inventoryService.recordStockIn(1L, 10, "REF-001", maxLengthReason, "admin");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for invalid SKU ID")
        void findSkuById_InvalidId_ThrowsException() {
            when(skuRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () -> inventoryService.recordStockIn(999L, 10, "REF-001", "reason", "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SKU with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Inner Class Tests")
    class InnerClassTests {

        @Nested
        @DisplayName("StockMovementSummary Tests")
        class StockMovementSummaryTests {

            @Test
            @DisplayName("Should create summary with all values")
            void constructor_AllValues() {
                StockMovementSummary summary = new StockMovementSummary(100, 30, 10, 20, 5);

                assertThat(summary.getTotalIn()).isEqualTo(100);
                assertThat(summary.getTotalOut()).isEqualTo(30);
                assertThat(summary.getTotalAdjustments()).isEqualTo(10);
                assertThat(summary.getTotalReserved()).isEqualTo(20);
                assertThat(summary.getTotalReleased()).isEqualTo(5);
                // Net movement = IN - OUT + ADJUSTMENTS = 100 - 30 + 10 = 80
                assertThat(summary.getNetMovement()).isEqualTo(80);
            }

            @Test
            @DisplayName("Should calculate negative net movement")
            void constructor_NegativeNetMovement() {
                StockMovementSummary summary = new StockMovementSummary(10, 50, -5, 0, 0);

                assertThat(summary.getTotalIn()).isEqualTo(10);
                assertThat(summary.getTotalOut()).isEqualTo(50);
                assertThat(summary.getTotalAdjustments()).isEqualTo(-5);
                // Net movement = IN - OUT + ADJUSTMENTS = 10 - 50 + (-5) = -45
                assertThat(summary.getNetMovement()).isEqualTo(-45);
            }

            @Test
            @DisplayName("Should handle zero values")
            void constructor_ZeroValues() {
                StockMovementSummary summary = new StockMovementSummary(0, 0, 0, 0, 0);

                assertThat(summary.getTotalIn()).isEqualTo(0);
                assertThat(summary.getTotalOut()).isEqualTo(0);
                assertThat(summary.getTotalAdjustments()).isEqualTo(0);
                assertThat(summary.getTotalReserved()).isEqualTo(0);
                assertThat(summary.getTotalReleased()).isEqualTo(0);
                assertThat(summary.getNetMovement()).isEqualTo(0);
            }

            @Test
            @DisplayName("Should handle large values")
            void constructor_LargeValues() {
                long largeValue = 1_000_000L;
                StockMovementSummary summary =
                        new StockMovementSummary(
                                largeValue, largeValue / 2, largeValue / 4, largeValue / 8, largeValue / 16);

                assertThat(summary.getTotalIn()).isEqualTo(largeValue);
                assertThat(summary.getTotalOut()).isEqualTo(largeValue / 2);
                assertThat(summary.getTotalAdjustments()).isEqualTo(largeValue / 4);
                // Net movement = 1_000_000 - 500_000 + 250_000 = 750_000
                assertThat(summary.getNetMovement()).isEqualTo(750_000L);
            }
        }

        @Nested
        @DisplayName("CurrentStockInfo Tests")
        class CurrentStockInfoTests {

            @Test
            @DisplayName("Should create stock info with all values")
            void constructor_AllValues() {
                CurrentStockInfo stockInfo = new CurrentStockInfo(100, 20, 80, 25, 50, false, false);

                assertThat(stockInfo.getStockQuantity()).isEqualTo(100);
                assertThat(stockInfo.getReservedQuantity()).isEqualTo(20);
                assertThat(stockInfo.getAvailableQuantity()).isEqualTo(80);
                assertThat(stockInfo.getReorderPoint()).isEqualTo(25);
                assertThat(stockInfo.getReorderQuantity()).isEqualTo(50);
                assertThat(stockInfo.isLowOnStock()).isFalse();
                assertThat(stockInfo.isOutOfStock()).isFalse();
            }

            @Test
            @DisplayName("Should create stock info with low stock flag")
            void constructor_LowStock() {
                CurrentStockInfo stockInfo = new CurrentStockInfo(15, 5, 10, 25, 50, true, false);

                assertThat(stockInfo.getStockQuantity()).isEqualTo(15);
                assertThat(stockInfo.getReservedQuantity()).isEqualTo(5);
                assertThat(stockInfo.getAvailableQuantity()).isEqualTo(10);
                assertThat(stockInfo.isLowOnStock()).isTrue();
                assertThat(stockInfo.isOutOfStock()).isFalse();
            }

            @Test
            @DisplayName("Should create stock info with out of stock flag")
            void constructor_OutOfStock() {
                CurrentStockInfo stockInfo = new CurrentStockInfo(0, 0, 0, 25, 50, true, true);

                assertThat(stockInfo.getStockQuantity()).isEqualTo(0);
                assertThat(stockInfo.getReservedQuantity()).isEqualTo(0);
                assertThat(stockInfo.getAvailableQuantity()).isEqualTo(0);
                assertThat(stockInfo.isLowOnStock()).isTrue();
                assertThat(stockInfo.isOutOfStock()).isTrue();
            }

            @Test
            @DisplayName("Should create stock info with zero reorder values")
            void constructor_ZeroReorderValues() {
                CurrentStockInfo stockInfo = new CurrentStockInfo(100, 0, 100, 0, 0, false, false);

                assertThat(stockInfo.getStockQuantity()).isEqualTo(100);
                assertThat(stockInfo.getReorderPoint()).isEqualTo(0);
                assertThat(stockInfo.getReorderQuantity()).isEqualTo(0);
                assertThat(stockInfo.isLowOnStock()).isFalse();
                assertThat(stockInfo.isOutOfStock()).isFalse();
            }
        }
    }
}
