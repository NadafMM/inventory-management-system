package com.inventorymanagement.category.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventorymanagement.product.model.Product;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CategoryMapper Unit Tests")
class CategoryMapperTest {

    private Category testCategory;
    private CategoryDto testCategoryDto;
    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Setup parent category
        parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");
        parentCategory.setDescription("Electronic devices");
        parentCategory.setPath("/1/");
        parentCategory.setLevel(0);
        parentCategory.setSortOrder(1);
        parentCategory.setIsActive(true);
        parentCategory.setMetadata("{\"color\":\"blue\"}");
        parentCategory.setCreatedAt(now.minusDays(1));
        parentCategory.setUpdatedAt(now.minusHours(1));
        parentCategory.setVersion(1L);

        // Setup child category
        childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setName("Smartphones");
        childCategory.setDescription("Mobile phones");
        childCategory.setPath("/1/2/");
        childCategory.setParent(parentCategory);
        childCategory.setLevel(1);
        childCategory.setSortOrder(2);
        childCategory.setIsActive(true);
        childCategory.setCreatedAt(now);
        childCategory.setUpdatedAt(now);
        childCategory.setVersion(1L);

        // Add child to parent
        parentCategory.getChildren().add(childCategory);

        // Add products to category
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone");
        product1.setCategory(childCategory);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung Galaxy");
        product2.setCategory(childCategory);

        childCategory.getProducts().addAll(Arrays.asList(product1, product2));

        testCategory = childCategory;

