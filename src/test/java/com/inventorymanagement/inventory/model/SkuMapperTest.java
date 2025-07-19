package com.inventorymanagement.inventory.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.product.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SkuMapper Unit Tests")
class SkuMapperTest {

    private Sku testSku;
    private SkuDto testSkuDto;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        // Setup product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("iPhone 15");
        testProduct.setCategory(testCategory);

        // Setup SKU
        testSku = new Sku();
        testSku.setId(1L);
        testSku.setSkuCode("IPH15-128-BLK");
        testSku.setVariantName("128GB Black");
        testSku.setSize("128GB");
        testSku.setColor("Black");
        testSku.setPrice(new BigDecimal("999.99"));
        testSku.setCost(new BigDecimal("799.99"));
        testSku.setStockQuantity(100);
        testSku.setReservedQuantity(10);
        testSku.setReorderPoint(20);
        testSku.setReorderQuantity(50);
        testSku.setBarcode("1234567890123");
        testSku.setLocation("A1-B2-C3");
        testSku.setIsActive(true);
        testSku.setMetadata("{\"weight\":\"174g\"}");
        testSku.setCreatedAt(now.minusDays(1));
        testSku.setUpdatedAt(now.minusHours(1));
        testSku.setDeletedAt(null);
        testSku.setVersion(1L);
        testSku.setProduct(testProduct);

