package com.inventorymanagement.product.controller;

import com.inventorymanagement.common.model.ApiResponse;
import com.inventorymanagement.common.model.BulkOperationRequest;
import com.inventorymanagement.common.model.BulkOperationResponse;
import com.inventorymanagement.common.model.PagedResponse;
import com.inventorymanagement.product.model.ProductDto;
import com.inventorymanagement.product.service.ProductService;
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
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
 * REST controller for Product management operations. Provides comprehensive CRUD operations, search, filtering, and bulk operations.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/v1/products")
@Tag(name = "Product Management", description = "Operations for managing products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new product.
     *
     * @param productDto the product data
     * @param request    HTTP request for path information
     * @return the created product
     */
    @PostMapping
    @Operation(summary = "Create Product", description = "Create a new product")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Product created successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Product already exists")
            })
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductDto productDto, HttpServletRequest request) {

        logger.info("Creating new product: {}", productDto.getName());

        ProductDto createdProduct = productService.createProduct(productDto);

        ApiResponse<ProductDto> response =
                ApiResponse.success(
                        "Product created successfully", createdProduct, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get product by ID.
     *
     * @param id      the product ID
     * @param request HTTP request for path information
     * @return the product
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Product", description = "Retrieve a product by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Product retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.debug("Retrieving product with ID: {}", id);

        ProductDto product = productService.getProductById(id);

        ApiResponse<ProductDto> response =
                ApiResponse.success("Product retrieved successfully", product, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing product.
     *
     * @param id         the product ID
     * @param productDto the updated product data
     * @param request    HTTP request for path information
     * @return the updated product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Product", description = "Update an existing product")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Product updated successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found")
            })
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody ProductDto productDto,
            HttpServletRequest request) {

        logger.info("Updating product with ID: {}", id);

        ProductDto updatedProduct = productService.updateProduct(id, productDto);

        ApiResponse<ProductDto> response =
                ApiResponse.success(
                        "Product updated successfully", updatedProduct, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a product.
     *
     * @param id      the product ID
     * @param request HTTP request for path information
     * @return confirmation message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Product", description = "Delete a product by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Product deleted successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Product has dependencies")
            })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.info("Deleting product with ID: {}", id);

        productService.deleteProduct(id);

        ApiResponse<Void> response =
                ApiResponse.success("Product deleted successfully", null, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== SEARCH AND FILTERING =====

    /**
     * Get all products with pagination and filtering.
     *
     * @param page         page number (0-based)
     * @param size         page size
     * @param sortBy       sort field
     * @param sortDir      sort direction
     * @param name         filter by name
     * @param categoryId   filter by category ID
     * @param brand        filter by brand
     * @param manufacturer filter by manufacturer
     * @param minPrice     filter by minimum price
     * @param maxPrice     filter by maximum price
     * @param isActive     filter by active status
     * @param request      HTTP request for path information
     * @return paginated list of products
     */
    @GetMapping
    @Operation(
            summary = "Get All Products",
            description = "Retrieve all products with pagination and filtering")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Products retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            HttpServletRequest request) {

        logger.debug(
                "Retrieving products with filters - page: {}, size: {}, name: {}, categoryId: {}, brand: {}, isActive: {}",
                page,
                size,
                name,
                categoryId,
                brand,
                isActive);

        // Build sort
        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply filters and get results
        Page<ProductDto> productsPage;
        if (hasFilters(name, categoryId, brand, manufacturer, minPrice, maxPrice, isActive)) {
            productsPage =
                    productService.searchProducts(
                            name, categoryId, brand, manufacturer, minPrice, maxPrice, isActive, pageable);
        } else {
            productsPage = productService.getAllProducts(pageable);
        }

        // Build response with pagination links
        String baseUrl = request.getRequestURL().toString();
        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productsPage, baseUrl);

        ApiResponse<PagedResponse<ProductDto>> response =
                ApiResponse.success(
                        "Products retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Search products by name or description.
     *
     * @param query   search query
     * @param page    page number
     * @param size    page size
     * @param request HTTP request for path information
     * @return search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search Products", description = "Search products by name or description")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Search completed successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid search parameters"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> searchProducts(
            @RequestParam @Parameter(description = "Search query") String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Searching products with query: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productsPage =
                productService.searchProductsByNameOrDescription(query, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productsPage, baseUrl);

        ApiResponse<PagedResponse<ProductDto>> response =
                ApiResponse.success(
                        "Search completed successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get products by category.
     *
     * @param categoryId the category ID
     * @param page       page number
     * @param size       page size
     * @param request    HTTP request for path information
     * @return products in the category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Get Products by Category",
            description = "Retrieve products in a specific category")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Products retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Category not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getProductsByCategory(
            @PathVariable @NotNull Long categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Retrieving products for category ID: {}", categoryId);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productsPage = productService.getProductsByCategory(categoryId, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productsPage, baseUrl);

        ApiResponse<PagedResponse<ProductDto>> response =
                ApiResponse.success(
                        "Products retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get products by brand.
     *
     * @param brand   the brand name
     * @param page    page number
     * @param size    page size
     * @param request HTTP request for path information
     * @return products by brand
     */
    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get Products by Brand", description = "Retrieve products by brand")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Products retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getProductsByBrand(
            @PathVariable @NotNull String brand,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Retrieving products for brand: {}", brand);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productsPage = productService.getProductsByBrand(brand, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<ProductDto> pagedResponse = PagedResponse.of(productsPage, baseUrl);

        ApiResponse<PagedResponse<ProductDto>> response =
                ApiResponse.success(
                        "Products retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== BULK OPERATIONS =====

    /**
     * Bulk create products.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PostMapping("/bulk")
    @Operation(
            summary = "Bulk Create Products",
            description = "Create multiple products in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<ProductDto>>> bulkCreateProducts(
            @Valid @RequestBody BulkOperationRequest<ProductDto> bulkRequest,
            HttpServletRequest request) {

        logger.info("Bulk creating {} products", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<ProductDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            ProductDto productDto = bulkRequest.getItems().get(i);
            try {
                ProductDto createdProduct = productService.createProduct(productDto);
                bulkResponse.addResult(createdProduct);
            } catch (Exception e) {
                logger.error("Failed to create product at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "CREATION_FAILED", productDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<ProductDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update products.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PutMapping("/bulk")
    @Operation(
            summary = "Bulk Update Products",
            description = "Update multiple products in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<ProductDto>>> bulkUpdateProducts(
            @Valid @RequestBody BulkOperationRequest<ProductDto> bulkRequest,
            HttpServletRequest request) {

        logger.info("Bulk updating {} products", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<ProductDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            ProductDto productDto = bulkRequest.getItems().get(i);
            try {
                if (productDto.getId() == null) {
                    throw new IllegalArgumentException("Product ID is required for update operation");
                }

                ProductDto updatedProduct = productService.updateProduct(productDto.getId(), productDto);
                bulkResponse.addResult(updatedProduct);
            } catch (Exception e) {
                logger.error("Failed to update product at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "UPDATE_FAILED", productDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<ProductDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk delete products.
     *
     * @param ids     list of product IDs to delete
     * @param request HTTP request for path information
     * @return bulk operation result
     */
    @DeleteMapping("/bulk")
    @Operation(
            summary = "Bulk Delete Products",
            description = "Delete multiple products in a single operation")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bulk operation completed",
                            content = @Content(schema = @Schema(implementation = BulkOperationResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden")
            })
    public ResponseEntity<ApiResponse<BulkOperationResponse<Long>>> bulkDeleteProducts(
            @RequestBody List<Long> ids, HttpServletRequest request) {

        logger.info("Bulk deleting {} products", ids.size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<Long> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            try {
                productService.deleteProduct(id);
                bulkResponse.addResult(id);
            } catch (Exception e) {
                logger.error("Failed to delete product at index {}: {}", i, e.getMessage());

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

    private boolean hasFilters(
            String name,
            Long categoryId,
            String brand,
            String manufacturer,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive) {
        return StringUtils.hasText(name)
                || categoryId != null
                || StringUtils.hasText(brand)
                || StringUtils.hasText(manufacturer)
                || minPrice != null
                || maxPrice != null
                || isActive != null;
    }
}
