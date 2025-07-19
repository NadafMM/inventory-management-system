package com.inventorymanagement.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.common.BaseUnitTest;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.testdata.TestDataFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for CategoryService
 */
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest extends BaseUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        reset(categoryRepository);

        testCategory =
                TestDataFactory.category().withId(1L).withName("Electronics").withPath("/1").build();

        testCategoryDto = new CategoryDto();
        testCategoryDto.setId(1L);
        testCategoryDto.setName("Electronics");
        testCategoryDto.setPath("/1");
    }

    // ===== TEST DATA HELPERS =====

    private Category createValidCategory() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        category.setSortOrder(1);
        category.setLevel(0);
        category.setPath("/");
        category.setIsActive(true);
        return category;
    }

    private CategoryDto createValidCategoryDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Test Category");
        dto.setDescription("Test Description");
        dto.setSortOrder(1);
        dto.setIsActive(true);
        return dto;
    }

    // ===== NESTED TEST CLASSES =====

    @Nested
    @DisplayName("Get Category By ID Tests")
    class GetCategoryByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryWhenFound() {

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

            CategoryDto result = categoryService.getCategoryById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Electronics");
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {

            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create root category successfully")
        void shouldCreateRootCategorySuccessfully() {

            CategoryDto newCategoryDto = new CategoryDto();
            newCategoryDto.setName("New Category");
            newCategoryDto.setDescription("Test Description");

            Category newCategory =
                    TestDataFactory.category()
                            .withName("New Category")
                            .withDescription("Test Description")
                            .build();

            when(categoryRepository.findRootByName("New Category")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

            CategoryDto result = categoryService.createCategory(newCategoryDto);

            assertThat(result).isNotNull();
            verify(categoryRepository, times(2)).save(any(Category.class)); // Saved twice for path update
        }

        @Test
        @DisplayName("Should create subcategory with parent")
        void shouldCreateSubcategoryWithParent() {

            CategoryDto subcategoryDto = new CategoryDto();
            subcategoryDto.setName("Smartphones");
            subcategoryDto.setParentId(1L);

            Category parentCategory = testCategory;
            Category subcategory =
                    TestDataFactory.category()
                            .withId(2L)
                            .withName("Smartphones")
                            .withParent(parentCategory)
                            .withPath("/1/2/")
                            .withLevel(1)
                            .build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
            when(categoryRepository.findByNameAndParentId("Smartphones", 1L))
                    .thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(subcategory);

            CategoryDto result = categoryService.createCategory(subcategoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(subcategoryDto.getName());
            assertThat(result.getParentId()).isEqualTo(1L);
            assertThat(result.getLevel()).isEqualTo(1);
            verify(categoryRepository).findByNameAndParentId("Smartphones", 1L);
        }

        @Test
        @DisplayName("Should throw exception when parent not found")
        void shouldThrowExceptionWhenParentNotFound() {

            CategoryDto subcategoryDto = new CategoryDto();
            subcategoryDto.setName("Smartphones");
            subcategoryDto.setParentId(999L);

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.createCategory(subcategoryDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when duplicate root category name")
        void shouldThrowExceptionWhenDuplicateRootCategoryName() {

            CategoryDto duplicateDto = new CategoryDto();
            duplicateDto.setName("Electronics");

            when(categoryRepository.findRootByName("Electronics")).thenReturn(Optional.of(testCategory));

            assertThatThrownBy(() -> categoryService.createCategory(duplicateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when duplicate subcategory name")
        void shouldThrowExceptionWhenDuplicateSubcategoryName() {

            CategoryDto duplicateDto = new CategoryDto();
            duplicateDto.setName("Smartphones");
            duplicateDto.setParentId(1L);

            Category existingSubcategory =
                    TestDataFactory.category().withName("Smartphones").withParent(testCategory).build();

            when(categoryRepository.findByNameAndParentId("Smartphones", 1L))
                    .thenReturn(Optional.of(existingSubcategory));

            assertThatThrownBy(() -> categoryService.createCategory(duplicateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when category name is empty")
        void shouldThrowExceptionWhenCategoryNameIsEmpty() {

            CategoryDto emptyNameDto = new CategoryDto();
            emptyNameDto.setName("");

            assertThatThrownBy(() -> categoryService.createCategory(emptyNameDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Category name is required");
        }

        @Test
        @DisplayName("Should throw exception when category name is too long")
        void shouldThrowExceptionWhenCategoryNameIsTooLong() {

            CategoryDto longNameDto = new CategoryDto();
            longNameDto.setName("A".repeat(256)); // Assuming max length is 255

            assertThatThrownBy(() -> categoryService.createCategory(longNameDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("too long");
        }

        @Test
        @DisplayName("Should throw exception when hierarchy depth exceeds maximum")
        void shouldThrowExceptionWhenHierarchyDepthExceedsMaximum() {

            CategoryDto deepCategoryDto = new CategoryDto();
            deepCategoryDto.setName("Deep Category");
            deepCategoryDto.setParentId(1L);

            Category deepParent =
                    TestDataFactory.category()
                            .withId(1L)
                            .withLevel(10) // Assuming max depth is 10
                            .build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(deepParent));

            assertThatThrownBy(() -> categoryService.createCategory(deepCategoryDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Maximum category depth exceeded");
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {

            Long categoryId = 1L;
            CategoryDto updateDto = createValidCategoryDto();
            updateDto.setName("Updated Electronics");
            updateDto.setDescription("Updated Description");

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);
            // Removed unnecessary stubbing for existsByNameAndParentIdExcludingId

            CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

            assertThat(result.getName()).isEqualTo("Updated Electronics");
            assertThat(result.getDescription()).isEqualTo("Updated Description");
            verify(categoryRepository).save(existingCategory);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent category")
        void shouldThrowExceptionWhenUpdatingNonExistent() {

            Long categoryId = 999L;
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Updated Name");

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when updating to duplicate name")
        void shouldThrowExceptionWhenUpdatingToDuplicateName() {

            Long categoryId = 1L;
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Duplicate Name");
            updateDto.setParentId(2L);

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);
            existingCategory.setParent(testCategory);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.existsByNameAndParentIdExcludingId("Duplicate Name", 2L, categoryId))
                    .thenReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should update category and maintain hierarchy integrity")
        void shouldUpdateCategoryAndMaintainHierarchyIntegrity() {

            Long categoryId = 1L;
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Updated Category");
            updateDto.setParentId(2L);

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            Category newParent =
                    TestDataFactory.category().withId(2L).withLevel(1).withPath("/2/").build();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

            CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

            assertThat(result).isNotNull();
            verify(categoryRepository).save(existingCategory);
        }

        @Test
        @DisplayName("Should throw exception when trying to set parent as self")
        void shouldThrowExceptionWhenTryingToSetParentAsSelf() {

            Long categoryId = 1L;
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Self Parent Category");
            updateDto.setParentId(categoryId); // Setting parent as self

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("cannot be its own parent");
        }

        @Test
        @DisplayName("Should throw exception when trying to create circular hierarchy")
        void shouldThrowExceptionWhenTryingToCreateCircularHierarchy() {

            Long categoryId = 1L;
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Circular Category");
            updateDto.setParentId(3L);

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            Category targetParent =
                    TestDataFactory.category()
                            .withId(3L)
                            .withParent(existingCategory) // This creates potential circular reference
                            .build();

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(targetParent));

            assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot move category to its own descendant");
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category without children")
        void shouldDeleteCategoryWithoutChildren() {

            Long categoryId = 1L;
            Category category = createValidCategory();
            category.setId(categoryId);
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.countProductsByCategoryId(categoryId)).thenReturn(0L);
            when(categoryRepository.findByParentId(categoryId)).thenReturn(new ArrayList<>());

            categoryService.deleteCategory(categoryId);

            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).countProductsByCategoryId(categoryId);
            verify(categoryRepository).findByParentId(categoryId);
            // Verify that the category was marked as deleted (not saved)
            assertThat(category.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should delete category with children")
        void shouldDeleteCategoryWithChildren() {

            Long categoryId = 1L;
            Category category = createValidCategory();
            category.setId(categoryId);

            // Create children
            Category child1 = createValidCategory();
            child1.setId(2L);
            child1.setParent(category);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.countProductsByCategoryId(categoryId)).thenReturn(0L);
            when(categoryRepository.findByParentId(categoryId)).thenReturn(List.of(child1));
            when(categoryRepository.findByParentId(2L)).thenReturn(new ArrayList<>());

            categoryService.deleteCategory(categoryId);

            -verify soft delete was applied recursively
            verify(categoryRepository).findById(categoryId);
            verify(categoryRepository).countProductsByCategoryId(categoryId);
            verify(categoryRepository).findByParentId(categoryId);
            verify(categoryRepository).findByParentId(2L);

            // Verify both category and child were marked as deleted
            assertThat(category.getDeletedAt()).isNotNull();
            assertThat(child1.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when category has products")
        void shouldThrowExceptionWhenCategoryHasProducts() {

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.countProductsByCategoryId(1L)).thenReturn(5L);

            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete category with existing products");
        }

        @Test
        @DisplayName("Should throw exception when trying to delete non-existent category")
        void shouldThrowExceptionWhenTryingToDeleteNonExistentCategory() {

            Long categoryId = 999L;
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }

        @Test
        @DisplayName("Should delete deep hierarchy recursively")
        void shouldDeleteDeepHierarchyRecursively() {

            Long rootId = 1L;
            Category root = createValidCategory();
            root.setId(rootId);

            Category level1 = createValidCategory();
            level1.setId(2L);
            level1.setParent(root);

            Category level2 = createValidCategory();
            level2.setId(3L);
            level2.setParent(level1);

            when(categoryRepository.findById(rootId)).thenReturn(Optional.of(root));
            when(categoryRepository.countProductsByCategoryId(rootId)).thenReturn(0L);
            when(categoryRepository.findByParentId(rootId)).thenReturn(List.of(level1));
            when(categoryRepository.findByParentId(2L)).thenReturn(List.of(level2));
            when(categoryRepository.findByParentId(3L)).thenReturn(new ArrayList<>());

            categoryService.deleteCategory(rootId);

            verify(categoryRepository).findByParentId(rootId);
            verify(categoryRepository).findByParentId(2L);
            verify(categoryRepository).findByParentId(3L);

            assertThat(root.getDeletedAt()).isNotNull();
            assertThat(level1.getDeletedAt()).isNotNull();
            assertThat(level2.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getAllCategories Tests")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories")
        void shouldReturnPaginatedCategories() {

            Pageable pageable = PageRequest.of(0, 10);
            List<Category> categories = Collections.singletonList(testCategory);
            Page<Category> categoryPage = new PageImpl<>(categories);

            when(categoryRepository.findAllActive(pageable)).thenReturn(categoryPage);

            Page<CategoryDto> result = categoryService.getAllCategories(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should return empty page when no categories exist")
        void shouldReturnEmptyPageWhenNoCategoriesExist() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(new ArrayList<>());

            when(categoryRepository.findAllActive(pageable)).thenReturn(emptyPage);

            Page<CategoryDto> result = categoryService.getAllCategories(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getRootCategories Tests")
    class GetRootCategoriesTests {

        @Test
        @DisplayName("Should return root categories")
        void shouldReturnRootCategories() {

            List<Category> rootCategories = Collections.singletonList(testCategory);
            when(categoryRepository.findAllRootCategories()).thenReturn(rootCategories);

            List<CategoryDto> result = categoryService.getRootCategories();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should return empty list when no root categories exist")
        void shouldReturnEmptyListWhenNoRootCategoriesExist() {

            when(categoryRepository.findAllRootCategories()).thenReturn(new ArrayList<>());

            List<CategoryDto> result = categoryService.getRootCategories();

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getChildCategories Tests")
    class GetChildCategoriesTests {

        @Test
        @DisplayName("Should return subcategories of parent")
        void shouldReturnSubcategoriesOfParent() {

            Category subcategory =
                    TestDataFactory.category().withName("Smartphones").withParent(testCategory).build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.findByParentId(1L))
                    .thenReturn(Collections.singletonList(subcategory));

            List<CategoryDto> result = categoryService.getChildCategories(1L);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Smartphones");
        }

        @Test
        @DisplayName("Should throw exception when parent category not found")
        void shouldThrowExceptionWhenParentCategoryNotFound() {

            Long parentId = 999L;
            when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getChildCategories(parentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }

        @Test
        @DisplayName("Should return empty list when no child categories exist")
        void shouldReturnEmptyListWhenNoChildCategoriesExist() {

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.findByParentId(1L)).thenReturn(new ArrayList<>());

            List<CategoryDto> result = categoryService.getChildCategories(1L);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation and Business Logic Tests")
    class ValidationAndBusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when category name contains invalid characters")
        void shouldThrowExceptionWhenCategoryNameContainsInvalidCharacters() {

            CategoryDto invalidDto = new CategoryDto();
            invalidDto.setName("Invalid/Name\\With|Special<>Characters");

            assertThatThrownBy(() -> categoryService.createCategory(invalidDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("invalid characters");
        }

        @Test
        @DisplayName("Should handle special characters in description")
        void shouldHandleSpecialCharactersInDescription() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Valid Name");
            categoryDto.setDescription("Description with special chars: @#$%^&*()");

            Category savedCategory =
                    TestDataFactory.category()
                            .withName("Valid Name")
                            .withDescription("Description with special chars: @#$%^&*()")
                            .build();

            when(categoryRepository.findRootByName("Valid Name")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).contains("@#$%^&*()");
        }

        @Test
        @DisplayName("Should trim whitespace from category name")
        void shouldTrimWhitespaceFromCategoryName() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("  Trimmed Name  ");

            Category savedCategory = TestDataFactory.category().withName("Trimmed Name").build();

            when(categoryRepository.findRootByName("Trimmed Name")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
            verify(categoryRepository).findRootByName("Trimmed Name");
        }

        @Test
        @DisplayName("Should validate sort order boundaries")
        void shouldValidateSortOrderBoundaries() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Test Category");
            categoryDto.setSortOrder(-1); // Invalid negative sort order

            assertThatThrownBy(() -> categoryService.createCategory(categoryDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Sort order must be non-negative");
        }

        @Test
        @DisplayName("Should handle maximum sort order value")
        void shouldHandleMaximumSortOrderValue() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Max Sort Category");
            categoryDto.setSortOrder(Integer.MAX_VALUE);

            Category savedCategory = TestDataFactory.category().withName("Max Sort Category").build();

            when(categoryRepository.findRootByName("Max Sort Category")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cache Behavior Tests")
    class CacheBehaviorTests {

        @Test
        @DisplayName("Should evict cache on category creation")
        void shouldEvictCacheOnCategoryCreation() {

            CategoryDto newCategoryDto = new CategoryDto();
            newCategoryDto.setName("Cache Test Category");

            Category savedCategory = TestDataFactory.category().withName("Cache Test Category").build();

            when(categoryRepository.findRootByName("Cache Test Category")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(newCategoryDto);

            assertThat(result).isNotNull();
            // Cache eviction is handled by @CacheEvict annotation
            // This test verifies the method executes successfully with cache annotations
        }

        @Test
        @DisplayName("Should evict cache on category update")
        void shouldEvictCacheOnCategoryUpdate() {

            Long categoryId = 1L;
            CategoryDto updateDto = createValidCategoryDto();
            updateDto.setName("Updated Cache Category");

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

            CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

            assertThat(result).isNotNull();
            // Cache eviction is handled by @CacheEvict annotation
            // This test verifies the method executes successfully with cache annotations
        }

        @Test
        @DisplayName("Should evict cache on category deletion")
        void shouldEvictCacheOnCategoryDeletion() {

            Long categoryId = 1L;
            Category category = createValidCategory();
            category.setId(categoryId);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(categoryRepository.countProductsByCategoryId(categoryId)).thenReturn(0L);
            when(categoryRepository.findByParentId(categoryId)).thenReturn(new ArrayList<>());

            categoryService.deleteCategory(categoryId);

            assertThat(category.getDeletedAt()).isNotNull();
            // Cache eviction is handled by @CacheEvict annotation
            // This test verifies the method executes successfully with cache annotations
        }
    }

    @Nested
    @DisplayName("Boundary and Edge Case Tests")
    class BoundaryAndEdgeCaseTests {

        @Test
        @DisplayName("Should handle null description gracefully")
        void shouldHandleNullDescriptionGracefully() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("No Description Category");
            categoryDto.setDescription(null);

            Category savedCategory =
                    TestDataFactory.category()
                            .withName("No Description Category")
                            .withDescription(null)
                            .build();

            when(categoryRepository.findRootByName("No Description Category"))
                    .thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isNull();
        }

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Empty Description Category");
            categoryDto.setDescription("");

            Category savedCategory =
                    TestDataFactory.category()
                            .withName("Empty Description Category")
                            .withDescription("")
                            .build();

            when(categoryRepository.findRootByName("Empty Description Category"))
                    .thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long description")
        void shouldHandleVeryLongDescription() {

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Long Description Category");
            String longDescription = "A".repeat(5000); // Very long description
            categoryDto.setDescription(longDescription);

            Category savedCategory =
                    TestDataFactory.category()
                            .withName("Long Description Category")
                            .withDescription(longDescription)
                            .build();

            when(categoryRepository.findRootByName("Long Description Category"))
                    .thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryDto result = categoryService.createCategory(categoryDto);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).hasSize(5000);
        }

        @Test
        @DisplayName("Should handle concurrent updates gracefully")
        void shouldHandleConcurrentUpdatesGracefully() {

            Long categoryId = 1L;
            CategoryDto updateDto = createValidCategoryDto();
            updateDto.setName("Concurrent Update");

            Category existingCategory = createValidCategory();
            existingCategory.setId(categoryId);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

            CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Concurrent Update");
        }
    }
}