        // Setup DTO
        testCategoryDto = new CategoryDto();
        testCategoryDto.setId(3L);
        testCategoryDto.setName("Tablets");
        testCategoryDto.setDescription("Tablet computers");
        testCategoryDto.setParentId(1L);
        testCategoryDto.setLevel(1);
        testCategoryDto.setSortOrder(3);
        testCategoryDto.setIsActive(false);
        testCategoryDto.setMetadata("{\"color\":\"red\"}");
        testCategoryDto.setCreatedAt(now);
        testCategoryDto.setUpdatedAt(now);
        testCategoryDto.setDeletedAt(now.minusHours(1));
        testCategoryDto.setVersion(2L);
    }

    @Nested
    @DisplayName("Entity to DTO Conversion Tests")
    class EntityToDtoTests {

        @Test
        @DisplayName("Should convert category entity to DTO successfully")
        void toDtoValidCategoryShouldReturnDto() {
            CategoryDto result = CategoryMapper.toDto(testCategory);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testCategory.getId());
            assertThat(result.getName()).isEqualTo(testCategory.getName());
            assertThat(result.getDescription()).isEqualTo(testCategory.getDescription());
            assertThat(result.getPath()).isEqualTo(testCategory.getPath());
            assertThat(result.getLevel()).isEqualTo(testCategory.getLevel());
            assertThat(result.getSortOrder()).isEqualTo(testCategory.getSortOrder());
            assertThat(result.getIsActive()).isEqualTo(testCategory.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testCategory.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testCategory.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testCategory.getUpdatedAt());
            assertThat(result.getVersion()).isEqualTo(testCategory.getVersion());

            // Check parent information
            assertThat(result.getParentId()).isEqualTo(testCategory.getParent().getId());
            assertThat(result.getParentName()).isEqualTo(testCategory.getParent().getName());

            // Check computed fields
            assertThat(result.getChildrenCount()).isEqualTo(testCategory.getChildren().size());
            assertThat(result.getProductsCount()).isEqualTo(testCategory.getProducts().size());
            assertThat(result.getIsRoot()).isEqualTo(testCategory.isRoot());
            assertThat(result.getIsLeaf()).isEqualTo(testCategory.isLeaf());
        }

        @Test
        @DisplayName("Should handle null category entity")
        void toDtoNullCategoryShouldReturnNull() {
            CategoryDto result = CategoryMapper.toDto(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle category without parent")
        void toDtoCategoryWithoutParentShouldSetParentFieldsNull() {
            Category rootCategory = new Category();
            rootCategory.setId(1L);
            rootCategory.setName("Root Category");
            rootCategory.setDescription("Root description");
            rootCategory.setPath("/1/");
            rootCategory.setLevel(0);
            rootCategory.setIsActive(true);
            // No parent set

            CategoryDto result = CategoryMapper.toDto(rootCategory);

            assertThat(result).isNotNull();
            assertThat(result.getParentId()).isNull();
            assertThat(result.getParentName()).isNull();
            assertThat(result.getIsRoot()).isTrue();
        }

        @Test
        @DisplayName("Should handle category with empty children and products")
        void toDtoCategoryWithEmptyCollectionsShouldSetZeroCounts() {
            Category emptyCategory = new Category();
            emptyCategory.setId(1L);
            emptyCategory.setName("Empty Category");
            emptyCategory.setIsActive(true);
            // Children and products collections are empty by default

            CategoryDto result = CategoryMapper.toDto(emptyCategory);

            assertThat(result).isNotNull();
            assertThat(result.getChildrenCount()).isEqualTo(0);
            assertThat(result.getProductsCount()).isEqualTo(0);
            assertThat(result.getIsLeaf()).isTrue();
        }
    }

    @Nested
    @DisplayName("DTO to Entity Conversion Tests")
    class DtoToEntityTests {

        @Test
        @DisplayName("Should convert DTO to category entity successfully")
        void toEntityValidDtoShouldReturnEntity() {
            Category result = CategoryMapper.toEntity(testCategoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testCategoryDto.getId());
            assertThat(result.getName()).isEqualTo(testCategoryDto.getName());
            assertThat(result.getDescription()).isEqualTo(testCategoryDto.getDescription());
            assertThat(result.getLevel()).isEqualTo(testCategoryDto.getLevel());
            assertThat(result.getSortOrder()).isEqualTo(testCategoryDto.getSortOrder());
            assertThat(result.getIsActive()).isEqualTo(testCategoryDto.getIsActive());
            assertThat(result.getMetadata()).isEqualTo(testCategoryDto.getMetadata());
            assertThat(result.getCreatedAt()).isEqualTo(testCategoryDto.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(testCategoryDto.getUpdatedAt());
            assertThat(result.getDeletedAt()).isEqualTo(testCategoryDto.getDeletedAt());
            assertThat(result.getVersion()).isEqualTo(testCategoryDto.getVersion());
        }

        @Test
        @DisplayName("Should handle null DTO")
        void toEntityNullDtoShouldReturnNull() {
            Category result = CategoryMapper.toEntity(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle DTO with null fields")
        void toEntityDtoWithNullFieldsShouldHandleGracefully() {
            CategoryDto dtoWithNulls = new CategoryDto();
            dtoWithNulls.setId(1L);
            dtoWithNulls.setName("Test Category");
            // All other fields are null

            Category result = CategoryMapper.toEntity(dtoWithNulls);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Category");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getLevel()).isNull();
        }
    }

    @Nested
    @DisplayName("Update Entity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity from DTO successfully")
        void updateEntityValidDtoShouldUpdateFields() {
            Category existingCategory = new Category();
            existingCategory.setId(1L);
            existingCategory.setName("Original Name");
            existingCategory.setDescription("Original Description");
            existingCategory.setIsActive(true);

            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Updated Name");
            updateDto.setDescription("Updated Description");
            updateDto.setIsActive(false);
            updateDto.setSortOrder(5);

            CategoryMapper.updateEntityFromDto(existingCategory, updateDto);

            assertThat(existingCategory.getId()).isEqualTo(1L); // ID should not change
            assertThat(existingCategory.getName()).isEqualTo("Updated Name");
            assertThat(existingCategory.getDescription()).isEqualTo("Updated Description");
            assertThat(existingCategory.getIsActive()).isFalse();
            assertThat(existingCategory.getSortOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle null entity in update")
        void updateEntityNullEntityShouldThrowException() {
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Test");

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        CategoryMapper.updateEntityFromDto(null, updateDto);
                    });
        }

        @Test
        @DisplayName("Should handle null DTO in update")
        void updateEntityNullDtoShouldThrowException() {
            Category existingCategory = new Category();
            existingCategory.setId(1L);

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        CategoryMapper.updateEntityFromDto(existingCategory, null);
                    });
        }

        @Test
        @DisplayName("Should not update ID, timestamps or relationship fields")
        void updateEntityShouldNotUpdateRestrictedFields() {
            LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
            LocalDateTime originalUpdatedAt = LocalDateTime.now().minusHours(1);
            Long originalVersion = 1L;

            Category existingCategory = new Category();
            existingCategory.setId(1L);
            existingCategory.setName("Original Name");
            existingCategory.setCreatedAt(originalCreatedAt);
            existingCategory.setUpdatedAt(originalUpdatedAt);
            existingCategory.setVersion(originalVersion);

            CategoryDto updateDto = new CategoryDto();
            updateDto.setId(999L); // Different ID
            updateDto.setName("Updated Name");
            updateDto.setCreatedAt(LocalDateTime.now());
            updateDto.setUpdatedAt(LocalDateTime.now());
            updateDto.setVersion(999L);

            CategoryMapper.updateEntityFromDto(existingCategory, updateDto);

            // These fields should not change
            assertThat(existingCategory.getId()).isEqualTo(1L);
            assertThat(existingCategory.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(existingCategory.getUpdatedAt()).isEqualTo(originalUpdatedAt);
            assertThat(existingCategory.getVersion()).isEqualTo(originalVersion);

            // This field should change
            assertThat(existingCategory.getName()).isEqualTo("Updated Name");
        }
    }

    @Nested
    @DisplayName("List Conversion Tests")
    class ListConversionTests {

        @Test
        @DisplayName("Should convert list of entities to DTOs")
        void toDtoListValidListShouldReturnDtoList() {
            List<Category> categories = Arrays.asList(parentCategory, testCategory);

            List<CategoryDto> result = CategoryMapper.toDtoList(categories);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(parentCategory.getId());
            assertThat(result.get(1).getId()).isEqualTo(testCategory.getId());
        }

        @Test
        @DisplayName("Should handle null list in toDtoList")
        void toDtoListNullListShouldReturnNull() {
            List<CategoryDto> result = CategoryMapper.toDtoList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty list in toDtoList")
        void toDtoListEmptyListShouldReturnEmptyList() {
            List<CategoryDto> result = CategoryMapper.toDtoList(Collections.emptyList());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null entities in list conversion")
        void toDtoListWithNullEntitiesShouldFilterNulls() {
            List<Category> categoriesWithNull = Arrays.asList(parentCategory, null, testCategory);

            List<CategoryDto> result = CategoryMapper.toDtoList(categoriesWithNull);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // null should be filtered out
            assertThat(result.get(0).getId()).isEqualTo(parentCategory.getId());
            assertThat(result.get(1).getId()).isEqualTo(testCategory.getId());
        }

        @Test
        @DisplayName("Should convert list of DTOs to entities")
        void toEntityListValidListShouldReturnEntityList() {
            CategoryDto dto1 = new CategoryDto();
            dto1.setId(1L);
            dto1.setName("Category 1");

            CategoryDto dto2 = new CategoryDto();
            dto2.setId(2L);
            dto2.setName("Category 2");

            List<CategoryDto> dtos = Arrays.asList(dto1, dto2);

            List<Category> result = CategoryMapper.toEntityList(dtos);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle null list in toEntityList")
        void toEntityListNullListShouldReturnNull() {
            List<Category> result = CategoryMapper.toEntityList(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty list in toEntityList")
        void toEntityListEmptyListShouldReturnEmptyList() {
            List<Category> result = CategoryMapper.toEntityList(Collections.emptyList());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }
}
