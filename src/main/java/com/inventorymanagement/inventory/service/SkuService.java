package com.inventorymanagement.inventory.service;

import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.InsufficientStockException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.inventory.model.SkuMapper;
import com.inventorymanagement.inventory.repository.SkuRepository;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service class for managing SKU entities and business operations. Provides comprehensive SKU management including inventory tracking, stock
 * operations, and automatic SKU code generation.
 */
@Service
@Transactional
public class SkuService {

    private static final Logger logger = LoggerFactory.getLogger(SkuService.class);
    private static final String SKU_ENTITY_NAME = "SKU";
    private static final int MAX_SKU_CODE_RETRY_ATTEMPTS = 5;
    private static final String SKU_CODE_PREFIX = "SKU";
    private static final SecureRandom random = new SecureRandom();

    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Autowired
    public SkuService(
            SkuRepository skuRepository,
            ProductRepository productRepository,
            InventoryService inventoryService) {
        this.skuRepository = skuRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    // ===== CRUD OPERATIONS =====

    /**
     * Creates a new SKU with business rule validation and automatic SKU code generation.
     *
     * @param skuDto the SKU data
     * @return the created SKU DTO
     * @throws ValidationException     if validation fails
     * @throws EntityNotFoundException if product not found
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto createSku(@Valid @NotNull SkuDto skuDto) {
        logger.info("Creating new SKU for product: {}", skuDto.getProductId());

        validateSkuForCreation(skuDto);

        Sku sku = SkuMapper.toEntity(skuDto);

        // Set up product relationship
        Product product = findProductById(skuDto.getProductId());
        validateProductForSku(product);
        sku.setProduct(product);

        // Generate SKU code if not provided
        if (!StringUtils.hasText(sku.getSkuCode())) {
            String generatedSkuCode = generateUniqueSkuCode(product);
            sku.setSkuCode(generatedSkuCode);
        }

        Sku savedSku = skuRepository.save(sku);

        // Create initial inventory transaction if stock quantity is set
        if (savedSku.getStockQuantity() > 0) {
            inventoryService.recordStockIn(
                    savedSku.getId(),
                    savedSku.getStockQuantity(),
                    "INITIAL_STOCK",
                    "Initial stock creation",
                    "SYSTEM");
        }

        logger.info(
                "Successfully created SKU with ID: {} and code: {}",
                savedSku.getId(),
                savedSku.getSkuCode());
        return SkuMapper.toDto(savedSku);
    }

    /**
     * Updates an existing SKU with business rule validation.
     *
     * @param id     the SKU ID
     * @param skuDto the updated SKU data
     * @return the updated SKU DTO
     * @throws EntityNotFoundException if SKU not found
     * @throws ValidationException     if validation fails
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto updateSku(@NotNull Long id, @Valid @NotNull SkuDto skuDto) {
        logger.info("Updating SKU with ID: {}", id);

        Sku existingSku = findSkuById(id);
        validateSkuForUpdate(existingSku, skuDto);

        // Store original stock quantity for comparison
        Integer originalStockQuantity = existingSku.getStockQuantity();

        // Update basic fields
        existingSku.setVariantName(skuDto.getVariantName());
        existingSku.setSize(skuDto.getSize());
        existingSku.setColor(skuDto.getColor());
        existingSku.setPrice(skuDto.getPrice());
        existingSku.setCost(skuDto.getCost());
        existingSku.setReorderPoint(skuDto.getReorderPoint());
        existingSku.setReorderQuantity(skuDto.getReorderQuantity());
        existingSku.setBarcode(skuDto.getBarcode());
        existingSku.setLocation(skuDto.getLocation());
        existingSku.setMetadata(skuDto.getMetadata());

        // Handle stock quantity changes
        if (!originalStockQuantity.equals(skuDto.getStockQuantity())) {
            int adjustment = skuDto.getStockQuantity() - originalStockQuantity;
            existingSku.setStockQuantity(skuDto.getStockQuantity());

            // Record inventory transaction for the adjustment
            if (adjustment != 0) {
                inventoryService.recordStockAdjustment(
                        existingSku.getId(),
                        adjustment,
                        "MANUAL_ADJUSTMENT",
                        "Manual stock adjustment",
                        "SYSTEM");
            }
        }

        // Handle product change
        if (shouldUpdateProduct(existingSku, skuDto)) {
            Product newProduct = findProductById(skuDto.getProductId());
            validateProductForSku(newProduct);
            existingSku.setProduct(newProduct);
        }

        Sku updatedSku = skuRepository.save(existingSku);
        logger.info("Successfully updated SKU with ID: {}", id);

        return SkuMapper.toDto(updatedSku);
    }

    /**
     * Retrieves a SKU by ID.
     *
     * @param id the SKU ID
     * @return the SKU DTO
     * @throws EntityNotFoundException if SKU not found
     */
    @Cacheable(value = "skus", key = "#id")
    public SkuDto getSkuById(@NotNull Long id) {
        logger.debug("Retrieving SKU with ID: {}", id);
        Sku sku = findSkuById(id);
        return SkuMapper.toDto(sku);
    }

    /**
     * Retrieves a SKU by SKU code.
     *
     * @param skuCode the SKU code
     * @return the SKU DTO
     * @throws EntityNotFoundException if SKU not found
     */
    @Cacheable(value = "skus", key = "'code:' + #skuCode")
    public SkuDto getSkuByCode(@NotNull String skuCode) {
        logger.debug("Retrieving SKU with code: {}", skuCode);
        Sku sku =
                skuRepository
                        .findBySkuCode(skuCode)
                        .orElseThrow(() -> new EntityNotFoundException(SKU_ENTITY_NAME, skuCode));
        return SkuMapper.toDto(sku);
    }

    /**
     * Retrieves all SKUs with pagination.
     *
     * @param pageable pagination information
     * @return page of SKU DTOs
     */
    public Page<SkuDto> getAllSkus(Pageable pageable) {
        logger.debug("Retrieving all SKUs with pagination");
        Page<Sku> skus = skuRepository.findAllActive(pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Soft deletes a SKU.
     *
     * @param id the SKU ID
     * @throws EntityNotFoundException if SKU not found
     * @throws BusinessException       if SKU has reserved stock
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void deleteSku(@NotNull Long id) {
        logger.info("Deleting SKU with ID: {}", id);

        Sku sku = findSkuById(id);
        validateSkuForDeletion(sku);

        // Release any reserved stock before deletion
        if (sku.getReservedQuantity() > 0) {
            sku.releaseReservedStock(sku.getReservedQuantity());
            inventoryService.recordStockRelease(
                    sku.getId(),
                    sku.getReservedQuantity(),
                    null,
                    "DELETION",
                    "Stock released due to SKU deletion",
                    "SYSTEM");
        }

        // Soft delete the SKU
        sku.markAsDeleted();
        skuRepository.save(sku);

        logger.info("Successfully deleted SKU with ID: {}", id);
    }

    // ===== PRODUCT-BASED OPERATIONS =====

    /**
     * Retrieves all SKUs for a specific product.
     *
     * @param productId the product ID
     * @param pageable  pagination information
     * @return page of SKU DTOs
     * @throws EntityNotFoundException if product not found
     */
    @Cacheable(
            value = "skus",
            key = "'product:' + #productId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<SkuDto> getSkusByProduct(@NotNull Long productId, Pageable pageable) {
        logger.debug("Retrieving SKUs for product ID: {}", productId);

        // Verify product exists
        findProductById(productId);

        Page<Sku> skus = skuRepository.findByProductId(productId, pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Retrieves all active SKUs for a specific product.
     *
     * @param productId the product ID
     * @return list of active SKU DTOs
     * @throws EntityNotFoundException if product not found
     */
    public List<SkuDto> getActiveSkusByProduct(@NotNull Long productId) {
        logger.debug("Retrieving active SKUs for product ID: {}", productId);

        // Verify product exists
        findProductById(productId);

        List<Sku> skus = skuRepository.findActiveByProductId(productId);
        return skus.stream().map(SkuMapper::toDto).toList();
    }

    // ===== INVENTORY OPERATIONS =====

    /**
     * Reserves stock for a SKU.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity to reserve
     * @param referenceId   the reference ID for the reservation
     * @param referenceType the reference type (e.g., "ORDER", "ALLOCATION")
     * @param reason        the reason for the reservation
     * @param performedBy   who performed the reservation
     * @throws EntityNotFoundException    if SKU not found
     * @throws InsufficientStockException if insufficient stock available
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void reserveStock(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info("Reserving {} units of stock for SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);

        if (!sku.getIsActive()) {
            throw new BusinessException("Cannot reserve stock for inactive SKU");
        }

        if (!sku.reserveStock(quantity)) {
            throw new InsufficientStockException(sku.getSkuCode(), quantity, sku.getAvailableQuantity());
        }

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockReservation(
                skuId, quantity, referenceId, referenceType, reason, performedBy);

        logger.info("Successfully reserved {} units for SKU ID: {}", quantity, skuId);
    }

    /**
     * Releases reserved stock for a SKU.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity to release
     * @param referenceId   the reference ID for the release
     * @param referenceType the reference type
     * @param reason        the reason for the release
     * @param performedBy   who performed the release
     * @throws EntityNotFoundException if SKU not found
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void releaseReservedStock(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info("Releasing {} units of reserved stock for SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);
        Integer actualReleased = sku.releaseReservedStock(quantity);

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockRelease(
                skuId, actualReleased, referenceId, referenceType, reason, performedBy);

        logger.info("Successfully released {} units for SKU ID: {}", actualReleased, skuId);
    }

    /**
     * Fulfills an order by reducing both stock and reserved quantities.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity to fulfill
     * @param referenceId   the reference ID for the fulfillment
     * @param referenceType the reference type (e.g., "ORDER", "SHIPMENT")
     * @param reason        the reason for the fulfillment
     * @param performedBy   who performed the fulfillment
     * @throws EntityNotFoundException    if SKU not found
     * @throws InsufficientStockException if insufficient reserved stock
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void fulfillOrder(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info("Fulfilling order for {} units of SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);

        if (!sku.fulfillOrder(quantity)) {
            throw new InsufficientStockException(sku.getSkuCode(), quantity, sku.getReservedQuantity());
        }

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockOut(
                skuId, quantity, referenceId, referenceType, reason, performedBy);

        logger.info("Successfully fulfilled order for {} units of SKU ID: {}", quantity, skuId);
    }

    /**
     * Adjusts stock quantity for a SKU.
     *
     * @param skuId       the SKU ID
     * @param adjustment  the adjustment amount (positive for increase, negative for decrease)
     * @param reason      the reason for the adjustment
     * @param performedBy who performed the adjustment
     * @throws EntityNotFoundException if SKU not found
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void adjustStock(
            @NotNull Long skuId, @NotNull Integer adjustment, String reason, String performedBy) {
        logger.info("Adjusting stock by {} units for SKU ID: {}", adjustment, skuId);

        if (adjustment == 0) {
            throw new ValidationException("adjustment", "Adjustment cannot be zero");
        }

        Sku sku = findSkuById(skuId);
        Integer newQuantity = sku.adjustStock(adjustment);

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockAdjustment(
                skuId, adjustment, "MANUAL_ADJUSTMENT", reason, performedBy);

        logger.info("Successfully adjusted stock to {} units for SKU ID: {}", newQuantity, skuId);
    }

    // ===== SEARCH AND FILTER OPERATIONS =====

    /**
     * Searches SKUs by various criteria.
     *
     * @param searchTerm the search term for SKU code or variant name
     * @param productId  the product ID filter (optional)
     * @param isActive   the active status filter (optional)
     * @param pageable   pagination information
     * @return page of matching SKU DTOs
     */
    public Page<SkuDto> searchSkus(
            String searchTerm, Long productId, Boolean isActive, Pageable pageable) {
        logger.debug(
                "Searching SKUs with criteria - searchTerm: {}, productId: {}, isActive: {}",
                searchTerm,
                productId,
                isActive);

        Page<Sku> skus =
                skuRepository.findWithExtendedFilters(
                        searchTerm, productId, null, null, null, isActive, null, pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Searches SKUs with extended criteria.
     *
     * @param skuCode     filter by SKU code (optional)
     * @param productId   filter by product ID (optional)
     * @param variantName filter by variant name (optional)
     * @param minPrice    filter by minimum price (optional)
     * @param maxPrice    filter by maximum price (optional)
     * @param isActive    filter by active status (optional)
     * @param isLowStock  filter by low stock status (optional)
     * @param pageable    pagination information
     * @return page of matching SKU DTOs
     */
    public Page<SkuDto> searchSkus(
            String skuCode,
            Long productId,
            String variantName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean isLowStock,
            Pageable pageable) {
        logger.debug(
                "Searching SKUs with extended criteria - skuCode: {}, productId: {}, variantName: {}, minPrice: {}, maxPrice: {}, isActive: {}, isLowStock: {}",
                skuCode,
                productId,
                variantName,
                minPrice,
                maxPrice,
                isActive,
                isLowStock);

        Page<Sku> skus =
                skuRepository.findWithExtendedFilters(
                        skuCode, productId, variantName, minPrice, maxPrice, isActive, isLowStock, pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Searches SKUs by code or variant name.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return page of matching SKU DTOs
     */
    public Page<SkuDto> searchSkusByCodeOrVariant(@NotNull String query, Pageable pageable) {
        logger.debug("Searching SKUs by code or variant: {}", query);

        if (!StringUtils.hasText(query)) {
            throw new ValidationException("query", "Search query cannot be empty");
        }

        Page<Sku> skus = skuRepository.findBySkuCodeOrVariantNameContainingIgnoreCase(query, pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Gets SKUs with low stock.
     *
     * @param pageable pagination information
     * @return page of low stock SKU DTOs
     */
    public Page<SkuDto> getLowStockSkus(Pageable pageable) {
        logger.debug("Retrieving low stock SKUs");
        Page<Sku> skus = skuRepository.findLowStockSkus(pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Adds stock to a SKU.
     *
     * @param skuId    the SKU ID
     * @param quantity the quantity to add
     * @return updated SKU DTO
     * @throws EntityNotFoundException if SKU not found
     * @throws ValidationException     if quantity is invalid
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto addStock(@NotNull Long skuId, @NotNull Integer quantity) {
        logger.info("Adding {} stock to SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);
        sku.adjustStock(quantity);

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockAdjustment(
                skuId, quantity, "STOCK_ADDITION", "Manual stock addition", "SYSTEM");

        logger.info("Successfully added {} stock to SKU ID: {}", quantity, skuId);
        return SkuMapper.toDto(sku);
    }

    /**
     * Removes stock from a SKU.
     *
     * @param skuId    the SKU ID
     * @param quantity the quantity to remove
     * @return updated SKU DTO
     * @throws EntityNotFoundException    if SKU not found
     * @throws ValidationException        if quantity is invalid
     * @throws InsufficientStockException if insufficient stock
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto removeStock(@NotNull Long skuId, @NotNull Integer quantity) {
        logger.info("Removing {} stock from SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);

        if (sku.getStockQuantity() < quantity) {
            throw new InsufficientStockException(sku.getSkuCode(), quantity, sku.getStockQuantity());
        }

        sku.adjustStock(-quantity);

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockAdjustment(
                skuId, -quantity, "STOCK_REMOVAL", "Manual stock removal", "SYSTEM");

        logger.info("Successfully removed {} stock from SKU ID: {}", quantity, skuId);
        return SkuMapper.toDto(sku);
    }

    /**
     * Reserves stock for a SKU.
     *
     * @param skuId    the SKU ID
     * @param quantity the quantity to reserve
     * @return updated SKU DTO
     * @throws EntityNotFoundException    if SKU not found
     * @throws ValidationException        if quantity is invalid
     * @throws InsufficientStockException if insufficient stock
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto reserveStock(@NotNull Long skuId, @NotNull Integer quantity) {
        logger.info("Reserving {} stock for SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);

        if (!sku.getIsActive()) {
            throw new BusinessException("Cannot reserve stock for inactive SKU");
        }

        if (!sku.reserveStock(quantity)) {
            throw new InsufficientStockException(sku.getSkuCode(), quantity, sku.getAvailableQuantity());
        }

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockReservation(
                skuId, quantity, null, "MANUAL_RESERVATION", "Manual stock reservation", "SYSTEM");

        logger.info("Successfully reserved {} stock for SKU ID: {}", quantity, skuId);
        return SkuMapper.toDto(sku);
    }

    /**
     * Releases reserved stock for a SKU.
     *
     * @param skuId    the SKU ID
     * @param quantity the quantity to release
     * @return updated SKU DTO
     * @throws EntityNotFoundException if SKU not found
     * @throws ValidationException     if quantity is invalid
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public SkuDto releaseStock(@NotNull Long skuId, @NotNull Integer quantity) {
        logger.info("Releasing {} reserved stock for SKU ID: {}", quantity, skuId);

        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }

        Sku sku = findSkuById(skuId);
        Integer actualReleased = sku.releaseReservedStock(quantity);

        skuRepository.save(sku);

        // Record inventory transaction
        inventoryService.recordStockRelease(
                skuId, actualReleased, null, "MANUAL_RELEASE", "Manual stock release", "SYSTEM");

        logger.info("Successfully released {} reserved stock for SKU ID: {}", actualReleased, skuId);
        return SkuMapper.toDto(sku);
    }

    /**
     * Finds SKUs with low stock (below reorder point).
     *
     * @param pageable pagination information
     * @return page of SKU DTOs with low stock
     */
    public Page<SkuDto> getSkusWithLowStock(Pageable pageable) {
        logger.debug("Retrieving SKUs with low stock");
        Page<Sku> skus = skuRepository.findLowStockSkus(pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Finds SKUs that are out of stock.
     *
     * @param pageable pagination information
     * @return page of SKU DTOs that are out of stock
     */
    public Page<SkuDto> getOutOfStockSkus(Pageable pageable) {
        logger.debug("Retrieving out of stock SKUs");
        Page<Sku> skus = skuRepository.findOutOfStockSkus(pageable);
        return skus.map(SkuMapper::toDto);
    }

    /**
     * Finds SKUs in a price range.
     *
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @param pageable pagination information
     * @return page of SKU DTOs in the price range
     */
    public Page<SkuDto> getSkusByPriceRange(
            @NotNull BigDecimal minPrice, @NotNull BigDecimal maxPrice, Pageable pageable) {
        logger.debug("Retrieving SKUs in price range: {} - {}", minPrice, maxPrice);

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new ValidationException(
                    "priceRange", "Minimum price cannot be greater than maximum price");
        }

        Page<Sku> skus = skuRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return skus.map(SkuMapper::toDto);
    }

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Activates a SKU.
     *
     * @param id the SKU ID
     * @throws EntityNotFoundException if SKU not found
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void activateSku(@NotNull Long id) {
        logger.info("Activating SKU with ID: {}", id);

        Sku sku = findSkuById(id);

        // Validate that product is active
        if (!sku.getProduct().getIsActive()) {
            throw new BusinessException("Cannot activate SKU for inactive product");
        }

        sku.activate();
        skuRepository.save(sku);

        logger.info("Successfully activated SKU with ID: {}", id);
    }

    /**
     * Deactivates a SKU.
     *
     * @param id the SKU ID
     * @throws EntityNotFoundException if SKU not found
     */
    @CacheEvict(
            value = {"skus", "inventory"},
            allEntries = true)
    public void deactivateSku(@NotNull Long id) {
        logger.info("Deactivating SKU with ID: {}", id);

        Sku sku = findSkuById(id);

        // Release any reserved stock before deactivation
        if (sku.getReservedQuantity() > 0) {
            sku.releaseReservedStock(sku.getReservedQuantity());
            inventoryService.recordStockRelease(
                    sku.getId(),
                    sku.getReservedQuantity(),
                    null,
                    "DEACTIVATION",
                    "Stock released due to SKU deactivation",
                    "SYSTEM");
        }

        sku.deactivate();
        skuRepository.save(sku);

        logger.info("Successfully deactivated SKU with ID: {}", id);
    }

    // ===== PRIVATE HELPER METHODS =====

    private Sku findSkuById(Long id) {
        return skuRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(SKU_ENTITY_NAME, id));
    }

    private Product findProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product", id));
    }

    private void validateSkuForCreation(SkuDto skuDto) {
        if (skuDto.getProductId() == null) {
            throw new ValidationException("productId", "Product is required");
        }

        if (skuDto.getPrice() == null || skuDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("price", "Price is required and must be non-negative");
        }

        // Check for duplicate SKU code if provided
        if (StringUtils.hasText(skuDto.getSkuCode())) {
            Optional<Sku> existing = skuRepository.findBySkuCode(skuDto.getSkuCode());
            if (existing.isPresent()) {
                throw new ValidationException("skuCode", "SKU code already exists");
            }
        }

        // Validate stock quantities
        validateStockQuantities(skuDto);
    }

    private void validateSkuForUpdate(Sku existingSku, SkuDto skuDto) {
        if (skuDto.getProductId() == null) {
            throw new ValidationException("productId", "Product is required");
        }

        if (skuDto.getPrice() == null || skuDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("price", "Price is required and must be non-negative");
        }

        // Check for duplicate SKU code if changed
        if (StringUtils.hasText(skuDto.getSkuCode())
                && !existingSku.getSkuCode().equals(skuDto.getSkuCode())) {
            Optional<Sku> existing = skuRepository.findBySkuCode(skuDto.getSkuCode());
            if (existing.isPresent()) {
                throw new ValidationException("skuCode", "SKU code already exists");
            }
        }

        // Validate stock quantities
        validateStockQuantities(skuDto);
    }

    private void validateStockQuantities(SkuDto skuDto) {
        if (skuDto.getStockQuantity() != null && skuDto.getStockQuantity() < 0) {
            throw new ValidationException("stockQuantity", "Stock quantity cannot be negative");
        }

        if (skuDto.getReservedQuantity() != null && skuDto.getReservedQuantity() < 0) {
            throw new ValidationException("reservedQuantity", "Reserved quantity cannot be negative");
        }

        if (skuDto.getReorderPoint() != null && skuDto.getReorderPoint() < 0) {
            throw new ValidationException("reorderPoint", "Reorder point cannot be negative");
        }

        if (skuDto.getReorderQuantity() != null && skuDto.getReorderQuantity() < 0) {
            throw new ValidationException("reorderQuantity", "Reorder quantity cannot be negative");
        }
    }

    private void validateProductForSku(Product product) {
        if (!product.getIsActive()) {
            throw new BusinessException("Cannot create SKU for inactive product");
        }
    }

    private void validateSkuForDeletion(Sku sku) {
        // Can delete SKU even if it has reserved stock, but we'll release it first
        // This is a business decision - could be stricter if needed
    }

    private boolean shouldUpdateProduct(Sku existingSku, SkuDto skuDto) {
        return !existingSku.getProduct().getId().equals(skuDto.getProductId());
    }

    /**
     * Generates a unique SKU code with retry logic.
     *
     * @param product the product for which to generate the SKU code
     * @return unique SKU code
     * @throws BusinessException if unable to generate unique code after max retries
     */
    private String generateUniqueSkuCode(Product product) {
        String baseCode = generateBaseSkuCode(product);

        for (int attempt = 0; attempt < MAX_SKU_CODE_RETRY_ATTEMPTS; attempt++) {
            String skuCode = baseCode + generateRandomSuffix();

            if (skuRepository.findBySkuCode(skuCode).isEmpty()) {
                return skuCode;
            }
        }

        throw new BusinessException(
                "Unable to generate unique SKU code after " + MAX_SKU_CODE_RETRY_ATTEMPTS + " attempts");
    }

    private String generateBaseSkuCode(Product product) {
        StringBuilder codeBuilder = new StringBuilder();

        // Add prefix
        codeBuilder.append(SKU_CODE_PREFIX);

        // Add category abbreviation if available
        if (product.getCategory() != null && StringUtils.hasText(product.getCategory().getName())) {
            String categoryAbbr =
                    product
                            .getCategory()
                            .getName()
                            .toUpperCase()
                            .replaceAll("[^A-Z0-9]", "")
                            .substring(0, Math.min(3, product.getCategory().getName().length()));
            codeBuilder.append(categoryAbbr);
        }

        // Add product abbreviation if available
        if (StringUtils.hasText(product.getName())) {
            String productAbbr =
                    product
                            .getName()
                            .toUpperCase()
                            .replaceAll("[^A-Z0-9]", "")
                            .substring(0, Math.min(3, product.getName().length()));
            codeBuilder.append(productAbbr);
        }

        // Add date component
        String dateComponent = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        codeBuilder.append(dateComponent);

        return codeBuilder.toString();
    }

    private String generateRandomSuffix() {
        return String.format("%03d", random.nextInt(1000));
    }
}
