package com.inventorymanagement.product.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.inventory.model.Sku;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductMapper Unit Tests")
class ProductMapperTest {

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;
    private Sku testSku1;
    private Sku testSku2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setPath("/1/");

        // Setup SKUs
        testSku1 = new Sku();
        testSku1.setId(1L);
        testSku1.setSkuCode("PROD-001-BLK");
        testSku1.setPrice(new BigDecimal("999.99"));
        testSku1.setStockQuantity(50);
        testSku1.setReservedQuantity(5);
        testSku1.setIsActive(true);

        testSku2 = new Sku();
        testSku2.setId(2L);
        testSku2.setSkuCode("PROD-001-WHT");
        testSku2.setPrice(new BigDecimal("1099.99"));
        testSku2.setStockQuantity(30);
        testSku2.setReservedQuantity(3);
        testSku2.setIsActive(true);

        // Setup product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("iPhone 15");
        testProduct.setDescription("Latest iPhone model");
        testProduct.setBrand("Apple");
        testProduct.setManufacturer("Apple Inc.");
        testProduct.setWeight(new BigDecimal("174"));
        testProduct.setDimensions("147.6 x 71.6 x 7.80 mm");
        testProduct.setColor("Multi");
        testProduct.setMaterial("Glass and Aluminum");
        testProduct.setIsActive(true);
        testProduct.setMetadata("{\"series\":\"iPhone\"}");
        testProduct.setCreatedAt(now.minusDays(1));
        testProduct.setUpdatedAt(now.minusHours(1));
        testProduct.setDeletedAt(null);
        testProduct.setVersion(1L);
        testProduct.setCategory(testCategory);
        testProduct.getSkus().addAll(Arrays.asList(testSku1, testSku2));

        // Set product reference in SKUs
        testSku1.setProduct(testProduct);
        testSku2.setProduct(testProduct);

