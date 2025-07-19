package com.inventorymanagement.inventory.repository;

import com.inventorymanagement.common.repository.BaseRepository;
import com.inventorymanagement.inventory.model.Sku;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for SKU entity. Provides inventory-specific query methods and stock management functionality.
 */
@Repository
public interface SkuRepository extends BaseRepository<Sku, Long> {

    // ===== PRODUCT-BASED QUERIES =====

    /**
     * Finds all SKUs for a specific product.
     *
     * @param productId the product ID
     * @return list of SKUs for the product
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.product.id = :productId AND s.deletedAt IS NULL ORDER BY s.variantName, s.skuCode")
    List<Sku> findByProductId(@Param("productId") Long productId);

    /**
     * Finds all SKUs for a specific product with pagination.
     *
     * @param productId the product ID
     * @param pageable  pagination information
     * @return page of SKUs for the product
     */
    @Query("SELECT s FROM Sku s WHERE s.product.id = :productId AND s.deletedAt IS NULL")
    Page<Sku> findByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * Finds all active SKUs for a specific product.
     *
     * @param productId the product ID
     * @return list of active SKUs for the product
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.product.id = :productId AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.variantName, s.skuCode")
    List<Sku> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Finds all SKUs for multiple products.
     *
     * @param productIds list of product IDs
     * @return list of SKUs for the specified products
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.product.id IN :productIds AND s.deletedAt IS NULL ORDER BY s.product.name, s.variantName, s.skuCode")
    List<Sku> findByProductIdIn(@Param("productIds") List<Long> productIds);

    // ===== SKU CODE QUERIES =====

    /**
     * Finds a SKU by its unique code.
     *
     * @param skuCode the SKU code
     * @return optional containing the SKU if found
     */
    @Query("SELECT s FROM Sku s WHERE s.skuCode = :skuCode AND s.deletedAt IS NULL")
    Optional<Sku> findBySkuCode(@Param("skuCode") String skuCode);

    /**
     * Finds SKUs by partial SKU code match.
     *
     * @param skuCodePattern the SKU code pattern
     * @return list of matching SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.skuCode LIKE CONCAT('%', :skuCodePattern, '%') AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findBySkuCodeContaining(@Param("skuCodePattern") String skuCodePattern);

    /**
     * Checks if a SKU code exists.
     *
     * @param skuCode   the SKU code
     * @param excludeId the SKU ID to exclude from the check
     * @return true if SKU code exists
     */
    @Query(
            "SELECT COUNT(s) > 0 FROM Sku s WHERE s.skuCode = :skuCode AND s.id != :excludeId AND s.deletedAt IS NULL")
    boolean existsBySkuCodeExcludingId(
            @Param("skuCode") String skuCode, @Param("excludeId") Long excludeId);

    // ===== INVENTORY QUERIES =====

