package com.inventorymanagement.category.controller;

import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.category.service.CategoryService;
import com.inventorymanagement.common.model.ApiResponse;
import com.inventorymanagement.common.model.BulkOperationRequest;
import com.inventorymanagement.common.model.BulkOperationResponse;
import com.inventorymanagement.common.model.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Category management operations. Provides comprehensive CRUD operations, search, filtering, and bulk operations.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Category Management", description = "Operations for managing product categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired private CategoryService categoryService;

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new category.
     *
     * @param categoryDto the category data
     * @param request     HTTP request for path information
     * @return the created category
     */
    @PostMapping
    @Operation(summary = "Create Category", description = "Create a new product category")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Category created successfully",
                            content =
                            @Content(
                                    schema =
                                    @Schema(
                                            implementation =
                                                    com.inventorymanagement.common.model.ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Category already exists")
            })
    public ResponseEntity<com.inventorymanagement.common.model.ApiResponse<CategoryDto>>
    createCategory(
            @Validated(CategoryDto.CreateGroup.class) @RequestBody CategoryDto categoryDto,
            HttpServletRequest request) {

        logger.info("Creating new category: {}", categoryDto.getName());

        CategoryDto createdCategory = categoryService.createCategory(categoryDto);

        com.inventorymanagement.common.model.ApiResponse<CategoryDto> response =
                com.inventorymanagement.common.model.ApiResponse.success(
                        "Category created successfully", createdCategory, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get category by ID.
     *
     * @param id      the category ID
     * @param request HTTP request for path information
     * @return the category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Category", description = "Retrieve a category by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Category retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Category not found")
            })
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @PathVariable @NotNull @Min(1) Long id, HttpServletRequest request) {

        logger.debug("Retrieving category with ID: {}", id);

        CategoryDto category = categoryService.getCategoryById(id);

        ApiResponse<CategoryDto> response =
                ApiResponse.success("Category retrieved successfully", category, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing category.
     *
     * @param id          the category ID
     * @param categoryDto the updated category data
     * @param request     HTTP request for path information
     * @return the updated category
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Category", description = "Update an existing category")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Category updated successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Category not found")
            })
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable @NotNull Long id,
            @Validated(CategoryDto.UpdateGroup.class) @RequestBody CategoryDto categoryDto,
            HttpServletRequest request) {

        logger.info("Updating category with ID: {}", id);

        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);

        ApiResponse<CategoryDto> response =
                ApiResponse.success(
                        "Category updated successfully", updatedCategory, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category.
     *
     * @param id      the category ID
     * @param request HTTP request for path information
     * @return confirmation message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category", description = "Delete a category by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Category deleted successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Category not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Category has dependencies")
            })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.info("Deleting category with ID: {}", id);

        categoryService.deleteCategory(id);

        ApiResponse<Void> response =
                ApiResponse.success("Category deleted successfully", null, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== SEARCH AND FILTERING =====

    /**
     * Get all categories with pagination and filtering.
     *
     * @param page     page number (0-based)
     * @param size     page size
     * @param sortBy   sort field
     * @param sortDir  sort direction
     * @param name     filter by name
     * @param parentId filter by parent ID
     * @param level    filter by level
     * @param isActive filter by active status
     * @param request  HTTP request for path information
     * @return paginated list of categories
     */
    @GetMapping
    @Operation(
            summary = "Get All Categories",
            description = "Retrieve all categories with pagination and filtering")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Categories retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters")
            })
    public ResponseEntity<ApiResponse<PagedResponse<CategoryDto>>> getAllCategories(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Boolean isActive,
            HttpServletRequest request) {

        logger.debug(
                "Retrieving categories with filters - page: {}, size: {}, name: {}, parentId: {}, level: {}, isActive: {}",
                page,
                size,
                name,
                parentId,
                level,
                isActive);

        // Build sort
        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply filters and get results
        Page<CategoryDto> categoriesPage;
        if (hasFilters(name, parentId, level, isActive)) {
            categoriesPage = categoryService.searchCategories(name, parentId, level, isActive, pageable);
        } else {
            categoriesPage = categoryService.getAllCategories(pageable);
        }

        // Build response with pagination links
        String baseUrl = request.getRequestURL().toString();
        PagedResponse<CategoryDto> pagedResponse = PagedResponse.of(categoriesPage, baseUrl);

        ApiResponse<PagedResponse<CategoryDto>> response =
                ApiResponse.success(
                        "Categories retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Search categories by name.
     *
     * @param query   search query
     * @param page    page number
     * @param size    page size
     * @param request HTTP request for path information
     * @return search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search Categories", description = "Search categories by name")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Search completed successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid search parameters")
            })
    public ResponseEntity<ApiResponse<PagedResponse<CategoryDto>>> searchCategories(
            @RequestParam @Parameter(description = "Search query") @NotBlank String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Searching categories with query: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDto> categoriesPage = categoryService.searchCategoriesByName(query, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<CategoryDto> pagedResponse = PagedResponse.of(categoriesPage, baseUrl);

        ApiResponse<PagedResponse<CategoryDto>> response =
                ApiResponse.success(
                        "Search completed successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get category hierarchy (tree structure).
     *
     * @param rootId  root category ID (optional)
     * @param request HTTP request for path information
     * @return category hierarchy
     */
    @GetMapping("/hierarchy")
    @Operation(
            summary = "Get Category Hierarchy",
            description = "Retrieve category hierarchy as a tree structure")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Hierarchy retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategoryHierarchy(
            @RequestParam(required = false) Long rootId, HttpServletRequest request) {

        logger.debug("Retrieving category hierarchy with root ID: {}", rootId);

        List<CategoryDto> hierarchy = categoryService.getCategoryHierarchy(rootId);

        ApiResponse<List<CategoryDto>> response =
                ApiResponse.success(
                        "Category hierarchy retrieved successfully", hierarchy, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get child categories of a specific category.
     *
     * @param id      parent category ID
     * @param request HTTP request for path information
     * @return list of child categories
     */
    @GetMapping("/{id}/children")
    @Operation(
            summary = "Get Child Categories",
            description = "Retrieve all child categories of a specific category")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Child categories retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Parent category not found")
            })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getChildCategories(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.debug("Retrieving child categories for parent ID: {}", id);

        List<CategoryDto> children = categoryService.getChildCategories(id);

        ApiResponse<List<CategoryDto>> response =
                ApiResponse.success(
                        "Child categories retrieved successfully", children, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== BULK OPERATIONS =====

    /**
     * Bulk create categories.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PostMapping("/bulk")
    @Operation(
            summary = "Bulk Create Categories",
            description = "Create multiple categories in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<CategoryDto>>> bulkCreateCategories(
            @Valid @RequestBody BulkOperationRequest<CategoryDto> bulkRequest,
            HttpServletRequest request) {

        logger.info("Bulk creating {} categories", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<CategoryDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            CategoryDto categoryDto = bulkRequest.getItems().get(i);
            try {
                CategoryDto createdCategory = categoryService.createCategory(categoryDto);
                bulkResponse.addResult(createdCategory);
            } catch (Exception e) {
                logger.error("Failed to create category at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "CREATION_FAILED", categoryDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<CategoryDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update categories.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PutMapping("/bulk")
    @Operation(
            summary = "Bulk Update Categories",
            description = "Update multiple categories in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<CategoryDto>>> bulkUpdateCategories(
            @Valid @RequestBody BulkOperationRequest<CategoryDto> bulkRequest,
            HttpServletRequest request) {

        logger.info("Bulk updating {} categories", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<CategoryDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            CategoryDto categoryDto = bulkRequest.getItems().get(i);
            try {
                if (categoryDto.getId() == null) {
                    throw new IllegalArgumentException("Category ID is required for update operation");
                }

                CategoryDto updatedCategory =
                        categoryService.updateCategory(categoryDto.getId(), categoryDto);
                bulkResponse.addResult(updatedCategory);
            } catch (Exception e) {
                logger.error("Failed to update category at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "UPDATE_FAILED", categoryDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<CategoryDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk delete categories.
     *
     * @param ids     list of category IDs to delete
     * @param request HTTP request for path information
     * @return bulk operation result
     */
    @DeleteMapping("/bulk")
    @Operation(
            summary = "Bulk Delete Categories",
            description = "Delete multiple categories in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<Long>>> bulkDeleteCategories(
            @RequestBody List<Long> ids, HttpServletRequest request) {

        logger.info("Bulk deleting {} categories", ids.size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<Long> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            try {
                categoryService.deleteCategory(id);
                bulkResponse.addResult(id);
            } catch (Exception e) {
                logger.error("Failed to delete category at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(i, e.getMessage(), "DELETION_FAILED", id);
                bulkResponse.addError(error);
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<Long>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== HELPER METHODS =====

    private boolean hasFilters(String name, Long parentId, Integer level, Boolean isActive) {
        return StringUtils.hasText(name) || parentId != null || level != null || isActive != null;
    }
}
