package com.inventorymanagement.inventory.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SkuDto Unit Tests")
class SkuDtoTest {

    private SkuDto testSkuDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        testSkuDto = new SkuDto();
        testSkuDto.setId(1L);
        testSkuDto.setSkuCode("TEST-SKU-001");
        testSkuDto.setProductId(100L);
        testSkuDto.setProductName("Test Product");
        testSkuDto.setCategoryName("Test Category");
        testSkuDto.setVariantName("Test Variant");
        testSkuDto.setSize("Large");
        testSkuDto.setColor("Red");
        testSkuDto.setPrice(new BigDecimal("99.99"));
        testSkuDto.setCost(new BigDecimal("79.99"));
        testSkuDto.setStockQuantity(100);
        testSkuDto.setReservedQuantity(10);
        testSkuDto.setAvailableQuantity(90);
        testSkuDto.setReorderPoint(20);
        testSkuDto.setReorderQuantity(50);
        testSkuDto.setBarcode("1234567890123");
        testSkuDto.setLocation("A1-B2-C3");
        testSkuDto.setIsActive(true);
        testSkuDto.setMetadata("{\"weight\":\"500g\"}");
        testSkuDto.setIsLowOnStock(false);
        testSkuDto.setIsOutOfStock(false);
        testSkuDto.setProfitMargin(new BigDecimal("20.00"));
        testSkuDto.setStockValue(new BigDecimal("7999.00"));
        testSkuDto.setCreatedAt(testDateTime);
        testSkuDto.setUpdatedAt(testDateTime);
        testSkuDto.setDeletedAt(null);
        testSkuDto.setVersion(1L);
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id")
        void setGetId_ShouldWork() {
            testSkuDto.setId(999L);

            assertThat(testSkuDto.getId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("Should set and get skuCode")
        void setGetSkuCode_ShouldWork() {
            testSkuDto.setSkuCode("NEW-SKU-CODE");

            assertThat(testSkuDto.getSkuCode()).isEqualTo("NEW-SKU-CODE");
        }

        @Test
        @DisplayName("Should set and get productId")
        void setGetProductId_ShouldWork() {
            testSkuDto.setProductId(555L);

            assertThat(testSkuDto.getProductId()).isEqualTo(555L);
        }

        @Test
        @DisplayName("Should set and get productName")
        void setGetProductName_ShouldWork() {
            testSkuDto.setProductName("New Product Name");

            assertThat(testSkuDto.getProductName()).isEqualTo("New Product Name");
        }

        @Test
        @DisplayName("Should set and get categoryName")
        void setGetCategoryName_ShouldWork() {
            testSkuDto.setCategoryName("New Category");

            assertThat(testSkuDto.getCategoryName()).isEqualTo("New Category");
        }

        @Test
        @DisplayName("Should set and get variantName")
        void setGetVariantName_ShouldWork() {
            testSkuDto.setVariantName("New Variant");

            assertThat(testSkuDto.getVariantName()).isEqualTo("New Variant");
        }

        @Test
        @DisplayName("Should set and get size")
        void setGetSize_ShouldWork() {
            testSkuDto.setSize("XL");

            assertThat(testSkuDto.getSize()).isEqualTo("XL");
        }

        @Test
        @DisplayName("Should set and get color")
        void setGetColor_ShouldWork() {
            testSkuDto.setColor("Blue");

            assertThat(testSkuDto.getColor()).isEqualTo("Blue");
        }

        @Test
        @DisplayName("Should set and get price")
        void setGetPrice_ShouldWork() {
            BigDecimal newPrice = new BigDecimal("199.99");
            testSkuDto.setPrice(newPrice);

            assertThat(testSkuDto.getPrice()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("Should set and get cost")
        void setGetCost_ShouldWork() {
            BigDecimal newCost = new BigDecimal("149.99");
            testSkuDto.setCost(newCost);

            assertThat(testSkuDto.getCost()).isEqualTo(newCost);
        }

        @Test
        @DisplayName("Should set and get stockQuantity")
        void setGetStockQuantity_ShouldWork() {
            testSkuDto.setStockQuantity(200);

            assertThat(testSkuDto.getStockQuantity()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should set and get reservedQuantity")
        void setGetReservedQuantity_ShouldWork() {
            testSkuDto.setReservedQuantity(25);

            assertThat(testSkuDto.getReservedQuantity()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should set and get availableQuantity")
        void setGetAvailableQuantity_ShouldWork() {
            testSkuDto.setAvailableQuantity(175);

            assertThat(testSkuDto.getAvailableQuantity()).isEqualTo(175);
        }

        @Test
        @DisplayName("Should set and get reorderPoint")
        void setGetReorderPoint_ShouldWork() {
            testSkuDto.setReorderPoint(15);

            assertThat(testSkuDto.getReorderPoint()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should set and get reorderQuantity")
        void setGetReorderQuantity_ShouldWork() {
            testSkuDto.setReorderQuantity(75);

            assertThat(testSkuDto.getReorderQuantity()).isEqualTo(75);
        }

        @Test
        @DisplayName("Should set and get barcode")
        void setGetBarcode_ShouldWork() {
            testSkuDto.setBarcode("9876543210987");

            assertThat(testSkuDto.getBarcode()).isEqualTo("9876543210987");
        }

        @Test
        @DisplayName("Should set and get location")
        void setGetLocation_ShouldWork() {
            testSkuDto.setLocation("B1-C2-D3");

            assertThat(testSkuDto.getLocation()).isEqualTo("B1-C2-D3");
        }

        @Test
        @DisplayName("Should set and get isActive")
        void setGetIsActive_ShouldWork() {
            testSkuDto.setIsActive(false);

            assertThat(testSkuDto.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get metadata")
        void setGetMetadata_ShouldWork() {
            testSkuDto.setMetadata("{\"temperature\":\"10C\"}");

            assertThat(testSkuDto.getMetadata()).isEqualTo("{\"temperature\":\"10C\"}");
        }

        @Test
        @DisplayName("Should set and get isLowOnStock")
        void setGetIsLowOnStock_ShouldWork() {
            testSkuDto.setIsLowOnStock(true);

            assertThat(testSkuDto.getIsLowOnStock()).isTrue();
        }

        @Test
        @DisplayName("Should set and get isOutOfStock")
        void setGetIsOutOfStock_ShouldWork() {
            testSkuDto.setIsOutOfStock(true);

            assertThat(testSkuDto.getIsOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("Should set and get profitMargin")
        void setGetProfitMargin_ShouldWork() {
            BigDecimal margin = new BigDecimal("25.50");
            testSkuDto.setProfitMargin(margin);

            assertThat(testSkuDto.getProfitMargin()).isEqualTo(margin);
        }

        @Test
        @DisplayName("Should set and get stockValue")
        void setGetStockValue_ShouldWork() {
            BigDecimal value = new BigDecimal("15000.00");
            testSkuDto.setStockValue(value);

            assertThat(testSkuDto.getStockValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("Should set and get createdAt")
        void setGetCreatedAt_ShouldWork() {
            LocalDateTime newTime = LocalDateTime.now().plusDays(1);
            testSkuDto.setCreatedAt(newTime);

            assertThat(testSkuDto.getCreatedAt()).isEqualTo(newTime);
        }

        @Test
        @DisplayName("Should set and get updatedAt")
        void setGetUpdatedAt_ShouldWork() {
            LocalDateTime newTime = LocalDateTime.now().plusHours(2);
            testSkuDto.setUpdatedAt(newTime);

            assertThat(testSkuDto.getUpdatedAt()).isEqualTo(newTime);
        }

        @Test
        @DisplayName("Should set and get deletedAt")
        void setGetDeletedAt_ShouldWork() {
            LocalDateTime deletedTime = LocalDateTime.now().minusHours(1);
            testSkuDto.setDeletedAt(deletedTime);

            assertThat(testSkuDto.getDeletedAt()).isEqualTo(deletedTime);
        }

        @Test
        @DisplayName("Should set and get version")
        void setGetVersion_ShouldWork() {
            testSkuDto.setVersion(5L);

            assertThat(testSkuDto.getVersion()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Null Value Handling Tests")
    class NullValueHandlingTests {

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValues() {
            SkuDto nullFieldsDto = new SkuDto();

            // Set all fields to null explicitly
            nullFieldsDto.setId(null);
            nullFieldsDto.setSkuCode(null);
            nullFieldsDto.setProductId(null);
            nullFieldsDto.setProductName(null);
            nullFieldsDto.setCategoryName(null);
            nullFieldsDto.setVariantName(null);
            nullFieldsDto.setSize(null);
            nullFieldsDto.setColor(null);
            nullFieldsDto.setPrice(null);
            nullFieldsDto.setCost(null);
            nullFieldsDto.setStockQuantity(null);
            nullFieldsDto.setReservedQuantity(null);
            nullFieldsDto.setAvailableQuantity(null);
            nullFieldsDto.setReorderPoint(null);
            nullFieldsDto.setReorderQuantity(null);
            nullFieldsDto.setBarcode(null);
            nullFieldsDto.setLocation(null);
            nullFieldsDto.setIsActive(null);
            nullFieldsDto.setMetadata(null);
            nullFieldsDto.setIsLowOnStock(null);
            nullFieldsDto.setIsOutOfStock(null);
            nullFieldsDto.setProfitMargin(null);
            nullFieldsDto.setStockValue(null);
            nullFieldsDto.setCreatedAt(null);
            nullFieldsDto.setUpdatedAt(null);
            nullFieldsDto.setDeletedAt(null);
            nullFieldsDto.setVersion(null);

            // All getters should return null
            assertThat(nullFieldsDto.getId()).isNull();
            assertThat(nullFieldsDto.getSkuCode()).isNull();
            assertThat(nullFieldsDto.getProductId()).isNull();
            assertThat(nullFieldsDto.getProductName()).isNull();
            assertThat(nullFieldsDto.getCategoryName()).isNull();
            assertThat(nullFieldsDto.getVariantName()).isNull();
            assertThat(nullFieldsDto.getSize()).isNull();
            assertThat(nullFieldsDto.getColor()).isNull();
            assertThat(nullFieldsDto.getPrice()).isNull();
            assertThat(nullFieldsDto.getCost()).isNull();
            assertThat(nullFieldsDto.getStockQuantity()).isNull();
            assertThat(nullFieldsDto.getReservedQuantity()).isNull();
            assertThat(nullFieldsDto.getAvailableQuantity()).isNull();
            assertThat(nullFieldsDto.getReorderPoint()).isNull();
            assertThat(nullFieldsDto.getReorderQuantity()).isNull();
            assertThat(nullFieldsDto.getBarcode()).isNull();
            assertThat(nullFieldsDto.getLocation()).isNull();
            assertThat(nullFieldsDto.getIsActive()).isNull();
            assertThat(nullFieldsDto.getMetadata()).isNull();
            assertThat(nullFieldsDto.getIsLowOnStock()).isNull();
            assertThat(nullFieldsDto.getIsOutOfStock()).isNull();
            assertThat(nullFieldsDto.getProfitMargin()).isNull();
            assertThat(nullFieldsDto.getStockValue()).isNull();
            assertThat(nullFieldsDto.getCreatedAt()).isNull();
            assertThat(nullFieldsDto.getUpdatedAt()).isNull();
            assertThat(nullFieldsDto.getDeletedAt()).isNull();
            assertThat(nullFieldsDto.getVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            testSkuDto.setPrice(BigDecimal.ZERO);
            testSkuDto.setCost(BigDecimal.ZERO);
            testSkuDto.setStockQuantity(0);
            testSkuDto.setReservedQuantity(0);
            testSkuDto.setAvailableQuantity(0);
            testSkuDto.setReorderPoint(0);
            testSkuDto.setReorderQuantity(0);
            testSkuDto.setProfitMargin(BigDecimal.ZERO);
            testSkuDto.setStockValue(BigDecimal.ZERO);
            testSkuDto.setVersion(0L);

            assertThat(testSkuDto.getPrice()).isEqualTo(BigDecimal.ZERO);
            assertThat(testSkuDto.getCost()).isEqualTo(BigDecimal.ZERO);
            assertThat(testSkuDto.getStockQuantity()).isEqualTo(0);
            assertThat(testSkuDto.getReservedQuantity()).isEqualTo(0);
            assertThat(testSkuDto.getAvailableQuantity()).isEqualTo(0);
            assertThat(testSkuDto.getReorderPoint()).isEqualTo(0);
            assertThat(testSkuDto.getReorderQuantity()).isEqualTo(0);
            assertThat(testSkuDto.getProfitMargin()).isEqualTo(BigDecimal.ZERO);
            assertThat(testSkuDto.getStockValue()).isEqualTo(BigDecimal.ZERO);
            assertThat(testSkuDto.getVersion()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle negative values")
        void shouldHandleNegativeValues() {
            testSkuDto.setStockQuantity(-1);
            testSkuDto.setReservedQuantity(-5);
            testSkuDto.setAvailableQuantity(-10);
            testSkuDto.setReorderPoint(-2);
            testSkuDto.setReorderQuantity(-20);

            assertThat(testSkuDto.getStockQuantity()).isEqualTo(-1);
            assertThat(testSkuDto.getReservedQuantity()).isEqualTo(-5);
            assertThat(testSkuDto.getAvailableQuantity()).isEqualTo(-10);
            assertThat(testSkuDto.getReorderPoint()).isEqualTo(-2);
            assertThat(testSkuDto.getReorderQuantity()).isEqualTo(-20);
        }

        @Test
        @DisplayName("Should handle very large values")
        void shouldHandleVeryLargeValues() {
            BigDecimal largePrice = new BigDecimal("999999999.99");
            Integer largeQuantity = Integer.MAX_VALUE;
            Long largeVersion = Long.MAX_VALUE;

            testSkuDto.setPrice(largePrice);
            testSkuDto.setStockQuantity(largeQuantity);
            testSkuDto.setVersion(largeVersion);

            assertThat(testSkuDto.getPrice()).isEqualTo(largePrice);
            assertThat(testSkuDto.getStockQuantity()).isEqualTo(largeQuantity);
            assertThat(testSkuDto.getVersion()).isEqualTo(largeVersion);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            testSkuDto.setSkuCode("");
            testSkuDto.setProductName("");
            testSkuDto.setCategoryName("");
            testSkuDto.setVariantName("");
            testSkuDto.setSize("");
            testSkuDto.setColor("");
            testSkuDto.setBarcode("");
            testSkuDto.setLocation("");
            testSkuDto.setMetadata("");

            assertThat(testSkuDto.getSkuCode()).isEmpty();
            assertThat(testSkuDto.getProductName()).isEmpty();
            assertThat(testSkuDto.getCategoryName()).isEmpty();
            assertThat(testSkuDto.getVariantName()).isEmpty();
            assertThat(testSkuDto.getSize()).isEmpty();
            assertThat(testSkuDto.getColor()).isEmpty();
            assertThat(testSkuDto.getBarcode()).isEmpty();
            assertThat(testSkuDto.getLocation()).isEmpty();
            assertThat(testSkuDto.getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            String longString = "A".repeat(1000);

            testSkuDto.setSkuCode(longString);
            testSkuDto.setProductName(longString);
            testSkuDto.setCategoryName(longString);
            testSkuDto.setVariantName(longString);
            testSkuDto.setSize(longString);
            testSkuDto.setColor(longString);
            testSkuDto.setBarcode(longString);
            testSkuDto.setLocation(longString);
            testSkuDto.setMetadata(longString);

            assertThat(testSkuDto.getSkuCode()).isEqualTo(longString);
            assertThat(testSkuDto.getProductName()).isEqualTo(longString);
            assertThat(testSkuDto.getCategoryName()).isEqualTo(longString);
            assertThat(testSkuDto.getVariantName()).isEqualTo(longString);
            assertThat(testSkuDto.getSize()).isEqualTo(longString);
            assertThat(testSkuDto.getColor()).isEqualTo(longString);
            assertThat(testSkuDto.getBarcode()).isEqualTo(longString);
            assertThat(testSkuDto.getLocation()).isEqualTo(longString);
            assertThat(testSkuDto.getMetadata()).isEqualTo(longString);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            assertThat(testSkuDto).isEqualTo(testSkuDto);
            assertThat(testSkuDto.hashCode()).isEqualTo(testSkuDto.hashCode());
        }

        @Test
        @DisplayName("Should be equal to another instance with same values")
        void shouldBeEqualToAnotherInstanceWithSameValues() {
            SkuDto otherDto = new SkuDto();
            otherDto.setId(testSkuDto.getId());
            otherDto.setSkuCode(testSkuDto.getSkuCode());
            otherDto.setProductId(testSkuDto.getProductId());
            otherDto.setProductName(testSkuDto.getProductName());
            otherDto.setCategoryName(testSkuDto.getCategoryName());
            otherDto.setVariantName(testSkuDto.getVariantName());
            otherDto.setSize(testSkuDto.getSize());
            otherDto.setColor(testSkuDto.getColor());
            otherDto.setPrice(testSkuDto.getPrice());
            otherDto.setCost(testSkuDto.getCost());
            otherDto.setStockQuantity(testSkuDto.getStockQuantity());
            otherDto.setReservedQuantity(testSkuDto.getReservedQuantity());
            otherDto.setAvailableQuantity(testSkuDto.getAvailableQuantity());
            otherDto.setReorderPoint(testSkuDto.getReorderPoint());
            otherDto.setReorderQuantity(testSkuDto.getReorderQuantity());
            otherDto.setBarcode(testSkuDto.getBarcode());
            otherDto.setLocation(testSkuDto.getLocation());
            otherDto.setIsActive(testSkuDto.getIsActive());
            otherDto.setMetadata(testSkuDto.getMetadata());
            otherDto.setIsLowOnStock(testSkuDto.getIsLowOnStock());
            otherDto.setIsOutOfStock(testSkuDto.getIsOutOfStock());
            otherDto.setProfitMargin(testSkuDto.getProfitMargin());
            otherDto.setStockValue(testSkuDto.getStockValue());
            otherDto.setCreatedAt(testSkuDto.getCreatedAt());
            otherDto.setUpdatedAt(testSkuDto.getUpdatedAt());
            otherDto.setDeletedAt(testSkuDto.getDeletedAt());
            otherDto.setVersion(testSkuDto.getVersion());

            assertThat(testSkuDto).isEqualTo(otherDto);
            assertThat(testSkuDto.hashCode()).isEqualTo(otherDto.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            assertThat(testSkuDto).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            assertThat(testSkuDto).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should not be equal when ID differs")
        void shouldNotBeEqualWhenIdDiffers() {
            SkuDto otherDto = new SkuDto();
            otherDto.setId(999L);
            otherDto.setSkuCode(testSkuDto.getSkuCode());

            assertThat(testSkuDto).isNotEqualTo(otherDto);
        }

        @Test
        @DisplayName("Should not be equal when SKU code differs")
        void shouldNotBeEqualWhenSkuCodeDiffers() {
            SkuDto otherDto = new SkuDto();
            otherDto.setId(testSkuDto.getId());
            otherDto.setSkuCode("DIFFERENT-SKU");

            assertThat(testSkuDto).isNotEqualTo(otherDto);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            SkuDto nullDto = new SkuDto();

            String result = nullDto.toString();

            assertThat(result).isNotNull();
            assertThat(result).contains("SkuDto");
        }
    }
}