    /**
     * Finds SKUs with low stock (stock quantity <= reorder point).
     *
     * @return list of SKUs with low stock
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity <= s.reorderPoint AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.stockQuantity ASC, s.skuCode")
    List<Sku> findLowStockSkus();

    /**
     * Finds SKUs with low stock with pagination.
     *
     * @param pageable pagination information
     * @return page of SKUs with low stock
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity <= s.reorderPoint AND s.isActive = true AND s.deletedAt IS NULL")
    Page<Sku> findLowStockSkus(Pageable pageable);

    /**
     * Finds SKUs that are out of stock.
     *
     * @return list of out of stock SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity = 0 AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findOutOfStockSkus();

    /**
     * Finds SKUs that are out of stock with pagination.
     *
     * @param pageable pagination information
     * @return page of out of stock SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity = 0 AND s.isActive = true AND s.deletedAt IS NULL")
    Page<Sku> findOutOfStockSkus(Pageable pageable);

    /**
     * Finds SKUs with available stock (stock quantity > reserved quantity).
     *
     * @return list of SKUs with available stock
     */
    @Query(
            "SELECT s FROM Sku s WHERE (s.stockQuantity - s.reservedQuantity) > 0 AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findSkusWithAvailableStock();

    /**
     * Finds SKUs with available stock with pagination.
     *
     * @param pageable pagination information
     * @return page of SKUs with available stock
     */
    @Query(
            "SELECT s FROM Sku s WHERE (s.stockQuantity - s.reservedQuantity) > 0 AND s.isActive = true AND s.deletedAt IS NULL")
    Page<Sku> findSkusWithAvailableStock(Pageable pageable);

    /**
     * Finds SKUs with reserved stock.
     *
     * @return list of SKUs with reserved stock
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.reservedQuantity > 0 AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.reservedQuantity DESC, s.skuCode")
    List<Sku> findSkusWithReservedStock();

    /**
     * Finds SKUs by stock quantity range.
     *
     * @param minStock minimum stock quantity
     * @param maxStock maximum stock quantity
     * @return list of SKUs in the stock range
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity BETWEEN :minStock AND :maxStock AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.stockQuantity DESC, s.skuCode")
    List<Sku> findByStockQuantityRange(
            @Param("minStock") Integer minStock, @Param("maxStock") Integer maxStock);

    // ===== PRICE QUERIES =====

    /**
     * Finds SKUs by price range.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of SKUs in the price range
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.price ASC, s.skuCode")
    List<Sku> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Finds SKUs by price range with pagination.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of SKUs in the price range
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true AND s.deletedAt IS NULL")
    Page<Sku> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Finds the minimum and maximum prices.
     *
     * @return array containing [minPrice, maxPrice]
     */
    @Query(
            "SELECT MIN(s.price), MAX(s.price) FROM Sku s WHERE s.isActive = true AND s.deletedAt IS NULL")
    Object[] findPriceRange();

    // ===== SEARCH AND FILTER QUERIES =====

    /**
     * Searches SKUs by variant name.
     *
     * @param variantName the variant name search term
     * @return list of matching SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE LOWER(s.variantName) LIKE LOWER(CONCAT('%', :variantName, '%')) AND s.deletedAt IS NULL ORDER BY s.variantName, s.skuCode")
    List<Sku> findByVariantNameContainingIgnoreCase(@Param("variantName") String variantName);

    /**
     * Finds SKUs by size.
     *
     * @param size the size
     * @return list of SKUs with the specified size
     */
    @Query(
            "SELECT s FROM Sku s WHERE LOWER(s.size) = LOWER(:size) AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findBySizeIgnoreCase(@Param("size") String size);

    /**
     * Finds SKUs by color.
     *
     * @param color the color
     * @return list of SKUs with the specified color
     */
    @Query(
            "SELECT s FROM Sku s WHERE LOWER(s.color) = LOWER(:color) AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findByColorIgnoreCase(@Param("color") String color);

    /**
     * Finds SKUs by location.
     *
     * @param location the location
     * @return list of SKUs at the specified location
     */
    @Query(
            "SELECT s FROM Sku s WHERE LOWER(s.location) = LOWER(:location) AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findByLocationIgnoreCase(@Param("location") String location);

    /**
     * Finds SKUs by barcode.
     *
     * @param barcode the barcode
     * @return optional containing the SKU if found
     */
    @Query("SELECT s FROM Sku s WHERE s.barcode = :barcode AND s.deletedAt IS NULL")
    Optional<Sku> findByBarcode(@Param("barcode") String barcode);

    /**
     * Finds SKUs by active status.
     *
     * @param isActive the active status
     * @return list of SKUs with the specified status
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.isActive = :isActive AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findByIsActive(@Param("isActive") Boolean isActive);

    /**
     * Finds SKUs by active status with pagination.
     *
     * @param isActive the active status
     * @param pageable pagination information
     * @return page of SKUs with the specified status
     */
    @Query("SELECT s FROM Sku s WHERE s.isActive = :isActive AND s.deletedAt IS NULL")
    Page<Sku> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    // ===== BUSINESS LOGIC QUERIES =====

    /**
     * Finds all distinct sizes.
     *
     * @return list of distinct sizes
     */
    @Query(
            "SELECT DISTINCT s.size FROM Sku s WHERE s.size IS NOT NULL AND s.deletedAt IS NULL ORDER BY s.size")
    List<String> findAllDistinctSizes();

    /**
     * Finds all distinct colors.
     *
     * @return list of distinct colors
     */
    @Query(
            "SELECT DISTINCT s.color FROM Sku s WHERE s.color IS NOT NULL AND s.deletedAt IS NULL ORDER BY s.color")
    List<String> findAllDistinctColors();

    /**
     * Finds all distinct locations.
     *
     * @return list of distinct locations
     */
    @Query(
            "SELECT DISTINCT s.location FROM Sku s WHERE s.location IS NOT NULL AND s.deletedAt IS NULL ORDER BY s.location")
    List<String> findAllDistinctLocations();

    /**
     * Counts SKUs by product.
     *
     * @param productId the product ID
     * @return number of SKUs for the product
     */
    @Query("SELECT COUNT(s) FROM Sku s WHERE s.product.id = :productId AND s.deletedAt IS NULL")
    long countByProductId(@Param("productId") Long productId);

    /**
     * Counts active SKUs by product.
     *
     * @param productId the product ID
     * @return number of active SKUs for the product
     */
    @Query(
            "SELECT COUNT(s) FROM Sku s WHERE s.product.id = :productId AND s.isActive = true AND s.deletedAt IS NULL")
    long countActiveByProductId(@Param("productId") Long productId);

    /**
     * Calculates total stock value.
     *
     * @return total stock value
     */
    @Query(
            "SELECT COALESCE(SUM(s.price * s.stockQuantity), 0) FROM Sku s WHERE s.isActive = true AND s.deletedAt IS NULL")
    BigDecimal calculateTotalStockValue();

    /**
     * Calculates total stock value by product.
     *
     * @param productId the product ID
     * @return total stock value for the product
     */
    @Query(
            "SELECT COALESCE(SUM(s.price * s.stockQuantity), 0) FROM Sku s WHERE s.product.id = :productId AND s.isActive = true AND s.deletedAt IS NULL")
    BigDecimal calculateStockValueByProductId(@Param("productId") Long productId);

    /**
     * Finds SKUs that need reordering (stock <= reorder point and reorder quantity > 0).
     *
     * @return list of SKUs that need reordering
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.stockQuantity <= s.reorderPoint AND s.reorderQuantity > 0 AND s.isActive = true AND s.deletedAt IS NULL ORDER BY s.stockQuantity ASC, s.skuCode")
    List<Sku> findSkusNeedingReorder();

    /**
     * Advanced search with multiple criteria.
     *
     * @param searchTerm search term for SKU code/variant name
     * @param productId  product filter (null to ignore)
     * @param size       size filter (null to ignore)
     * @param color      color filter (null to ignore)
     * @param location   location filter (null to ignore)
     * @param minPrice   minimum price filter (null to ignore)
     * @param maxPrice   maximum price filter (null to ignore)
     * @param minStock   minimum stock filter (null to ignore)
     * @param maxStock   maximum stock filter (null to ignore)
     * @param isActive   active status filter (null to ignore)
     * @param pageable   pagination information
     * @return page of matching SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE "
                    + "(:searchTerm IS NULL OR LOWER(s.skuCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.variantName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND "
                    + "(:productId IS NULL OR s.product.id = :productId) AND "
                    + "(:size IS NULL OR LOWER(s.size) = LOWER(:size)) AND "
                    + "(:color IS NULL OR LOWER(s.color) = LOWER(:color)) AND "
                    + "(:location IS NULL OR LOWER(s.location) = LOWER(:location)) AND "
                    + "(:minPrice IS NULL OR s.price >= :minPrice) AND "
                    + "(:maxPrice IS NULL OR s.price <= :maxPrice) AND "
                    + "(:minStock IS NULL OR s.stockQuantity >= :minStock) AND "
                    + "(:maxStock IS NULL OR s.stockQuantity <= :maxStock) AND "
                    + "(:isActive IS NULL OR s.isActive = :isActive) AND "
                    + "s.deletedAt IS NULL")
    Page<Sku> findWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("productId") Long productId,
            @Param("size") String size,
            @Param("color") String color,
            @Param("location") String location,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Finds SKUs with stock movements (having inventory transactions).
     *
     * @return list of SKUs with inventory transactions
     */
    @Query(
            "SELECT DISTINCT s FROM Sku s JOIN s.inventoryTransactions t WHERE s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findSkusWithStockMovements();

    /**
     * Finds SKUs without stock movements (no inventory transactions).
     *
     * @return list of SKUs without inventory transactions
     */
    @Query(
            "SELECT s FROM Sku s WHERE s.id NOT IN (SELECT DISTINCT t.sku.id FROM InventoryTransaction t) AND s.deletedAt IS NULL ORDER BY s.skuCode")
    List<Sku> findSkusWithoutStockMovements();

    /**
     * Extended search with multiple filter criteria.
     *
     * @param skuCode     filter by SKU code (null to ignore)
     * @param productId   filter by product ID (null to ignore)
     * @param variantName filter by variant name (null to ignore)
     * @param minPrice    filter by minimum price (null to ignore)
     * @param maxPrice    filter by maximum price (null to ignore)
     * @param isActive    filter by active status (null to ignore)
     * @param isLowStock  filter by low stock status (null to ignore)
     * @param pageable    pagination information
     * @return page of matching SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE "
                    + "(:skuCode IS NULL OR LOWER(s.skuCode) LIKE LOWER(CONCAT('%', :skuCode, '%'))) AND "
                    + "(:productId IS NULL OR s.product.id = :productId) AND "
                    + "(:variantName IS NULL OR LOWER(s.variantName) LIKE LOWER(CONCAT('%', :variantName, '%'))) AND "
                    + "(:minPrice IS NULL OR s.price >= :minPrice) AND "
                    + "(:maxPrice IS NULL OR s.price <= :maxPrice) AND "
                    + "(:isActive IS NULL OR s.isActive = :isActive) AND "
                    + "(:isLowStock IS NULL OR (:isLowStock = true AND s.stockQuantity <= s.reorderPoint) OR (:isLowStock = false AND s.stockQuantity > s.reorderPoint)) AND "
                    + "s.deletedAt IS NULL")
    Page<Sku> findWithExtendedFilters(
            @Param("skuCode") String skuCode,
            @Param("productId") Long productId,
            @Param("variantName") String variantName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isActive") Boolean isActive,
            @Param("isLowStock") Boolean isLowStock,
            Pageable pageable);

    /**
     * Searches SKUs by code or variant name.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return page of matching SKUs
     */
    @Query(
            "SELECT s FROM Sku s WHERE "
                    + "(LOWER(s.skuCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.variantName) LIKE LOWER(CONCAT('%', :query, '%'))) AND "
                    + "s.deletedAt IS NULL")
    Page<Sku> findBySkuCodeOrVariantNameContainingIgnoreCase(
            @Param("query") String query, Pageable pageable);
}
