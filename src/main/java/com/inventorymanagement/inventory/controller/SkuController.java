package com.inventorymanagement.inventory.controller;

import com.inventorymanagement.common.model.ApiResponse;
import com.inventorymanagement.common.model.BulkOperationRequest;
import com.inventorymanagement.common.model.BulkOperationResponse;
import com.inventorymanagement.common.model.PagedResponse;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.inventory.service.InventoryService;
import com.inventorymanagement.inventory.service.SkuService;
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
 * REST controller for SKU management operations. Provides comprehensive CRUD operations, search, filtering, bulk operations, and inventory
 * management.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/v1/skus")
@Tag(name = "SKU Management", description = "Operations for managing SKUs and inventory")
public class SkuController {

    private static final Logger logger = LoggerFactory.getLogger(SkuController.class);

    @Autowired
    private SkuService skuService;

    @Autowired
    private InventoryService inventoryService;

    // ===== CRUD OPERATIONS =====

    /**
     * Create a new SKU.
     *
     * @param skuDto  the SKU data
     * @param request HTTP request for path information
     * @return the created SKU
     */
    @PostMapping
    @Operation(summary = "Create SKU", description = "Create a new SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "SKU created successfully",
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
                            description = "SKU already exists")
            })
    public ResponseEntity<ApiResponse<SkuDto>> createSku(
            @Valid @RequestBody SkuDto skuDto, HttpServletRequest request) {

        logger.info("Creating new SKU: {}", skuDto.getSkuCode());

        SkuDto createdSku = skuService.createSku(skuDto);

        ApiResponse<SkuDto> response =
                ApiResponse.success("SKU created successfully", createdSku, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get SKU by ID.
     *
     * @param id      the SKU ID
     * @param request HTTP request for path information
     * @return the SKU
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get SKU", description = "Retrieve a SKU by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKU retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<SkuDto>> getSkuById(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.debug("Retrieving SKU with ID: {}", id);

        SkuDto sku = skuService.getSkuById(id);

        ApiResponse<SkuDto> response =
                ApiResponse.success("SKU retrieved successfully", sku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get SKU by SKU code.
     *
     * @param skuCode the SKU code
     * @param request HTTP request for path information
     * @return the SKU
     */
    @GetMapping("/code/{skuCode}")
    @Operation(summary = "Get SKU by Code", description = "Retrieve a SKU by its code")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKU retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<SkuDto>> getSkuByCode(
            @PathVariable @NotNull String skuCode, HttpServletRequest request) {

        logger.debug("Retrieving SKU with code: {}", skuCode);

        SkuDto sku = skuService.getSkuByCode(skuCode);

        ApiResponse<SkuDto> response =
                ApiResponse.success("SKU retrieved successfully", sku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing SKU.
     *
     * @param id      the SKU ID
     * @param skuDto  the updated SKU data
     * @param request HTTP request for path information
     * @return the updated SKU
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update SKU", description = "Update an existing SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKU updated successfully",
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
                            description = "SKU not found")
            })
    public ResponseEntity<ApiResponse<SkuDto>> updateSku(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody SkuDto skuDto,
            HttpServletRequest request) {

        logger.info("Updating SKU with ID: {}", id);

        SkuDto updatedSku = skuService.updateSku(id, skuDto);

        ApiResponse<SkuDto> response =
                ApiResponse.success("SKU updated successfully", updatedSku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a SKU.
     *
     * @param id      the SKU ID
     * @param request HTTP request for path information
     * @return confirmation message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SKU", description = "Delete a SKU by its ID")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKU deleted successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "SKU has dependencies")
            })
    public ResponseEntity<ApiResponse<Void>> deleteSku(
            @PathVariable @NotNull Long id, HttpServletRequest request) {

        logger.info("Deleting SKU with ID: {}", id);

        skuService.deleteSku(id);

        ApiResponse<Void> response =
                ApiResponse.success("SKU deleted successfully", null, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== SEARCH AND FILTERING =====

    /**
     * Get all SKUs with pagination and filtering.
     *
     * @param page        page number (0-based)
     * @param size        page size
     * @param sortBy      sort field
     * @param sortDir     sort direction
     * @param skuCode     filter by SKU code
     * @param productId   filter by product ID
     * @param variantName filter by variant name
     * @param minPrice    filter by minimum price
     * @param maxPrice    filter by maximum price
     * @param isActive    filter by active status
     * @param isLowStock  filter by low stock status
     * @param request     HTTP request for path information
     * @return paginated list of SKUs
     */
    @GetMapping
    @Operation(
            summary = "Get All SKUs",
            description = "Retrieve all SKUs with pagination and filtering")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKUs retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<SkuDto>>> getAllSkus(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "skuCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String skuCode,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String variantName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isLowStock,
            HttpServletRequest request) {

        logger.debug(
                "Retrieving SKUs with filters - page: {}, size: {}, skuCode: {}, productId: {}, isActive: {}",
                page,
                size,
                skuCode,
                productId,
                isActive);

        // Build sort
        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply filters and get results
        Page<SkuDto> skusPage;
        if (hasFilters(skuCode, productId, variantName, minPrice, maxPrice, isActive, isLowStock)) {
            skusPage =
                    skuService.searchSkus(
                            skuCode, productId, variantName, minPrice, maxPrice, isActive, isLowStock, pageable);
        } else {
            skusPage = skuService.getAllSkus(pageable);
        }

        // Build response with pagination links
        String baseUrl = request.getRequestURL().toString();
        PagedResponse<SkuDto> pagedResponse = PagedResponse.of(skusPage, baseUrl);

        ApiResponse<PagedResponse<SkuDto>> response =
                ApiResponse.success("SKUs retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Search SKUs by code or variant name.
     *
     * @param query   search query
     * @param page    page number
     * @param size    page size
     * @param request HTTP request for path information
     * @return search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search SKUs", description = "Search SKUs by code or variant name")
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
    public ResponseEntity<ApiResponse<PagedResponse<SkuDto>>> searchSkus(
            @RequestParam @Parameter(description = "Search query") String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Searching SKUs with query: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<SkuDto> skusPage = skuService.searchSkusByCodeOrVariant(query, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<SkuDto> pagedResponse = PagedResponse.of(skusPage, baseUrl);

        ApiResponse<PagedResponse<SkuDto>> response =
                ApiResponse.success(
                        "Search completed successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get SKUs by product.
     *
     * @param productId the product ID
     * @param page      page number
     * @param size      page size
     * @param request   HTTP request for path information
     * @return SKUs for the product
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get SKUs by Product", description = "Retrieve SKUs for a specific product")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SKUs retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Product not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<SkuDto>>> getSkusByProduct(
            @PathVariable @NotNull Long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Retrieving SKUs for product ID: {}", productId);

        Pageable pageable = PageRequest.of(page, size);
        Page<SkuDto> skusPage = skuService.getSkusByProduct(productId, pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<SkuDto> pagedResponse = PagedResponse.of(skusPage, baseUrl);

        ApiResponse<PagedResponse<SkuDto>> response =
                ApiResponse.success("SKUs retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Get low stock SKUs.
     *
     * @param page    page number
     * @param size    page size
     * @param request HTTP request for path information
     * @return low stock SKUs
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get Low Stock SKUs", description = "Retrieve SKUs with low stock levels")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Low stock SKUs retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedResponse<SkuDto>>> getLowStockSkus(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest request) {

        logger.debug("Retrieving low stock SKUs");

        Pageable pageable = PageRequest.of(page, size);
        Page<SkuDto> skusPage = skuService.getLowStockSkus(pageable);

        String baseUrl = request.getRequestURL().toString();
        PagedResponse<SkuDto> pagedResponse = PagedResponse.of(skusPage, baseUrl);

        ApiResponse<PagedResponse<SkuDto>> response =
                ApiResponse.success(
                        "Low stock SKUs retrieved successfully", pagedResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== INVENTORY OPERATIONS =====

    /**
     * Add stock to a SKU.
     *
     * @param id       the SKU ID
     * @param quantity quantity to add
     * @param request  HTTP request for path information
     * @return updated SKU
     */
    @PostMapping("/{id}/stock/add")
    @Operation(summary = "Add Stock", description = "Add stock quantity to a SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Stock added successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found")
            })
    public ResponseEntity<ApiResponse<SkuDto>> addStock(
            @PathVariable @NotNull Long id,
            @RequestParam @Min(1) int quantity,
            HttpServletRequest request) {

        logger.info("Adding {} stock to SKU with ID: {}", quantity, id);

        SkuDto updatedSku = skuService.addStock(id, quantity);

        ApiResponse<SkuDto> response =
                ApiResponse.success("Stock added successfully", updatedSku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Remove stock from a SKU.
     *
     * @param id       the SKU ID
     * @param quantity quantity to remove
     * @param request  HTTP request for path information
     * @return updated SKU
     */
    @PostMapping("/{id}/stock/remove")
    @Operation(summary = "Remove Stock", description = "Remove stock quantity from a SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Stock removed successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity or insufficient stock"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found")
            })
    public ResponseEntity<ApiResponse<SkuDto>> removeStock(
            @PathVariable @NotNull Long id,
            @RequestParam @Min(1) int quantity,
            HttpServletRequest request) {

        logger.info("Removing {} stock from SKU with ID: {}", quantity, id);

        SkuDto updatedSku = skuService.removeStock(id, quantity);

        ApiResponse<SkuDto> response =
                ApiResponse.success("Stock removed successfully", updatedSku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Reserve stock for a SKU.
     *
     * @param id       the SKU ID
     * @param quantity quantity to reserve
     * @param request  HTTP request for path information
     * @return updated SKU
     */
    @PostMapping("/{id}/stock/reserve")
    @Operation(summary = "Reserve Stock", description = "Reserve stock quantity for a SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Stock reserved successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity or insufficient stock"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found")
            })
    public ResponseEntity<ApiResponse<SkuDto>> reserveStock(
            @PathVariable @NotNull Long id,
            @RequestParam @Min(1) int quantity,
            HttpServletRequest request) {

        logger.info("Reserving {} stock for SKU with ID: {}", quantity, id);

        SkuDto updatedSku = skuService.reserveStock(id, quantity);

        ApiResponse<SkuDto> response =
                ApiResponse.success("Stock reserved successfully", updatedSku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Release reserved stock for a SKU.
     *
     * @param id       the SKU ID
     * @param quantity quantity to release
     * @param request  HTTP request for path information
     * @return updated SKU
     */
    @PostMapping("/{id}/stock/release")
    @Operation(summary = "Release Stock", description = "Release reserved stock quantity for a SKU")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Stock released successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid quantity or insufficient reserved stock"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SKU not found")
            })
    public ResponseEntity<ApiResponse<SkuDto>> releaseStock(
            @PathVariable @NotNull Long id,
            @RequestParam @Min(1) int quantity,
            HttpServletRequest request) {

        logger.info("Releasing {} reserved stock for SKU with ID: {}", quantity, id);

        SkuDto updatedSku = skuService.releaseStock(id, quantity);

        ApiResponse<SkuDto> response =
                ApiResponse.success("Stock released successfully", updatedSku, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    // ===== BULK OPERATIONS =====

    /**
     * Bulk create SKUs.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PostMapping("/bulk")
    @Operation(
            summary = "Bulk Create SKUs",
            description = "Create multiple SKUs in a single operation")
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
    public ResponseEntity<ApiResponse<BulkOperationResponse<SkuDto>>> bulkCreateSkus(
            @Valid @RequestBody BulkOperationRequest<SkuDto> bulkRequest, HttpServletRequest request) {

        logger.info("Bulk creating {} SKUs", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<SkuDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            SkuDto skuDto = bulkRequest.getItems().get(i);
            try {
                SkuDto createdSku = skuService.createSku(skuDto);
                bulkResponse.addResult(createdSku);
            } catch (Exception e) {
                logger.error("Failed to create SKU at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "CREATION_FAILED", skuDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<SkuDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update SKUs.
     *
     * @param bulkRequest bulk operation request
     * @param request     HTTP request for path information
     * @return bulk operation result
     */
    @PutMapping("/bulk")
    @Operation(
            summary = "Bulk Update SKUs",
            description = "Update multiple SKUs in a single operation")
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
    public ResponseEntity<ApiResponse<BulkOperationResponse<SkuDto>>> bulkUpdateSkus(
            @Valid @RequestBody BulkOperationRequest<SkuDto> bulkRequest, HttpServletRequest request) {

        logger.info("Bulk updating {} SKUs", bulkRequest.getItems().size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<SkuDto> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            SkuDto skuDto = bulkRequest.getItems().get(i);
            try {
                if (skuDto.getId() == null) {
                    throw new IllegalArgumentException("SKU ID is required for update operation");
                }

                SkuDto updatedSku = skuService.updateSku(skuDto.getId(), skuDto);
                bulkResponse.addResult(updatedSku);
            } catch (Exception e) {
                logger.error("Failed to update SKU at index {}: {}", i, e.getMessage());

                BulkOperationResponse.BulkOperationError error =
                        new BulkOperationResponse.BulkOperationError(
                                i, e.getMessage(), "UPDATE_FAILED", skuDto);
                bulkResponse.addError(error);

                if (bulkRequest.getOptions() != null && !bulkRequest.getOptions().isContinueOnError()) {
                    break;
                }
            }
        }

        bulkResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        ApiResponse<BulkOperationResponse<SkuDto>> response =
                ApiResponse.success("Bulk operation completed", bulkResponse, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk delete SKUs.
     *
     * @param ids     list of SKU IDs to delete
     * @param request HTTP request for path information
     * @return bulk operation result
     */
    @DeleteMapping("/bulk")
    @Operation(
            summary = "Bulk Delete SKUs",
            description = "Delete multiple SKUs in a single operation")
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
    public ResponseEntity<ApiResponse<BulkOperationResponse<Long>>> bulkDeleteSkus(
            @RequestBody List<Long> ids, HttpServletRequest request) {

        logger.info("Bulk deleting {} SKUs", ids.size());

        long startTime = System.currentTimeMillis();
        BulkOperationResponse<Long> bulkResponse = new BulkOperationResponse<>();

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            try {
                skuService.deleteSku(id);
                bulkResponse.addResult(id);
            } catch (Exception e) {
                logger.error("Failed to delete SKU at index {}: {}", i, e.getMessage());

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
            String skuCode,
            Long productId,
            String variantName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean isLowStock) {
        return StringUtils.hasText(skuCode)
                || productId != null
                || StringUtils.hasText(variantName)
                || minPrice != null
                || maxPrice != null
                || isActive != null
                || isLowStock != null;
    }
}