        // Setup DTO
        testSkuDto = new SkuDto();
        testSkuDto.setId(2L);
        testSkuDto.setSkuCode("IPH15-256-WHT");
        testSkuDto.setProductId(1L);
        testSkuDto.setVariantName("256GB White");
        testSkuDto.setSize("256GB");
        testSkuDto.setColor("White");
        testSkuDto.setPrice(new BigDecimal("1099.99"));
        testSkuDto.setCost(new BigDecimal("899.99"));
        testSkuDto.setStockQuantity(75);
        testSkuDto.setReservedQuantity(5);
        testSkuDto.setReorderPoint(15);
        testSkuDto.setReorderQuantity(40);
        testSkuDto.setBarcode("1234567890124");
        testSkuDto.setLocation("A1-B2-C4");
        testSkuDto.setIsActive(false);
        testSkuDto.setMetadata("{\"weight\":\"174g\"}");
        testSkuDto.setCreatedAt(now);
        testSkuDto.setUpdatedAt(now);
        testSkuDto.setDeletedAt(now.minusHours(2));
        testSkuDto.setVersion(2L);
    }

    @Nested
    @DisplayName("Entity to DTO Conversion Tests")
    class EntityToDtoTests {

        @Test
        @DisplayName("Should convert SKU entity to DTO successfully")
        void toDto_ValidSku_ShouldReturnDto() {
            SkuDto result = SkuMapper.toDto(testSku);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSku.getId());
            assertThat(result.getSkuCode()).isEqualTo(testSku.getSkuCode());
            assertThat(result.getVariantName()).isEqualTo(testSku.getVariantName());
            assertThat(result.getSize()).isEqualTo(testSku.getSize());
            assertThat(result.getColor()).isEqualTo(testSku.getColor());
            assertThat(result.getPrice()).isEqualTo(testSku.getPrice());
            assertThat(result.getCost()).isEqualTo(testSku.getCost());
            assertThat(result.getStockQuantity()).isEqualTo(testSku.getStockQuantity());
            assertThat(result.getReservedQuantity()).isEqualTo(testSku.getReservedQuantity());
            assertThat(result.getReorderPoint()).isEqualTo(testSku.getReorderPoint());
            assertThat(result.getReorderQuantity()).isEqualTo(testSku.getReorderQuantity());
            assertThat(result.getBarcode()).isEqualTo(testSku.getBarcode());
            assertThat(result.getLocation()).isEqualTo(testSku.getLocation());
            assertThat(result.getIsActive()).isEqualTo(testSku.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testSku.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testSku.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testSku.getUpdatedAt());
            assertThat(result.getDeletedAt()).isEqualTo(testSku.getDeletedAt());
            assertThat(result.getVersion()).isEqualTo(testSku.getVersion());

            // Check product information
            assertThat(result.getProductId()).isEqualTo(testSku.getProduct().getId());
            assertThat(result.getProductName()).isEqualTo(testSku.getProduct().getName());
            assertThat(result.getCategoryName()).isEqualTo(testSku.getProduct().getCategory().getName());

            // Check computed fields
            assertThat(result.getAvailableQuantity()).isEqualTo(testSku.getAvailableQuantity());
            assertThat(result.getIsLowOnStock()).isEqualTo(testSku.isLowOnStock());
            assertThat(result.getIsOutOfStock()).isEqualTo(testSku.isOutOfStock());
            assertThat(result.getProfitMargin()).isEqualTo(testSku.getProfitMargin());
            assertThat(result.getStockValue()).isEqualTo(testSku.getStockValue());
        }

        @Test
        @DisplayName("Should handle SKU without product")
        void toDto_SkuWithoutProduct_ShouldSetProductFieldsNull() {
            Sku skuWithoutProduct = new Sku();
            skuWithoutProduct.setId(1L);
            skuWithoutProduct.setSkuCode("TEST-SKU");
            skuWithoutProduct.setPrice(new BigDecimal("99.99"));
            skuWithoutProduct.setStockQuantity(10);
            skuWithoutProduct.setIsActive(true);
            // No product set

            SkuDto result = SkuMapper.toDto(skuWithoutProduct);

            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isNull();
            assertThat(result.getProductName()).isNull();
            assertThat(result.getCategoryName()).isNull();
        }

        @Test
        @DisplayName("Should handle product without category")
        void toDto_ProductWithoutCategory_ShouldSetCategoryNameNull() {
            Product productWithoutCategory = new Product();
            productWithoutCategory.setId(1L);
            productWithoutCategory.setName("Test Product");
            // No category set

            Sku skuWithProductNoCategory = new Sku();
            skuWithProductNoCategory.setId(1L);
            skuWithProductNoCategory.setSkuCode("TEST-SKU");
            skuWithProductNoCategory.setProduct(productWithoutCategory);
            skuWithProductNoCategory.setPrice(new BigDecimal("99.99"));

            SkuDto result = SkuMapper.toDto(skuWithProductNoCategory);

            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(1L);
            assertThat(result.getProductName()).isEqualTo("Test Product");
            assertThat(result.getCategoryName()).isNull();
        }
    }

    @Nested
    @DisplayName("DTO to Entity Conversion Tests")
    class DtoToEntityTests {

        @Test
        @DisplayName("Should convert DTO to SKU entity successfully")
        void toEntity_ValidDto_ShouldReturnEntity() {
            Sku result = SkuMapper.toEntity(testSkuDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSkuDto.getId());
            assertThat(result.getSkuCode()).isEqualTo(testSkuDto.getSkuCode());
            assertThat(result.getVariantName()).isEqualTo(testSkuDto.getVariantName());
            assertThat(result.getSize()).isEqualTo(testSkuDto.getSize());
            assertThat(result.getColor()).isEqualTo(testSkuDto.getColor());
            assertThat(result.getPrice()).isEqualTo(testSkuDto.getPrice());
            assertThat(result.getCost()).isEqualTo(testSkuDto.getCost());
            assertThat(result.getStockQuantity()).isEqualTo(testSkuDto.getStockQuantity());
            assertThat(result.getReservedQuantity()).isEqualTo(testSkuDto.getReservedQuantity());
            assertThat(result.getReorderPoint()).isEqualTo(testSkuDto.getReorderPoint());
            assertThat(result.getReorderQuantity()).isEqualTo(testSkuDto.getReorderQuantity());
            assertThat(result.getBarcode()).isEqualTo(testSkuDto.getBarcode());
            assertThat(result.getLocation()).isEqualTo(testSkuDto.getLocation());
            assertThat(result.getIsActive()).isEqualTo(testSkuDto.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testSkuDto.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testSkuDto.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testSkuDto.getUpdatedAt());
            assertThat(result.getDeletedAt()).isEqualTo(testSkuDto.getDeletedAt());
            assertThat(result.getVersion()).isEqualTo(testSkuDto.getVersion());
        }

        @Test
        @DisplayName("Should handle null DTO")
        void toEntity_NullDto_ShouldReturnNull() {
            Sku result = SkuMapper.toEntity(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle DTO with null fields")
        void toEntity_DtoWithNullFields_ShouldHandleGracefully() {
            SkuDto dtoWithNulls = new SkuDto();
            dtoWithNulls.setId(1L);
            dtoWithNulls.setSkuCode("TEST-SKU");
            dtoWithNulls.setStockQuantity(10);
            dtoWithNulls.setReservedQuantity(100);
            // All other fields are null

            Sku result = SkuMapper.toEntity(dtoWithNulls);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSkuCode()).isEqualTo("TEST-SKU");
            assertThat(result.getVariantName()).isNull();
            assertThat(result.getPrice()).isNull();
            assertThat(result.getIsActive()).isNull();
        }
    }

    @Nested
    @DisplayName("Update Entity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity from DTO successfully")
        void updateEntity_ValidDto_ShouldUpdateFields() {
            Sku existingSku = new Sku();
            existingSku.setId(1L);
            existingSku.setSkuCode("ORIGINAL-SKU");
            existingSku.setPrice(new BigDecimal("50.00"));
            existingSku.setStockQuantity(100);
            existingSku.setIsActive(true);
            existingSku.setReservedQuantity(10);

            SkuDto updateDto = new SkuDto();
            updateDto.setSkuCode("UPDATED-SKU");
            updateDto.setPrice(new BigDecimal("75.00"));
            updateDto.setStockQuantity(150);
            updateDto.setIsActive(false);
            updateDto.setVariantName("Updated Variant");
            updateDto.setReservedQuantity(10);

            SkuMapper.updateEntityFromDto(existingSku, updateDto);

            assertThat(existingSku.getId()).isEqualTo(1L); // ID should not change
            assertThat(existingSku.getSkuCode()).isEqualTo("UPDATED-SKU");
            assertThat(existingSku.getPrice()).isEqualTo(new BigDecimal("75.00"));
            assertThat(existingSku.getStockQuantity()).isEqualTo(150);
            assertThat(existingSku.getIsActive()).isFalse();
            assertThat(existingSku.getVariantName()).isEqualTo("Updated Variant");
        }

        @Test
        @DisplayName("Should handle null entity in update")
        void updateEntity_NullEntity_ShouldThrowException() {
            SkuDto updateDto = new SkuDto();
            updateDto.setSkuCode("TEST");

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        SkuMapper.updateEntityFromDto(null, updateDto);
                    });
        }

        @Test
        @DisplayName("Should handle null DTO in update")
        void updateEntity_NullDto_ShouldThrowException() {
            Sku existingSku = new Sku();
            existingSku.setId(1L);

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        SkuMapper.updateEntityFromDto(existingSku, null);
                    });
        }

        @Test
        @DisplayName("Should not update ID, timestamps or relationship fields")
        void updateEntity_ShouldNotUpdateRestrictedFields() {
            LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
            LocalDateTime originalUpdatedAt = LocalDateTime.now().minusHours(1);
            Long originalVersion = 1L;

            Sku existingSku = new Sku();
            existingSku.setId(1L);
            existingSku.setSkuCode("ORIGINAL-SKU");
            existingSku.setCreatedAt(originalCreatedAt);
            existingSku.setUpdatedAt(originalUpdatedAt);
            existingSku.setVersion(originalVersion);
            existingSku.setStockQuantity(100);
            existingSku.setReservedQuantity(10);

            SkuDto updateDto = new SkuDto();
            updateDto.setId(999L); // Different ID
            updateDto.setSkuCode("UPDATED-SKU");
            updateDto.setCreatedAt(LocalDateTime.now());
            updateDto.setUpdatedAt(LocalDateTime.now());
            updateDto.setVersion(999L);
            updateDto.setStockQuantity(100);
            updateDto.setReservedQuantity(10);

            SkuMapper.updateEntityFromDto(existingSku, updateDto);

            // These fields should not change
            assertThat(existingSku.getId()).isEqualTo(1L);
            assertThat(existingSku.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(existingSku.getUpdatedAt()).isEqualTo(originalUpdatedAt);
            assertThat(existingSku.getVersion()).isEqualTo(originalVersion);

            // This field should change
            assertThat(existingSku.getSkuCode()).isEqualTo("UPDATED-SKU");
        }
    }

    @Nested
    @DisplayName("List Conversion Tests")
    class ListConversionTests {

        @Test
        @DisplayName("Should convert list of entities to DTOs")
        void toDtoList_ValidList_ShouldReturnDtoList() {
            Sku sku2 = new Sku();
            sku2.setId(2L);
            sku2.setSkuCode("SKU-002");
            sku2.setProduct(testProduct);
            sku2.setPrice(new BigDecimal("199.99"));

            List<Sku> skus = Arrays.asList(testSku, sku2);

            List<SkuDto> result = SkuMapper.toDtoList(skus);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(testSku.getId());
            assertThat(result.get(1).getId()).isEqualTo(sku2.getId());
        }

        @Test
        @DisplayName("Should handle null list in toDtoList")
        void toDtoList_NullList_ShouldReturnNull() {
            List<SkuDto> result = SkuMapper.toDtoList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty list in toDtoList")
        void toDtoList_EmptyList_ShouldReturnEmptyList() {
            List<SkuDto> result = SkuMapper.toDtoList(Collections.emptyList());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null entities in list conversion")
        void toDtoList_WithNullEntities_ShouldFilterNulls() {
            Sku sku2 = new Sku();
            sku2.setId(2L);
            sku2.setSkuCode("SKU-002");
            sku2.setPrice(new BigDecimal("999.99"));

            List<Sku> skusWithNull = Arrays.asList(testSku, null, sku2);

            List<SkuDto> result = SkuMapper.toDtoList(skusWithNull);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // null should be filtered out
            assertThat(result.get(0).getId()).isEqualTo(testSku.getId());
            assertThat(result.get(1).getId()).isEqualTo(sku2.getId());
        }

        @Test
        @DisplayName("Should convert list of DTOs to entities")
        void toEntityList_ValidList_ShouldReturnEntityList() {
            SkuDto dto1 = new SkuDto();
            dto1.setId(1L);
            dto1.setSkuCode("SKU-001");
            dto1.setStockQuantity(10);
            dto1.setReservedQuantity(1);

            SkuDto dto2 = new SkuDto();
            dto2.setId(2L);
            dto2.setSkuCode("SKU-002");
            dto2.setStockQuantity(20);
            dto2.setReservedQuantity(2);

            List<SkuDto> dtos = Arrays.asList(dto1, dto2);

            List<Sku> result = SkuMapper.toEntityList(dtos);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle null list in toEntityList")
        void toEntityList_NullList_ShouldReturnNull() {
            List<Sku> result = SkuMapper.toEntityList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty list in toEntityList")
        void toEntityList_EmptyList_ShouldReturnEmptyList() {
            List<Sku> result = SkuMapper.toEntityList(Collections.emptyList());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null DTOs in list conversion")
        void toEntityList_WithNullDtos_ShouldFilterNulls() {
            SkuDto dto1 = new SkuDto();
            dto1.setId(1L);
            dto1.setSkuCode("SKU-001");
            dto1.setStockQuantity(10);
            dto1.setReservedQuantity(1);

            SkuDto dto2 = new SkuDto();
            dto2.setId(2L);
            dto2.setSkuCode("SKU-002");
            dto2.setStockQuantity(20);
            dto2.setReservedQuantity(2);

            List<SkuDto> dtosWithNull = Arrays.asList(dto1, null, dto2);

            List<Sku> result = SkuMapper.toEntityList(dtosWithNull);
            System.out.println("Result size: " + result.size());

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // null should be filtered out
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }
    }
}
