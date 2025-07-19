package com.inventorymanagement.product.repository;

import com.inventorymanagement.common.repository.BaseRepository;
import com.inventorymanagement.product.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Product entity. Provides product-specific query methods and search functionality.
 */
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {

    // ===== CATEGORY-BASED QUERIES =====

    /**
     * Finds all products in a specific category.
     *
     * @param categoryId the category ID
     * @return list of products in the category
     */
    @Query(
            "SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Finds all products in a specific category with pagination.
     *
     * @param categoryId the category ID
     * @param pageable   pagination information
     * @return page of products in the category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Finds all products in categories that match a path pattern (hierarchical).
     *
     * @param categoryPath the category path pattern
     * @return list of products in matching categories
     */
    @Query(
            "SELECT p FROM Product p JOIN p.category c WHERE c.path LIKE CONCAT(:categoryPath, '%') AND p.deletedAt IS NULL AND c.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByCategoryPathStartingWith(@Param("categoryPath") String categoryPath);

    /**
     * Finds all products in multiple categories.
     *
     * @param categoryIds list of category IDs
     * @return list of products in the specified categories
     */
    @Query(
            "SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);

    /**
     * Finds all products in multiple categories with pagination.
     *
     * @param categoryIds list of category IDs
     * @param pageable    pagination information
     * @return page of products in the specified categories
     */
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.deletedAt IS NULL")
    Page<Product> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // ===== SEARCH AND FILTER QUERIES =====

    /**
     * Searches products by name (case-insensitive partial match).
     *
     * @param name the search term
     * @return list of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Searches products by name with pagination.
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return page of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.deletedAt IS NULL")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Searches products by name or description.
     *
     * @param searchTerm the search term
     * @return list of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Searches products by name or description with pagination.
     *
     * @param searchTerm the search term
     * @param pageable   pagination information
     * @return page of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.deletedAt IS NULL")
    Page<Product> searchByNameOrDescription(
            @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Finds products by brand.
     *
     * @param brand the brand name
     * @return list of products with the specified brand
     */
    @Query(
            "SELECT p FROM Product p WHERE LOWER(p.brand) = LOWER(:brand) AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByBrandIgnoreCase(@Param("brand") String brand);

    /**
     * Finds products by brand with pagination.
     *
     * @param brand    the brand name
     * @param pageable pagination information
     * @return page of products with the specified brand
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.brand) = LOWER(:brand) AND p.deletedAt IS NULL")
    Page<Product> findByBrandIgnoreCase(@Param("brand") String brand, Pageable pageable);

    /**
     * Finds products by manufacturer.
     *
     * @param manufacturer the manufacturer name
     * @return list of products with the specified manufacturer
     */
    @Query(
            "SELECT p FROM Product p WHERE LOWER(p.manufacturer) = LOWER(:manufacturer) AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByManufacturerIgnoreCase(@Param("manufacturer") String manufacturer);

    /**
     * Finds products by manufacturer with pagination.
     *
     * @param manufacturer the manufacturer name
     * @param pageable     pagination information
     * @return page of products with the specified manufacturer
     */
    @Query(
            "SELECT p FROM Product p WHERE LOWER(p.manufacturer) = LOWER(:manufacturer) AND p.deletedAt IS NULL")
    Page<Product> findByManufacturerIgnoreCase(
            @Param("manufacturer") String manufacturer, Pageable pageable);

    /**
     * Finds products by active status.
     *
     * @param isActive the active status
     * @return list of products with the specified status
     */
    @Query(
            "SELECT p FROM Product p WHERE p.isActive = :isActive AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findByIsActive(@Param("isActive") Boolean isActive);

    /**
     * Finds products by active status with pagination.
     *
     * @param isActive the active status
     * @param pageable pagination information
     * @return page of products with the specified status
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = :isActive AND p.deletedAt IS NULL")
    Page<Product> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    // ===== INVENTORY-BASED QUERIES =====

    /**
     * Finds products that have SKUs.
     *
     * @return list of products with SKUs
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.deletedAt IS NULL AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithSkus();

    /**
     * Finds products that have no SKUs.
     *
     * @return list of products without SKUs
     */
    @Query(
            "SELECT p FROM Product p WHERE p.id NOT IN (SELECT DISTINCT s.product.id FROM Sku s WHERE s.deletedAt IS NULL) AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithoutSkus();

    /**
     * Finds products that have active SKUs.
     *
     * @return list of products with active SKUs
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithActiveSkus();

    /**
     * Finds products that are low on stock (any SKU below reorder point).
     *
     * @return list of products with low stock
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.stockQuantity <= s.reorderPoint AND s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithLowStock();

    /**
     * Finds products that are low on stock (any SKU below reorder point) with pagination.
     *
     * @param pageable pagination information
     * @return page of products with low stock
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.stockQuantity <= s.reorderPoint AND s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL")
    Page<Product> findLowStockProducts(Pageable pageable);

    /**
     * Finds products that are out of stock (any SKU with zero stock).
     *
     * @return list of products with out of stock SKUs
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.stockQuantity = 0 AND s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithOutOfStockSkus();

    /**
     * Finds products with SKUs in a price range.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of products with SKUs in the price range
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL ORDER BY p.name")
    List<Product> findProductsWithSkuPriceRange(
            @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Finds products with SKUs in a price range with pagination.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of products with SKUs in the price range
     */
    @Query(
            "SELECT DISTINCT p FROM Product p JOIN p.skus s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true AND s.deletedAt IS NULL AND p.deletedAt IS NULL")
    Page<Product> findProductsWithSkuPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // ===== BUSINESS LOGIC QUERIES =====

    /**
     * Finds products with their SKU count.
     *
     * @return list of products with their SKU count
     */
    @Query(
            "SELECT p, COUNT(s) as skuCount FROM Product p LEFT JOIN p.skus s WHERE p.deletedAt IS NULL AND (s.deletedAt IS NULL OR s IS NULL) GROUP BY p ORDER BY p.name")
    List<Object[]> findProductsWithSkuCount();

    /**
     * Finds products with their active SKU count.
     *
     * @return list of products with their active SKU count
     */
    @Query(
            "SELECT p, COUNT(s) as activeSkuCount FROM Product p LEFT JOIN p.skus s WHERE p.deletedAt IS NULL AND (s.isActive = true AND s.deletedAt IS NULL OR s IS NULL) GROUP BY p ORDER BY p.name")
    List<Object[]> findProductsWithActiveSkuCount();

    /**
     * Finds products with their total stock quantity.
     *
     * @return list of products with their total stock quantity
     */
    @Query(
            "SELECT p, COALESCE(SUM(s.stockQuantity), 0) as totalStock FROM Product p LEFT JOIN p.skus s WHERE p.deletedAt IS NULL AND (s.isActive = true AND s.deletedAt IS NULL OR s IS NULL) GROUP BY p ORDER BY p.name")
    List<Object[]> findProductsWithTotalStock();

    /**
     * Counts products in a specific category.
     *
     * @param categoryId the category ID
     * @return number of products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Counts active products in a specific category.
     *
     * @param categoryId the category ID
     * @return number of active products
     */
    @Query(
            "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true AND p.deletedAt IS NULL")
    long countActiveByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Finds products by exact name (case-sensitive).
     *
     * @param name the product name
     * @return optional containing the product if found
     */
    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.deletedAt IS NULL")
    Optional<Product> findByName(@Param("name") String name);

    /**
     * Finds products by exact name and category.
     *
     * @param name       the product name
     * @param categoryId the category ID
     * @return optional containing the product if found
     */
    @Query(
            "SELECT p FROM Product p WHERE p.name = :name AND p.category.id = :categoryId AND p.deletedAt IS NULL")
    Optional<Product> findByNameAndCategoryId(
            @Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Checks if a product name exists in the same category.
     *
     * @param name       the product name
     * @param categoryId the category ID
     * @param excludeId  the product ID to exclude from the check
     * @return true if name exists in the category
     */
    @Query(
            "SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.category.id = :categoryId AND p.id != :excludeId AND p.deletedAt IS NULL")
    boolean existsByNameAndCategoryIdExcludingId(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("excludeId") Long excludeId);

    /**
     * Finds all distinct brands.
     *
     * @return list of distinct brands
     */
    @Query(
            "SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.deletedAt IS NULL ORDER BY p.brand")
    List<String> findAllDistinctBrands();

    /**
     * Finds all distinct manufacturers.
     *
     * @return list of distinct manufacturers
     */
    @Query(
            "SELECT DISTINCT p.manufacturer FROM Product p WHERE p.manufacturer IS NOT NULL AND p.deletedAt IS NULL ORDER BY p.manufacturer")
    List<String> findAllDistinctManufacturers();

    /**
     * Advanced search with multiple criteria.
     *
     * @param searchTerm   search term for name/description
     * @param categoryId   category filter (null to ignore)
     * @param brand        brand filter (null to ignore)
     * @param manufacturer manufacturer filter (null to ignore)
     * @param isActive     active status filter (null to ignore)
     * @param pageable     pagination information
     * @return page of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE "
                    + "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND "
                    + "(:categoryId IS NULL OR p.category.id = :categoryId) AND "
                    + "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND "
                    + "(:manufacturer IS NULL OR LOWER(p.manufacturer) = LOWER(:manufacturer)) AND "
                    + "(:isActive IS NULL OR p.isActive = :isActive) AND "
                    + "p.deletedAt IS NULL")
    Page<Product> findWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("manufacturer") String manufacturer,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Extended search with price range criteria.
     *
     * @param name         search term for name
     * @param categoryId   category filter (null to ignore)
     * @param brand        brand filter (null to ignore)
     * @param manufacturer manufacturer filter (null to ignore)
     * @param minPrice     minimum price filter (null to ignore)
     * @param maxPrice     maximum price filter (null to ignore)
     * @param isActive     active status filter (null to ignore)
     * @param pageable     pagination information
     * @return page of matching products
     */
    @Query(
            "SELECT DISTINCT p FROM Product p LEFT JOIN p.skus s WHERE "
                    + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                    + "(:categoryId IS NULL OR p.category.id = :categoryId) AND "
                    + "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND "
                    + "(:manufacturer IS NULL OR LOWER(p.manufacturer) = LOWER(:manufacturer)) AND "
                    + "(:minPrice IS NULL OR s.price >= :minPrice) AND "
                    + "(:maxPrice IS NULL OR s.price <= :maxPrice) AND "
                    + "(:isActive IS NULL OR p.isActive = :isActive) AND "
                    + "p.deletedAt IS NULL")
    Page<Product> findWithExtendedFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("manufacturer") String manufacturer,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Searches products by name or description with pagination.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return page of matching products
     */
    @Query(
            "SELECT p FROM Product p WHERE "
                    + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND "
                    + "p.deletedAt IS NULL")
    Page<Product> findByNameOrDescriptionContainingIgnoreCase(
            @Param("query") String query, Pageable pageable);
}