        // Setup DTO
        testProductDto = new ProductDto();
        testProductDto.setId(2L);
        testProductDto.setName("Samsung Galaxy S24");
        testProductDto.setDescription("Latest Samsung phone");
        testProductDto.setCategoryId(1L);
        testProductDto.setBrand("Samsung");
        testProductDto.setManufacturer("Samsung Electronics");
        testProductDto.setWeight(new BigDecimal("167"));
        testProductDto.setDimensions("147.0 x 70.6 x 7.6 mm");
        testProductDto.setColor("Black");
        testProductDto.setMaterial("Glass and Metal");
        testProductDto.setIsActive(false);
        testProductDto.setMetadata("{\"series\":\"Galaxy\"}");
        testProductDto.setCreatedAt(now);
        testProductDto.setUpdatedAt(now);
        testProductDto.setDeletedAt(now.minusHours(2));
        testProductDto.setVersion(2L);
    }

    @Nested
    @DisplayName("Entity to DTO Conversion Tests")
    class EntityToDtoTests {

        @Test
        @DisplayName("Should convert product entity to DTO successfully")
        void toDto_ValidProduct_ShouldReturnDto() {
            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testProduct.getId());
            assertThat(result.getName()).isEqualTo(testProduct.getName());
            assertThat(result.getDescription()).isEqualTo(testProduct.getDescription());
            assertThat(result.getBrand()).isEqualTo(testProduct.getBrand());
            assertThat(result.getManufacturer()).isEqualTo(testProduct.getManufacturer());
            assertThat(result.getWeight()).isEqualTo(testProduct.getWeight());
            assertThat(result.getDimensions()).isEqualTo(testProduct.getDimensions());
            assertThat(result.getColor()).isEqualTo(testProduct.getColor());
            assertThat(result.getMaterial()).isEqualTo(testProduct.getMaterial());
            assertThat(result.getIsActive()).isEqualTo(testProduct.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testProduct.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testProduct.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testProduct.getUpdatedAt());
            assertThat(result.getDeletedAt()).isEqualTo(testProduct.getDeletedAt());
            assertThat(result.getVersion()).isEqualTo(testProduct.getVersion());
        }

        @Test
        @DisplayName("Should set category information when product has category")
        void toDto_ProductWithCategory_ShouldSetCategoryInfo() {
            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result.getCategoryId()).isEqualTo(testCategory.getId());
            assertThat(result.getCategoryName()).isEqualTo(testCategory.getName());
            assertThat(result.getCategoryPath()).isEqualTo(testCategory.getPath());
        }

        @Test
        @DisplayName("Should set computed fields correctly")
        void toDto_ProductWithSkus_ShouldSetComputedFields() {
            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result.getSkuCount()).isEqualTo(testProduct.getSkus().size());
            assertThat(result.getActiveSkuCount()).isEqualTo(testProduct.getActiveSkuCount());
            assertThat(result.getTotalStockQuantity()).isEqualTo(testProduct.getTotalStockQuantity());
            assertThat(result.getTotalAvailableQuantity())
                    .isEqualTo(testProduct.getTotalAvailableQuantity());
            assertThat(result.getIsLowOnStock()).isEqualTo(testProduct.isLowOnStock());
        }

        @Test
        @DisplayName("Should set price range when product has SKUs")
        void toDto_ProductWithSkus_ShouldSetPriceRange() {
            ProductDto result = ProductMapper.toDto(testProduct);

            BigDecimal[] priceRange = testProduct.getPriceRange();
            assertThat(result.getMinPrice()).isEqualTo(priceRange[0]);
            assertThat(result.getMaxPrice()).isEqualTo(priceRange[1]);
        }

        @Test
        @DisplayName("Should return null when product is null")
        void toDto_NullProduct_ShouldReturnNull() {
            ProductDto result = ProductMapper.toDto(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle product without category")
        void toDto_ProductWithoutCategory_ShouldNotSetCategoryInfo() {
            testProduct.setCategory(null);

            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result.getCategoryId()).isNull();
            assertThat(result.getCategoryName()).isNull();
            assertThat(result.getCategoryPath()).isNull();
        }

        @Test
        @DisplayName("Should handle product without SKUs")
        void toDto_ProductWithoutSkus_ShouldSetZeroValues() {
            testProduct.getSkus().clear();

            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result.getSkuCount()).isEqualTo(0);
            assertThat(result.getActiveSkuCount()).isEqualTo(0);
            assertThat(result.getTotalStockQuantity()).isEqualTo(0);
            assertThat(result.getTotalAvailableQuantity()).isEqualTo(0);
            assertThat(result.getMinPrice()).isNull();
            assertThat(result.getMaxPrice()).isNull();
        }

        @Test
        @DisplayName("Should handle product with null price range")
        void toDto_ProductWithNullPriceRange_ShouldNotSetPrices() {
            testProduct.getSkus().clear(); // This will make price range null

            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result.getMinPrice()).isNull();
            assertThat(result.getMaxPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("DTO to Entity Conversion Tests")
    class DtoToEntityTests {

        @Test
        @DisplayName("Should convert DTO to product entity successfully")
        void toEntity_ValidDto_ShouldReturnEntity() {
            Product result = ProductMapper.toEntity(testProductDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testProductDto.getId());
            assertThat(result.getName()).isEqualTo(testProductDto.getName());
            assertThat(result.getDescription()).isEqualTo(testProductDto.getDescription());
            assertThat(result.getBrand()).isEqualTo(testProductDto.getBrand());
            assertThat(result.getManufacturer()).isEqualTo(testProductDto.getManufacturer());
            assertThat(result.getWeight()).isEqualTo(testProductDto.getWeight());
            assertThat(result.getDimensions()).isEqualTo(testProductDto.getDimensions());
            assertThat(result.getColor()).isEqualTo(testProductDto.getColor());
            assertThat(result.getMaterial()).isEqualTo(testProductDto.getMaterial());
            assertThat(result.getIsActive()).isEqualTo(testProductDto.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testProductDto.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testProductDto.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testProductDto.getUpdatedAt());
            assertThat(result.getDeletedAt()).isEqualTo(testProductDto.getDeletedAt());
            assertThat(result.getVersion()).isEqualTo(testProductDto.getVersion());
        }

        @Test
        @DisplayName("Should return null when DTO is null")
        void toEntity_NullDto_ShouldReturnNull() {
            Product result = ProductMapper.toEntity(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle DTO with null values")
        void toEntity_DtoWithNullValues_ShouldCreateEntity() {
            ProductDto dto = new ProductDto();
            dto.setName("Test Product");
            dto.setCategoryId(1L);
            // Leave other fields as null

            Product result = ProductMapper.toEntity(dto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getBrand()).isNull();
            assertThat(result.getManufacturer()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity Update from DTO Tests")
    class EntityUpdateTests {

        @Test
        @DisplayName("Should update entity from DTO successfully")
        void updateEntityFromDto_ValidInputs_ShouldUpdateEntity() {
            Product entityToUpdate = new Product();
            entityToUpdate.setId(1L);
            entityToUpdate.setName("Old Product");
            entityToUpdate.setDescription("Old Description");
            entityToUpdate.setBrand("Old Brand");
            entityToUpdate.setIsActive(true);

            ProductDto updateDto = new ProductDto();
            updateDto.setName("New Product");
            updateDto.setDescription("New Description");
            updateDto.setBrand("New Brand");
            updateDto.setManufacturer("New Manufacturer");
            updateDto.setWeight(new BigDecimal("200"));
            updateDto.setDimensions("150x75x8mm");
            updateDto.setColor("Blue");
            updateDto.setMaterial("Plastic");
            updateDto.setIsActive(false);
            updateDto.setMetadata("{\"updated\":true}");

            ProductMapper.updateEntityFromDto(entityToUpdate, updateDto);

            assertThat(entityToUpdate.getName()).isEqualTo("New Product");
            assertThat(entityToUpdate.getDescription()).isEqualTo("New Description");
            assertThat(entityToUpdate.getBrand()).isEqualTo("New Brand");
            assertThat(entityToUpdate.getManufacturer()).isEqualTo("New Manufacturer");
            assertThat(entityToUpdate.getWeight()).isEqualTo(new BigDecimal("200"));
            assertThat(entityToUpdate.getDimensions()).isEqualTo("150x75x8mm");
            assertThat(entityToUpdate.getColor()).isEqualTo("Blue");
            assertThat(entityToUpdate.getMaterial()).isEqualTo("Plastic");
            assertThat(entityToUpdate.getIsActive()).isFalse();
            assertThat(entityToUpdate.getMetadata()).isEqualTo("{\"updated\":true}");

            // ID should not be updated
            assertThat(entityToUpdate.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle null entity in update")
        void updateEntityFromDto_NullEntity_ShouldThrowException() {
            ProductDto updateDto = new ProductDto();
            updateDto.setName("Test Product");

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        ProductMapper.updateEntityFromDto(null, updateDto);
                    });
        }

        @Test
        @DisplayName("Should handle null DTO in update")
        void updateEntityFromDto_NullDto_ShouldThrowException() {
            Product entity = new Product();
            entity.setId(1L);
            entity.setName("Original Product");

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        ProductMapper.updateEntityFromDto(entity, null);
                    });
        }

        @Test
        @DisplayName("Should not update ID, timestamps or relationship fields")
        void updateEntityFromDto_ShouldNotUpdateRestrictedFields() {
            LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
            LocalDateTime originalUpdatedAt = LocalDateTime.now().minusHours(1);
            Long originalVersion = 1L;

            Product entityToUpdate = new Product();
            entityToUpdate.setId(1L);
            entityToUpdate.setName("Original Product");
            entityToUpdate.setCreatedAt(originalCreatedAt);
            entityToUpdate.setUpdatedAt(originalUpdatedAt);
            entityToUpdate.setVersion(originalVersion);

            ProductDto updateDto = new ProductDto();
            updateDto.setId(999L); // Different ID
            updateDto.setName("Updated Product");
            updateDto.setCreatedAt(LocalDateTime.now());
            updateDto.setUpdatedAt(LocalDateTime.now());
            updateDto.setVersion(999L);

            ProductMapper.updateEntityFromDto(entityToUpdate, updateDto);

            // These fields should not change
            assertThat(entityToUpdate.getId()).isEqualTo(1L);
            assertThat(entityToUpdate.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(entityToUpdate.getUpdatedAt()).isEqualTo(originalUpdatedAt);
            assertThat(entityToUpdate.getVersion()).isEqualTo(originalVersion);

            // This field should change
            assertThat(entityToUpdate.getName()).isEqualTo("Updated Product");
        }

        @Test
        @DisplayName("Should handle DTO with null values")
        void updateEntityFromDto_DtoWithNullValues_ShouldUpdateWithNulls() {
            Product entityToUpdate = new Product();
            entityToUpdate.setName("Original Product");
            entityToUpdate.setDescription("Original Description");
            entityToUpdate.setBrand("Original Brand");
            entityToUpdate.setIsActive(true);

            ProductDto updateDto = new ProductDto();
            updateDto.setName("Updated Product");
            updateDto.setDescription(null);
            updateDto.setBrand(null);
            updateDto.setIsActive(null);

            ProductMapper.updateEntityFromDto(entityToUpdate, updateDto);

            assertThat(entityToUpdate.getName()).isEqualTo("Updated Product");
            assertThat(entityToUpdate.getDescription()).isNull();
            assertThat(entityToUpdate.getBrand()).isNull();
            assertThat(entityToUpdate.getIsActive()).isNull();
        }
    }

    @Nested
    @DisplayName("Collection Conversion Tests")
    class CollectionConversionTests {

        @Test
        @DisplayName("Should convert list of entities to DTOs")
        void toDtoList_ValidEntities_ShouldReturnDtoList() {
            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Product 2");
            product2.setCategory(testCategory);

            List<Product> products = Arrays.asList(testProduct, product2);

            List<ProductDto> result = ProductMapper.toDtoList(products);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(testProduct.getId());
            assertThat(result.get(1).getId()).isEqualTo(product2.getId());
        }

        @Test
        @DisplayName("Should handle empty list")
        void toDtoList_EmptyList_ShouldReturnEmptyList() {
            List<ProductDto> result = ProductMapper.toDtoList(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null list")
        void toDtoList_NullList_ShouldReturnNull() {
            List<ProductDto> result = ProductMapper.toDtoList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle list with null entities")
        void toDtoList_ListWithNulls_ShouldFilterNulls() {
            List<Product> products = Arrays.asList(testProduct, null, testProduct);

            List<ProductDto> result = ProductMapper.toDtoList(products);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(testProduct.getId());
            assertThat(result.get(1).getId()).isEqualTo(testProduct.getId());
        }

        @Test
        @DisplayName("Should convert list of DTOs to entities")
        void toEntityList_ValidList_ShouldReturnEntityList() {
            ProductDto dto1 = new ProductDto();
            dto1.setId(1L);
            dto1.setName("Product 1");

            ProductDto dto2 = new ProductDto();
            dto2.setId(2L);
            dto2.setName("Product 2");

            List<ProductDto> dtos = Arrays.asList(dto1, dto2);

            List<Product> result = ProductMapper.toEntityList(dtos);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle null list in toEntityList")
        void toEntityList_NullList_ShouldReturnNull() {
            List<Product> result = ProductMapper.toEntityList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty list in toEntityList")
        void toEntityList_EmptyList_ShouldReturnEmptyList() {
            List<Product> result = ProductMapper.toEntityList(Collections.emptyList());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null DTOs in list conversion")
        void toEntityList_WithNullDtos_ShouldFilterNulls() {
            ProductDto dto1 = new ProductDto();
            dto1.setId(1L);
            dto1.setName("Product 1");

            ProductDto dto2 = new ProductDto();
            dto2.setId(2L);
            dto2.setName("Product 2");

            List<ProductDto> dtosWithNull = Arrays.asList(dto1, null, dto2);

            List<Product> result = ProductMapper.toEntityList(dtosWithNull);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // null should be filtered out
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle product with empty SKU collection")
        void toDto_ProductWithEmptySkuCollection_ShouldSetDefaultValues() {
            Product productWithEmptySkus = new Product();
            productWithEmptySkus.setId(1L);
            productWithEmptySkus.setName("Empty SKU Product");
            productWithEmptySkus.setCategory(testCategory);
            // SKUs collection is empty by default

            ProductDto result = ProductMapper.toDto(productWithEmptySkus);

            assertThat(result).isNotNull();
            assertThat(result.getSkuCount()).isEqualTo(0);
            assertThat(result.getActiveSkuCount()).isEqualTo(0);
            assertThat(result.getTotalStockQuantity()).isEqualTo(0);
            assertThat(result.getTotalAvailableQuantity()).isEqualTo(0);
            assertThat(result.getMinPrice()).isNull();
            assertThat(result.getMaxPrice()).isNull();
            assertThat(result.getIsLowOnStock()).isFalse();
        }

        @Test
        @DisplayName("Should handle product with inactive SKUs")
        void toDto_ProductWithInactiveSkus_ShouldCalculateCorrectly() {
            testSku1.setIsActive(false);
            testSku2.setIsActive(false);

            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getSkuCount()).isEqualTo(2);
            assertThat(result.getActiveSkuCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle product with very large numbers")
        void toDto_ProductWithLargeNumbers_ShouldHandleCorrectly() {
            testProduct.setWeight(new BigDecimal("999999999.99"));
            testSku1.setStockQuantity(Integer.MAX_VALUE);
            testSku2.setStockQuantity(Integer.MAX_VALUE - 1000);

            ProductDto result = ProductMapper.toDto(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getWeight()).isEqualTo(new BigDecimal("999999999.99"));
            // Total stock quantity might overflow, but mapper should handle it
            assertThat(result.getTotalStockQuantity()).isNotNull();
        }

        @Test
        @DisplayName("Should handle DTO with minimal fields")
        void toEntity_MinimalDto_ShouldCreateEntity() {
            ProductDto minimalDto = new ProductDto();
            minimalDto.setName("Minimal Product");

            Product result = ProductMapper.toEntity(minimalDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Minimal Product");
            assertThat(result.getId()).isNull();
            assertThat(result.getDescription()).isNull();
            assertThat(result.getBrand()).isNull();
            assertThat(result.getManufacturer()).isNull();
            assertThat(result.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should handle entity with null computed fields gracefully")
        void toDto_EntityWithNullComputedFields_ShouldHandleGracefully() {
            Product productWithNulls = new Product();
            productWithNulls.setId(1L);
            productWithNulls.setName("Product with nulls");
            // All other fields are null

            ProductDto result = ProductMapper.toDto(productWithNulls);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Product with nulls");
            assertThat(result.getCategoryId()).isNull();
            assertThat(result.getCategoryName()).isNull();
            assertThat(result.getCategoryPath()).isNull();
            assertThat(result.getSkuCount()).isEqualTo(0);
            assertThat(result.getActiveSkuCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle update with empty strings")
        void updateEntity_WithEmptyStrings_ShouldSetEmptyStrings() {
            Product entityToUpdate = new Product();
            entityToUpdate.setName("Original Name");
            entityToUpdate.setDescription("Original Description");

            ProductDto updateDto = new ProductDto();
            updateDto.setName("");
            updateDto.setDescription("");
            updateDto.setBrand("");
            updateDto.setManufacturer("");

            ProductMapper.updateEntityFromDto(entityToUpdate, updateDto);

            assertThat(entityToUpdate.getName()).isEmpty();
            assertThat(entityToUpdate.getDescription()).isEmpty();
            assertThat(entityToUpdate.getBrand()).isEmpty();
            assertThat(entityToUpdate.getManufacturer()).isEmpty();
        }

        @Test
        @DisplayName("Should handle update with very long strings")
        void updateEntity_WithVeryLongStrings_ShouldSetLongStrings() {
            String longString = "A".repeat(1000);
            Product entityToUpdate = new Product();
            entityToUpdate.setName("Original Name");

            ProductDto updateDto = new ProductDto();
            updateDto.setName(longString);
            updateDto.setDescription(longString);
            updateDto.setBrand(longString);

            ProductMapper.updateEntityFromDto(entityToUpdate, updateDto);

            assertThat(entityToUpdate.getName()).isEqualTo(longString);
            assertThat(entityToUpdate.getDescription()).isEqualTo(longString);
            assertThat(entityToUpdate.getBrand()).isEqualTo(longString);
        }
    }
}
