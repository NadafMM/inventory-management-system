package com.inventorymanagement.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.category.service.CategoryService;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.GlobalExceptionHandler;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.model.PagedResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprehensive functional tests for CategoryController to achieve 90% code coverage
 */
@WebMvcTest(CategoryController.class)
@ContextConfiguration(classes = {CategoryControllerTest.TestConfig.class, CategoryController.class})
@ActiveProfiles("test")
@DisplayName("CategoryController Comprehensive Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CategoryService categoryService;
    private CategoryDto testCategory;
    private CategoryDto childCategory;
    private CategoryDto invalidCategory;
    private PagedResponse<CategoryDto> pagedResponse;

    /**
     * Helper method to convert object to JSON string
     */
    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    void setUp() {
        testCategory = new CategoryDto();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices and accessories");
        testCategory.setLevel(0);
        testCategory.setPath("/1/");
        testCategory.setIsActive(true);

        childCategory = new CategoryDto();
        childCategory.setId(2L);
        childCategory.setName("Laptops");
        childCategory.setParentId(1L);
        childCategory.setLevel(1);
        childCategory.setPath("/1/2/");
        childCategory.setIsActive(true);

        invalidCategory = new CategoryDto();
        // Missing required fields for testing validation
    }

    @SpringBootApplication
    @Import(GlobalExceptionHandler.class)
    static class TestConfig {
        // Minimal configuration for WebMvcTest
    }

    @Nested
    @DisplayName("Category Creation")
    class CategoryCreationTests {

        @Test
        @DisplayName("Should create category successfully")
        void createCategory_Success() throws Exception {
            when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(testCategory);

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Electronics"));

            verify(categoryService, times(1)).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle validation errors")
        void createCategory_ValidationError() throws Exception {
            Spring validation fails on request body, service is never called
                    mockMvc
          .perform(
                    post("/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(invalidCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"));

            // Verify that service was NOT called due to validation failure
            verifyNoInteractions(categoryService);
        }

        @Test
        @DisplayName("Should handle duplicate category name")
        void createCategory_DuplicateName() throws Exception {
            when(categoryService.createCategory(any(CategoryDto.class)))
                    .thenThrow(new ValidationException("Category with name 'Electronics' already exists"));

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(categoryService).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle malformed JSON")
        void createCategory_MalformedJson() throws Exception {
            String malformedJson = "{ \"name\": \"Test\", \"invalid\": }";

            mockMvc
                    .perform(
                            post("/v1/categories").contentType(MediaType.APPLICATION_JSON).content(malformedJson))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle empty request body")
        void createCategory_EmptyBody() throws Exception {
            mockMvc
                    .perform(post("/v1/categories").contentType(MediaType.APPLICATION_JSON).content(""))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle missing content type")
        void createCategory_MissingContentType() throws Exception {
            mockMvc
                    .perform(post("/v1/categories").content(toJson(testCategory)))
                    .andExpect(status().isUnsupportedMediaType());

            verify(categoryService, never()).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle business logic error")
        void createCategory_BusinessError() throws Exception {
            when(categoryService.createCategory(any(CategoryDto.class)))
                    .thenThrow(new BusinessException("Maximum hierarchy depth exceeded"));

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value("Maximum hierarchy depth exceeded"));

            verify(categoryService).createCategory(any(CategoryDto.class));
        }
    }

    @Nested
    @DisplayName("Category Retrieval")
    class CategoryRetrievalTests {

        @Test
        @DisplayName("Should get category by ID successfully")
        void getCategory_Found() throws Exception {
            when(categoryService.getCategoryById(1L)).thenReturn(testCategory);

            mockMvc
                    .perform(get("/v1/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Electronics"));

            verify(categoryService).getCategoryById(1L);
        }

        @Test
        @DisplayName("Should handle category not found")
        void getCategory_NotFound() throws Exception {
            when(categoryService.getCategoryById(999L))
                    .thenThrow(new EntityNotFoundException("Category", 999L));

            mockMvc
                    .perform(get("/v1/categories/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService).getCategoryById(999L);
        }

        @Test
        @DisplayName("Should handle invalid ID format")
        void getCategory_InvalidId() throws Exception {
            mockMvc.perform(get("/v1/categories/invalid")).andExpect(status().isBadRequest());

            verify(categoryService, never()).getCategoryById(any());
        }

        @Test
        @DisplayName("Should handle negative ID")
        void getCategory_NegativeId() throws Exception {
            mockMvc.perform(get("/v1/categories/-1")).andExpect(status().isBadRequest());

            verify(categoryService, never()).getCategoryById(any());
        }

        @Test
        @DisplayName("Should get all categories with pagination")
        void getAllCategories_Success() throws Exception {
            List<CategoryDto> categories = Arrays.asList(testCategory, childCategory);
            Page<CategoryDto> page = new PageImpl<>(categories);
            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(page);

            mockMvc
                    .perform(get("/v1/categories").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value("Electronics"))
                    .andExpect(jsonPath("$.data.content[1].name").value("Laptops"));

            verify(categoryService).getAllCategories(any(Pageable.class));
        }

        @Test
        @DisplayName("Should get categories with default pagination")
        void getAllCategories_DefaultPagination() throws Exception {
            List<CategoryDto> categories = Collections.singletonList(testCategory);
            Page<CategoryDto> page = new PageImpl<>(categories);
            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(page);

            mockMvc
                    .perform(get("/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(categoryService).getAllCategories(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle large page numbers")
        void getAllCategories_LargePageNumber() throws Exception {
            Page<CategoryDto> emptyPage = new PageImpl<>(Collections.emptyList());
            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc
                    .perform(get("/v1/categories").param("page", "1000").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty());

            verify(categoryService).getAllCategories(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle invalid pagination parameters")
        void getAllCategories_InvalidPagination() throws Exception {
            mockMvc
                    .perform(get("/v1/categories").param("page", "invalid").param("size", "negative"))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).getAllCategories(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Category Updates")
    class CategoryUpdateTests {

        @Test
        @DisplayName("Should update category successfully")
        void updateCategory_Success() throws Exception {
            CategoryDto updateDto = new CategoryDto();
            updateDto.setName("Updated Electronics");
            updateDto.setDescription("Updated description");

            CategoryDto updatedCategory = new CategoryDto();
            updatedCategory.setId(1L);
            updatedCategory.setName("Updated Electronics");
            updatedCategory.setDescription("Updated description");

            when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                    .thenReturn(updatedCategory);

            mockMvc
                    .perform(
                            put("/v1/categories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Updated Electronics"));

            verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle update of non-existent category")
        void updateCategory_NotFound() throws Exception {
            when(categoryService.updateCategory(eq(999L), any(CategoryDto.class)))
                    .thenThrow(new EntityNotFoundException("Category", 999L));

            mockMvc
                    .perform(
                            put("/v1/categories/999")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService).updateCategory(eq(999L), any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle validation error during update")
        void updateCategory_ValidationError() throws Exception {
            when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                    .thenThrow(new ValidationException("Category name cannot be empty"));

            mockMvc
                    .perform(
                            put("/v1/categories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(invalidCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle business logic error during update")
        void updateCategory_BusinessError() throws Exception {
            when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                    .thenThrow(new BusinessException("Cannot update category with existing products"));

            mockMvc
                    .perform(
                            put("/v1/categories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value("Cannot update category with existing products"));

            verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle partial update")
        void updateCategory_PartialUpdate() throws Exception {
            CategoryDto partialUpdate = new CategoryDto();
            partialUpdate.setDescription("Only updating description");

            CategoryDto updatedCategory = new CategoryDto();
            updatedCategory.setId(1L);
            updatedCategory.setName("Electronics"); // Original name preserved
            updatedCategory.setDescription("Only updating description");

            when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                    .thenReturn(updatedCategory);

            mockMvc
                    .perform(
                            put("/v1/categories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(partialUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.description").value("Only updating description"));

            verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
        }
    }

    @Nested
    @DisplayName("Category Deletion")
    class CategoryDeletionTests {

        @Test
        @DisplayName("Should delete category successfully")
        void deleteCategory_Success() throws Exception {
            doNothing().when(categoryService).deleteCategory(1L);

            mockMvc
                    .perform(delete("/v1/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category deleted successfully"));

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("Should handle deletion of non-existent category")
        void deleteCategory_NotFound() throws Exception {
            doThrow(new EntityNotFoundException("Category", 999L))
                    .when(categoryService)
                    .deleteCategory(999L);

            mockMvc
                    .perform(delete("/v1/categories/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService).deleteCategory(999L);
        }

        @Test
        @DisplayName("Should handle business logic error during deletion")
        void deleteCategory_BusinessError() throws Exception {
            doThrow(new BusinessException("Cannot delete category with existing products"))
                    .when(categoryService)
                    .deleteCategory(1L);

            mockMvc
                    .perform(delete("/v1/categories/1"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value("Cannot delete category with existing products"));

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("Should handle bulk deletion successfully")
        void bulkDeleteCategories_Success() throws Exception {
            List<Long> ids = Arrays.asList(1L, 2L, 3L);

            doNothing().when(categoryService).deleteCategory(anyLong());

            mockMvc
                    .perform(
                            delete("/v1/categories/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(ids)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(categoryService, times(3)).deleteCategory(anyLong());
        }

        @Test
        @DisplayName("Should handle bulk deletion with some failures")
        void bulkDeleteCategories_PartialFailure() throws Exception {
            List<Long> ids = Arrays.asList(1L, 999L, 3L);

            doNothing().when(categoryService).deleteCategory(1L);
            doThrow(new EntityNotFoundException("Category", 999L))
                    .when(categoryService)
                    .deleteCategory(999L);
            doNothing().when(categoryService).deleteCategory(3L);

            mockMvc
                    .perform(
                            delete("/v1/categories/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(ids)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(categoryService).deleteCategory(1L);
            verify(categoryService).deleteCategory(999L);
            verify(categoryService).deleteCategory(3L);
        }
    }

    @Nested
    @DisplayName("Category Hierarchy")
    class CategoryHierarchyTests {

        @Test
        @DisplayName("Should get full category hierarchy")
        void getCategoryHierarchy_Success() throws Exception {
            List<CategoryDto> hierarchy = Collections.singletonList(testCategory);
            when(categoryService.getCategoryHierarchy(null)).thenReturn(hierarchy);

            mockMvc
                    .perform(get("/v1/categories/hierarchy"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("Electronics"));

            verify(categoryService).getCategoryHierarchy(null);
        }

        @Test
        @DisplayName("Should get hierarchy starting from specific root")
        void getCategoryHierarchy_WithRootId() throws Exception {
            List<CategoryDto> children = Collections.singletonList(childCategory);
            when(categoryService.getCategoryHierarchy(1L)).thenReturn(children);

            mockMvc
                    .perform(get("/v1/categories/hierarchy").param("rootId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("Laptops"));

            verify(categoryService).getCategoryHierarchy(1L);
        }

        @Test
        @DisplayName("Should handle empty hierarchy")
        void getCategoryHierarchy_Empty() throws Exception {
            when(categoryService.getCategoryHierarchy(null)).thenReturn(Collections.emptyList());

            mockMvc
                    .perform(get("/v1/categories/hierarchy"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());

            verify(categoryService).getCategoryHierarchy(null);
        }

        @Test
        @DisplayName("Should handle non-existent root ID")
        void getCategoryHierarchy_InvalidRootId() throws Exception {
            when(categoryService.getCategoryHierarchy(999L))
                    .thenThrow(new EntityNotFoundException("Category", 999L));

            mockMvc
                    .perform(get("/v1/categories/hierarchy").param("rootId", "999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            verify(categoryService).getCategoryHierarchy(999L);
        }

        @Test
        @DisplayName("Should get child categories")
        void getChildCategories_Success() throws Exception {
            List<CategoryDto> children = Collections.singletonList(childCategory);
            when(categoryService.getChildCategories(1L)).thenReturn(children);

            mockMvc
                    .perform(get("/v1/categories/1/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("Laptops"));

            verify(categoryService).getChildCategories(1L);
        }

        @Test
        @DisplayName("Should handle category with no children")
        void getChildCategories_NoChildren() throws Exception {
            when(categoryService.getChildCategories(2L)).thenReturn(Collections.emptyList());

            mockMvc
                    .perform(get("/v1/categories/2/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());

            verify(categoryService).getChildCategories(2L);
        }
    }

    @Nested
    @DisplayName("Category Search")
    class CategorySearchTests {

        @Test
        @DisplayName("Should search categories by name")
        void searchCategories_Success() throws Exception {
            List<CategoryDto> searchResults = Collections.singletonList(testCategory);
            Page<CategoryDto> page = new PageImpl<>(searchResults);

            when(categoryService.searchCategoriesByName(eq("Electronics"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc
                    .perform(get("/v1/categories/search").param("query", "Electronics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].name").value("Electronics"));

            verify(categoryService).searchCategoriesByName(eq("Electronics"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle empty search results")
        void searchCategories_NoResults() throws Exception {
            Page<CategoryDto> emptyPage = new PageImpl<>(Collections.emptyList());

            when(categoryService.searchCategoriesByName(eq("NonExistent"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc
                    .perform(get("/v1/categories/search").param("query", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty());

            verify(categoryService).searchCategoriesByName(eq("NonExistent"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle missing search query")
        void searchCategories_MissingQuery() throws Exception {
            mockMvc.perform(get("/v1/categories/search")).andExpect(status().isBadRequest());

            verify(categoryService, never()).searchCategoriesByName(any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle empty search query")
        void searchCategories_EmptyQuery() throws Exception {
            mockMvc
                    .perform(get("/v1/categories/search").param("query", ""))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).searchCategoriesByName(any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle special characters in search")
        void searchCategories_SpecialCharacters() throws Exception {
            List<CategoryDto> searchResults = Collections.singletonList(testCategory);
            Page<CategoryDto> page = new PageImpl<>(searchResults);

            when(categoryService.searchCategoriesByName(eq("Test & Category"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc
                    .perform(get("/v1/categories/search").param("query", "Test & Category"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(categoryService).searchCategoriesByName(eq("Test & Category"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle case insensitive search")
        void searchCategories_CaseInsensitive() throws Exception {
            List<CategoryDto> searchResults = Collections.singletonList(testCategory);
            Page<CategoryDto> page = new PageImpl<>(searchResults);

            when(categoryService.searchCategoriesByName(eq("electronics"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc
                    .perform(get("/v1/categories/search").param("query", "electronics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(categoryService).searchCategoriesByName(eq("electronics"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long category names")
        void createCategory_VeryLongName() throws Exception {
            CategoryDto longNameCategory = new CategoryDto();
            longNameCategory.setName("A".repeat(1000)); // Very long name

            when(categoryService.createCategory(any(CategoryDto.class)))
                    .thenThrow(new ValidationException("Category name too long"));

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(longNameCategory)))
                    .andExpect(status().isBadRequest());

            verify(categoryService).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle Unicode characters in category names")
        void createCategory_UnicodeCharacters() throws Exception {
            CategoryDto unicodeCategory = new CategoryDto();
            unicodeCategory.setName("电子产品 🇨🇳");
            unicodeCategory.setDescription("测试描述");

            when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(unicodeCategory);

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(unicodeCategory)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            verify(categoryService).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void createCategory_NullValues() throws Exception {
            CategoryDto nullCategory = new CategoryDto();
            nullCategory.setName(null);
            nullCategory.setDescription(null);

            Spring validation fails due to null name, service is never called
                    mockMvc
          .perform(
                    post("/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(nullCategory)))
                    .andExpect(status().isBadRequest());

            // Verify that service was NOT called due to validation failure
            verifyNoInteractions(categoryService);
        }

        @Test
        @DisplayName("Should handle extremely large IDs")
        void getCategory_LargeId() throws Exception {
            Long largeId = Long.MAX_VALUE;
            when(categoryService.getCategoryById(largeId))
                    .thenThrow(new EntityNotFoundException("Category", largeId));

            mockMvc.perform(get("/v1/categories/" + largeId)).andExpect(status().isNotFound());

            verify(categoryService).getCategoryById(largeId);
        }

        @Test
        @DisplayName("Should handle concurrent modification")
        void updateCategory_ConcurrentModification() throws Exception {
            when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                    .thenThrow(new BusinessException("Category was modified by another user"));

            mockMvc
                    .perform(
                            put("/v1/categories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isUnprocessableEntity());

            verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle network timeouts gracefully")
        void createCategory_NetworkTimeout() throws Exception {
            when(categoryService.createCategory(any(CategoryDto.class)))
                    .thenThrow(new RuntimeException("Database connection timeout"));

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isInternalServerError());

            verify(categoryService).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle unsupported HTTP methods")
        void unsupportedHttpMethod() throws Exception {
            mockMvc.perform(patch("/v1/categories/1")).andExpect(status().isMethodNotAllowed());

            verify(categoryService, never()).updateCategory(any(), any());
        }

        @Test
        @DisplayName("Should handle invalid JSON content type")
        void createCategory_InvalidContentType() throws Exception {
            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .content(toJson(testCategory)))
                    .andExpect(status().isUnsupportedMediaType());

            verify(categoryService, never()).createCategory(any(CategoryDto.class));
        }

        @Test
        @DisplayName("Should handle very large request bodies")
        void createCategory_LargeRequestBody() throws Exception {
            CategoryDto largeCategory = new CategoryDto();
            largeCategory.setName("Electronics");
            largeCategory.setDescription("X".repeat(10000)); // Very large description

            when(categoryService.createCategory(any(CategoryDto.class)))
                    .thenThrow(new ValidationException("Description too long"));

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(largeCategory)))
                    .andExpect(status().isBadRequest());

            verify(categoryService).createCategory(any(CategoryDto.class));
        }
    }
}
