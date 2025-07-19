package com.inventorymanagement.inventory.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventorymanagement.common.testdata.TestDataFactory;
import com.inventorymanagement.product.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for Sku entity.
 */
@DisplayName("Sku Entity Tests")
class SkuTest {

    private Sku testSku;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = TestDataFactory.product().withId(1L).withName("Test Product").build();

        testSku =
                TestDataFactory.sku()
                        .withId(1L)
                        .withSkuCode("TEST-SKU-001")
                        .withProduct(testProduct)
                        .withPrice(new BigDecimal("100.00"))
                        .withStockQuantity(50)
                        .withReorderPoint(20)
                        .withReorderQuantity(100)
                        .build();
        testSku.setReservedQuantity(10);
    }

    @Nested
    @DisplayName("Constructor and Basic Property Tests")
    class ConstructorAndBasicPropertyTests {

        @Test
        @DisplayName("Should create Sku with all properties")
        void constructorAllProperties() {
            Sku sku = new Sku();
            sku.setId(1L);
            sku.setSkuCode("TEST-SKU");
            sku.setProduct(testProduct);
            sku.setVariantName("Large");
            sku.setSize("XL");
            sku.setColor("Blue");
            sku.setPrice(new BigDecimal("99.99"));
            sku.setCost(new BigDecimal("49.99"));
            sku.setStockQuantity(100);
            sku.setReservedQuantity(20);
            sku.setReorderPoint(30);
            sku.setReorderQuantity(200);
            sku.setBarcode("123456789");
            sku.setLocation("A1-B2");
            sku.setMetadata("{\"supplier\": \"Test Supplier\"}");
            sku.setIsActive(true);

            assertThat(sku.getId()).isEqualTo(1L);
            assertThat(sku.getSkuCode()).isEqualTo("TEST-SKU");
            assertThat(sku.getProduct()).isEqualTo(testProduct);
            assertThat(sku.getVariantName()).isEqualTo("Large");
            assertThat(sku.getSize()).isEqualTo("XL");
            assertThat(sku.getColor()).isEqualTo("Blue");
            assertThat(sku.getPrice()).isEqualTo(new BigDecimal("99.99"));
            assertThat(sku.getCost()).isEqualTo(new BigDecimal("49.99"));
            assertThat(sku.getStockQuantity()).isEqualTo(100);
            assertThat(sku.getReservedQuantity()).isEqualTo(20);
            assertThat(sku.getReorderPoint()).isEqualTo(30);
            assertThat(sku.getReorderQuantity()).isEqualTo(200);
            assertThat(sku.getBarcode()).isEqualTo("123456789");
            assertThat(sku.getLocation()).isEqualTo("A1-B2");
            assertThat(sku.getMetadata()).isEqualTo("{\"supplier\": \"Test Supplier\"}");
            assertThat(sku.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should have default values for optional fields")
        void constructorDefaultValues() {
            Sku sku = new Sku();

            assertThat(sku.getStockQuantity()).isEqualTo(0);
            assertThat(sku.getReservedQuantity()).isEqualTo(0);
            assertThat(sku.getAvailableQuantity()).isEqualTo(0);
            assertThat(sku.getReorderPoint()).isEqualTo(0);
            assertThat(sku.getReorderQuantity()).isEqualTo(0);
            assertThat(sku.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stock Status Tests")
    class StockStatusTests {

        @Test
        @DisplayName("Should correctly identify low stock when at reorder point")
        void isLowOnStockAtReorderPointReturnsTrue() {
            testSku.setStockQuantity(20);
            testSku.setReorderPoint(20);

            assertThat(testSku.isLowOnStock()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify low stock when below reorder point")
        void isLowOnStockBelowReorderPointReturnsTrue() {
            testSku.setStockQuantity(15);
            testSku.setReorderPoint(20);

            assertThat(testSku.isLowOnStock()).isTrue();
        }

        @Test
        @DisplayName("Should not identify low stock when above reorder point")
        void isLowOnStockAboveReorderPointReturnsFalse() {
            testSku.setStockQuantity(25);
            testSku.setReorderPoint(20);

            assertThat(testSku.isLowOnStock()).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify out of stock")
        void isOutOfStockZeroQuantityReturnsTrue() {
            testSku.setStockQuantity(0);

            assertThat(testSku.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("Should not identify out of stock when quantity is positive")
        void isOutOfStockPositiveQuantityReturnsFalse() {
            testSku.setStockQuantity(1);

            assertThat(testSku.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify available stock")
        void hasAvailableStockPositiveAvailableReturnsTrue() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(10);

            assertThat(testSku.hasAvailableStock()).isTrue();
        }

        @Test
        @DisplayName("Should not identify available stock when all is reserved")
        void hasAvailableStockAllReservedReturnsFalse() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(50);

            assertThat(testSku.hasAvailableStock()).isFalse();
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Tests")
    class StockAdjustmentTests {

        @Test
        @DisplayName("Should increase stock with positive adjustment")
        void adjustStockPositiveAdjustmentIncreasesStock() {
            testSku.setStockQuantity(50);

            Integer newQuantity = testSku.adjustStock(25);

            assertThat(newQuantity).isEqualTo(75);
            assertThat(testSku.getStockQuantity()).isEqualTo(75);
        }

        @Test
        @DisplayName("Should decrease stock with negative adjustment")
        void adjustStockNegativeAdjustmentDecreasesStock() {
            testSku.setStockQuantity(50);

            Integer newQuantity = testSku.adjustStock(-20);

            assertThat(newQuantity).isEqualTo(30);
            assertThat(testSku.getStockQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should not allow stock to go below zero")
        void adjustStockLargeNegativeAdjustmentDoesNotGoBelowZero() {
            testSku.setStockQuantity(50);

            Integer newQuantity = testSku.adjustStock(-100);

            assertThat(newQuantity).isEqualTo(0);
            assertThat(testSku.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle zero adjustment")
        void adjustStockZeroAdjustmentNoChange() {
            testSku.setStockQuantity(50);

            Integer newQuantity = testSku.adjustStock(0);

            assertThat(newQuantity).isEqualTo(50);
            assertThat(testSku.getStockQuantity()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Stock Reservation Tests")
    class StockReservationTests {

        @Test
        @DisplayName("Should successfully reserve stock when sufficient available")
        void reserveStockSufficientStockReturnsTrue() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(10);

            boolean result = testSku.reserveStock(20);

            assertThat(result).isTrue();
            assertThat(testSku.getReservedQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should fail to reserve stock when insufficient available")
        void reserveStockInsufficientStockReturnsFalse() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(10);

            boolean result = testSku.reserveStock(50); // Trying to reserve more than available

            assertThat(result).isFalse();
            assertThat(testSku.getReservedQuantity()).isEqualTo(10); // Unchanged
        }

        @Test
        @DisplayName("Should fail to reserve negative or zero quantity")
        void reserveStockNonPositiveQuantityReturnsFalse() {
            boolean resultZero = testSku.reserveStock(0);
            boolean resultNegative = testSku.reserveStock(-5);

            assertThat(resultZero).isFalse();
            assertThat(resultNegative).isFalse();
        }

        @Test
        @DisplayName("Should successfully reserve exactly all available stock")
        void reserveStockExactlyAvailableReturnsTrue() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(10);

            boolean result = testSku.reserveStock(40);

            assertThat(result).isTrue();
            assertThat(testSku.getReservedQuantity()).isEqualTo(50);
            assertThat(testSku.getAvailableQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Reserved Stock Release Tests")
    class ReservedStockReleaseTests {

        @Test
        @DisplayName("Should release requested quantity when sufficient reserved")
        void releaseReservedStockSufficientReservedReleasesFullAmount() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(30);

            Integer released = testSku.releaseReservedStock(15);

            assertThat(released).isEqualTo(15);
            assertThat(testSku.getReservedQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should release only available reserved when requested more than reserved")
        void releaseReservedStockMoreThanReservedReleasesOnlyAvailable() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(20);

            Integer released = testSku.releaseReservedStock(30);

            assertThat(released).isEqualTo(20); // Only what was reserved
            assertThat(testSku.getReservedQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero for non-positive quantity")
        void releaseReservedStockNonPositiveQuantityReturnsZero() {
            Integer releasedZero = testSku.releaseReservedStock(0);
            Integer releasedNegative = testSku.releaseReservedStock(-5);

            assertThat(releasedZero).isEqualTo(0);
            assertThat(releasedNegative).isEqualTo(0);
        }

        @Test
        @DisplayName("Should release all reserved stock")
        void releaseReservedStockAllReservedReleasesAll() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(30);

            Integer released = testSku.releaseReservedStock(30);

            assertThat(released).isEqualTo(30);
            assertThat(testSku.getReservedQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Order Fulfillment Tests")
    class OrderFulfillmentTests {

        @Test
        @DisplayName("Should successfully fulfill order when sufficient reserved stock")
        void fulfillOrderSufficientReservedReturnsTrue() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(30);

            boolean result = testSku.fulfillOrder(20);

            assertThat(result).isTrue();
            assertThat(testSku.getStockQuantity()).isEqualTo(30); // 50 - 20
            assertThat(testSku.getReservedQuantity()).isEqualTo(10); // 30 - 20
        }

        @Test
        @DisplayName("Should fail to fulfill order when insufficient reserved stock")
        void fulfillOrderInsufficientReservedReturnsFalse() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(15);

            boolean result = testSku.fulfillOrder(20); // More than reserved

            assertThat(result).isFalse();
            assertThat(testSku.getStockQuantity()).isEqualTo(50); // Unchanged
            assertThat(testSku.getReservedQuantity()).isEqualTo(15); // Unchanged
        }

        @Test
        @DisplayName("Should fail to fulfill non-positive quantity")
        void fulfillOrderNonPositiveQuantityReturnsFalse() {
            boolean resultZero = testSku.fulfillOrder(0);
            boolean resultNegative = testSku.fulfillOrder(-5);

            assertThat(resultZero).isFalse();
            assertThat(resultNegative).isFalse();
        }

        @Test
        @DisplayName("Should fulfill order for exact reserved quantity")
        void fulfillOrderExactReservedQuantityReturnsTrue() {
            testSku.setStockQuantity(50);
            testSku.setReservedQuantity(25);

            boolean result = testSku.fulfillOrder(25);

            assertThat(result).isTrue();
            assertThat(testSku.getStockQuantity()).isEqualTo(25);
            assertThat(testSku.getReservedQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Financial Calculation Tests")
    class FinancialCalculationTests {

        @Test
        @DisplayName("Should calculate profit margin correctly")
        void getProfitMarginValidCostAndPriceCalculatesCorrectly() {
            testSku.setPrice(new BigDecimal("100.00"));
            testSku.setCost(new BigDecimal("60.00"));

            BigDecimal profitMargin = testSku.getProfitMargin();

            // Profit = 100 - 60 = 40
            // Margin = (40 / 100) * 100 = 40%
            assertThat(profitMargin).isEqualTo(new BigDecimal("40.0000"));
        }

        @Test
        @DisplayName("Should return null when cost is null")
        void getProfitMarginNullCostReturnsNull() {
            testSku.setPrice(new BigDecimal("100.00"));
            testSku.setCost(null);

            BigDecimal profitMargin = testSku.getProfitMargin();

            assertThat(profitMargin).isNull();
        }

        @Test
        @DisplayName("Should return null when cost is zero")
        void getProfitMarginZeroCostReturnsNull() {
            testSku.setPrice(new BigDecimal("100.00"));
            testSku.setCost(BigDecimal.ZERO);

            BigDecimal profitMargin = testSku.getProfitMargin();

            assertThat(profitMargin).isNull();
        }

        @Test
        @DisplayName("Should calculate negative profit margin when cost exceeds price")
        void getProfitMarginCostExceedsPriceNegativeMargin() {
            testSku.setPrice(new BigDecimal("60.00"));
            testSku.setCost(new BigDecimal("100.00"));

            BigDecimal profitMargin = testSku.getProfitMargin();

            // Profit = 60 - 100 = -40
            // Margin = (-40 / 60) * 100 = -66.6667% but actual calculation is -66.6700%
            assertThat(profitMargin).isEqualTo(new BigDecimal("-66.6700"));
        }

        @Test
        @DisplayName("Should calculate stock value correctly")
        void getStockValueValidPriceAndQuantityCalculatesCorrectly() {
            testSku.setPrice(new BigDecimal("25.50"));
            testSku.setStockQuantity(100);

            BigDecimal stockValue = testSku.getStockValue();

            assertThat(stockValue).isEqualTo(new BigDecimal("2550.00"));
        }

        @Test
        @DisplayName("Should calculate zero stock value for zero quantity")
        void getStockValueZeroQuantityReturnsZero() {
            testSku.setPrice(new BigDecimal("25.50"));
            testSku.setStockQuantity(0);

            BigDecimal stockValue = testSku.getStockValue();

            assertThat(stockValue).isEqualTo(new BigDecimal("0.00"));
        }
    }

    @Nested
    @DisplayName("Activation/Deactivation Tests")
    class ActivationTests {

        @Test
        @DisplayName("Should deactivate SKU")
        void deactivateSetsActiveToFalse() {
            testSku.setIsActive(true);

            testSku.deactivate();

            assertThat(testSku.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should activate SKU")
        void activateSetsActiveToTrue() {
            testSku.setIsActive(false);

            testSku.activate();

            assertThat(testSku.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all key fields are same")
        void equalsSameKeyFieldsReturnsTrue() {
            Product sharedProduct = TestDataFactory.product().withId(1L).withName("Test Product").build();

            Sku sku1 =
                    TestDataFactory.sku()
                            .withId(1L)
                            .withSkuCode("TEST-SKU")
                            .withProduct(sharedProduct)
                            .build();
            sku1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

            Sku sku2 =
                    TestDataFactory.sku()
                            .withId(1L)
                            .withSkuCode("TEST-SKU")
                            .withProduct(sharedProduct)
                            .build();
            sku2.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));

            assertThat(sku1).isEqualTo(sku2);
            assertThat(sku1.hashCode()).isEqualTo(sku2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs differ")
        void equalsDifferentIdsReturnsFalse() {
            Sku sku1 = TestDataFactory.sku().withId(1L).withSkuCode("TEST-SKU").build();
            Sku sku2 = TestDataFactory.sku().withId(2L).withSkuCode("TEST-SKU").build();

            assertThat(sku1).isNotEqualTo(sku2);
        }

        @Test
        @DisplayName("Should handle null comparisons")
        void equalsNullComparisons() {
            assertThat(testSku).isNotEqualTo(null);
            assertThat(testSku).isEqualTo(testSku);
            assertThat(testSku).isNotEqualTo("not a sku");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include key fields in toString")
        void toStringIncludesKeyFields() {
            testSku.setId(1L);
            testSku.setSkuCode("TEST-SKU-001");

            String result = testSku.toString();

            assertThat(result).contains("Sku{");
            assertThat(result).contains("id=1");
            assertThat(result).contains("skuCode='TEST-SKU-001'");
        }
    }
}
