package com.inventorymanagement.inventory.repository;

import com.inventorymanagement.inventory.model.InventoryTransaction;
import com.inventorymanagement.inventory.model.InventoryTransaction.TransactionType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for InventoryTransaction entity. Provides transaction audit trail and inventory movement tracking functionality.
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    // ===== SKU-BASED QUERIES =====

    /**
     * Finds all transactions for a specific SKU.
     *
     * @param skuId the SKU ID
     * @return list of transactions for the SKU
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findBySkuId(@Param("skuId") Long skuId);

    /**
     * Finds all transactions for a specific SKU with pagination.
     *
     * @param skuId    the SKU ID
     * @param pageable pagination information
     * @return page of transactions for the SKU
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findBySkuId(@Param("skuId") Long skuId, Pageable pageable);

    /**
     * Finds all transactions for multiple SKUs.
     *
     * @param skuIds list of SKU IDs
     * @return list of transactions for the specified SKUs
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.sku.id IN :skuIds ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findBySkuIdIn(@Param("skuIds") List<Long> skuIds);

    /**
     * Finds recent transactions for a specific SKU.
     *
     * @param skuId the SKU ID
     * @param limit maximum number of transactions to return
     * @return list of recent transactions
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId ORDER BY t.createdAt DESC LIMIT :limit")
    List<InventoryTransaction> findRecentBySkuId(
            @Param("skuId") Long skuId, @Param("limit") int limit);

    // ===== TRANSACTION TYPE QUERIES =====

    /**
     * Finds transactions by type.
     *
     * @param transactionType the transaction type
     * @return list of transactions with the specified type
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.transactionType = :transactionType ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByTransactionType(
            @Param("transactionType") TransactionType transactionType);

    /**
     * Finds transactions by type with pagination.
     *
     * @param transactionType the transaction type
     * @param pageable        pagination information
     * @return page of transactions with the specified type
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.transactionType = :transactionType ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByTransactionType(
            @Param("transactionType") TransactionType transactionType, Pageable pageable);

    /**
     * Finds transactions by SKU and type.
     *
     * @param skuId           the SKU ID
     * @param transactionType the transaction type
     * @return list of transactions for the SKU with the specified type
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.transactionType = :transactionType ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findBySkuIdAndTransactionType(
            @Param("skuId") Long skuId, @Param("transactionType") TransactionType transactionType);

    /**
     * Finds stock increase transactions (IN and positive ADJUSTMENT).
     *
     * @return list of stock increase transactions
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE (t.transactionType = 'IN' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity > 0)) ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findStockIncreaseTransactions();

    /**
     * Finds stock decrease transactions (OUT and negative ADJUSTMENT).
     *
     * @return list of stock decrease transactions
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE (t.transactionType = 'OUT' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity < 0)) ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findStockDecreaseTransactions();

    // ===== DATE-BASED QUERIES =====

    /**
     * Finds transactions within a date range.
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of transactions within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Finds transactions within a date range with pagination.
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @param pageable  pagination information
     * @return page of transactions within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Finds transactions within a date range with pagination (alias for findByCreatedAtBetween).
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @param pageable  pagination information
     * @return page of transactions within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Finds transactions after a specific date.
     *
     * @param date the date to search from
     * @return list of transactions after the date
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.createdAt > :date ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByCreatedAtAfter(@Param("date") LocalDateTime date);

    /**
     * Finds transactions for a SKU within a date range.
     *
     * @param skuId     the SKU ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of transactions for the SKU within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findBySkuIdAndCreatedAtBetween(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds transactions for a SKU within a date range with pagination.
     *
     * @param skuId     the SKU ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @param pageable  pagination information
     * @return page of transactions for the SKU within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findBySkuIdAndCreatedAtBetween(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Finds transactions for a SKU within a date range (alias for findBySkuIdAndCreatedAtBetween).
     *
     * @param skuId     the SKU ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @param pageable  pagination information
     * @return page of transactions for the SKU within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findBySkuIdAndDateRange(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Finds transactions for a SKU within a date range (alias for findBySkuIdAndCreatedAtBetween).
     *
     * @param skuId     the SKU ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of transactions for the SKU within the date range
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findBySkuIdAndDateRange(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ===== REFERENCE-BASED QUERIES =====

    /**
     * Finds transactions by reference ID.
     *
     * @param referenceId the reference ID
     * @return list of transactions with the specified reference ID
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.referenceId = :referenceId ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByReferenceId(@Param("referenceId") String referenceId);

    /**
     * Finds transactions by reference ID with pagination.
     *
     * @param referenceId the reference ID
     * @param pageable    pagination information
     * @return page of transactions with the specified reference ID
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.referenceId = :referenceId ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByReferenceId(
            @Param("referenceId") String referenceId, Pageable pageable);

    /**
     * Finds transactions by reference type.
     *
     * @param referenceType the reference type
     * @return list of transactions with the specified reference type
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.referenceType = :referenceType ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByReferenceType(@Param("referenceType") String referenceType);

    /**
     * Finds transactions by reference ID and type.
     *
     * @param referenceId   the reference ID
     * @param referenceType the reference type
     * @return list of transactions with the specified reference ID and type
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.referenceId = :referenceId AND t.referenceType = :referenceType ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByReferenceIdAndReferenceType(
            @Param("referenceId") String referenceId, @Param("referenceType") String referenceType);

    /**
     * Finds transactions performed by a specific user.
     *
     * @param performedBy the user who performed the transaction
     * @return list of transactions performed by the user
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.performedBy = :performedBy ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByPerformedBy(@Param("performedBy") String performedBy);

    /**
     * Finds transactions performed by a specific user with pagination.
     *
     * @param performedBy the user who performed the transaction
     * @param pageable    pagination information
     * @return page of transactions performed by the user
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.performedBy = :performedBy ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findByPerformedBy(
            @Param("performedBy") String performedBy, Pageable pageable);

    // ===== AGGREGATION QUERIES =====

    /**
     * Calculates total quantity changes for a SKU.
     *
     * @param skuId the SKU ID
     * @return total quantity change (positive for net increase, negative for net decrease)
     */
    @Query(
            "SELECT COALESCE(SUM(CASE WHEN t.transactionType = 'IN' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity > 0) THEN t.quantity WHEN t.transactionType = 'OUT' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity < 0) THEN -ABS(t.quantity) ELSE 0 END), 0) FROM InventoryTransaction t WHERE t.sku.id = :skuId")
    Long calculateTotalQuantityChangeBySkuId(@Param("skuId") Long skuId);

    /**
     * Calculates total quantity changes for a SKU within a date range.
     *
     * @param skuId     the SKU ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return total quantity change within the date range
     */
    @Query(
            "SELECT COALESCE(SUM(CASE WHEN t.transactionType = 'IN' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity > 0) THEN t.quantity WHEN t.transactionType = 'OUT' OR (t.transactionType = 'ADJUSTMENT' AND t.quantity < 0) THEN -ABS(t.quantity) ELSE 0 END), 0) FROM InventoryTransaction t WHERE t.sku.id = :skuId AND t.createdAt BETWEEN :startDate AND :endDate")
    Long calculateQuantityChangeBySkuIdAndDateRange(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Counts transactions by SKU.
     *
     * @param skuId the SKU ID
     * @return number of transactions for the SKU
     */
    @Query("SELECT COUNT(t) FROM InventoryTransaction t WHERE t.sku.id = :skuId")
    long countBySkuId(@Param("skuId") Long skuId);

    /**
     * Counts transactions by type.
     *
     * @param transactionType the transaction type
     * @return number of transactions with the specified type
     */
    @Query("SELECT COUNT(t) FROM InventoryTransaction t WHERE t.transactionType = :transactionType")
    long countByTransactionType(@Param("transactionType") TransactionType transactionType);

    /**
     * Finds transaction summary by SKU (grouped by transaction type).
     *
     * @param skuId the SKU ID
     * @return list of transaction type and count pairs
     */
    @Query(
            "SELECT t.transactionType, COUNT(t), COALESCE(SUM(ABS(t.quantity)), 0) FROM InventoryTransaction t WHERE t.sku.id = :skuId GROUP BY t.transactionType ORDER BY t.transactionType")
    List<Object[]> findTransactionSummaryBySkuId(@Param("skuId") Long skuId);

    /**
     * Finds daily transaction summary within a date range.
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of date, transaction type, and count
     */
    @Query(
            "SELECT DATE(t.createdAt), t.transactionType, COUNT(t), COALESCE(SUM(ABS(t.quantity)), 0) FROM InventoryTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(t.createdAt), t.transactionType ORDER BY DATE(t.createdAt) DESC, t.transactionType")
    List<Object[]> findDailyTransactionSummary(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ===== BUSINESS LOGIC QUERIES =====

    /**
     * Finds all distinct reference types.
     *
     * @return list of distinct reference types
     */
    @Query(
            "SELECT DISTINCT t.referenceType FROM InventoryTransaction t WHERE t.referenceType IS NOT NULL ORDER BY t.referenceType")
    List<String> findAllDistinctReferenceTypes();

    /**
     * Finds all distinct users who performed transactions.
     *
     * @return list of distinct users
     */
    @Query(
            "SELECT DISTINCT t.performedBy FROM InventoryTransaction t WHERE t.performedBy IS NOT NULL ORDER BY t.performedBy")
    List<String> findAllDistinctPerformedBy();

    /**
     * Finds the most recent transaction for a SKU.
     *
     * @param skuId the SKU ID
     * @return the most recent transaction for the SKU
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE t.sku.id = :skuId ORDER BY t.createdAt DESC LIMIT 1")
    InventoryTransaction findMostRecentBySkuId(@Param("skuId") Long skuId);

    /**
     * Finds transactions with specific reason.
     *
     * @param reason the transaction reason
     * @return list of transactions with the specified reason
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE LOWER(t.reason) LIKE LOWER(CONCAT('%', :reason, '%')) ORDER BY t.createdAt DESC")
    List<InventoryTransaction> findByReasonContainingIgnoreCase(@Param("reason") String reason);

    /**
     * Advanced search with multiple criteria.
     *
     * @param skuId           SKU filter (null to ignore)
     * @param transactionType transaction type filter (null to ignore)
     * @param referenceId     reference ID filter (null to ignore)
     * @param referenceType   reference type filter (null to ignore)
     * @param performedBy     user filter (null to ignore)
     * @param startDate       start date filter (null to ignore)
     * @param endDate         end date filter (null to ignore)
     * @param pageable        pagination information
     * @return page of matching transactions
     */
    @Query(
            "SELECT t FROM InventoryTransaction t WHERE "
                    + "(:skuId IS NULL OR t.sku.id = :skuId) AND "
                    + "(:transactionType IS NULL OR t.transactionType = :transactionType) AND "
                    + "(:referenceId IS NULL OR t.referenceId = :referenceId) AND "
                    + "(:referenceType IS NULL OR t.referenceType = :referenceType) AND "
                    + "(:performedBy IS NULL OR t.performedBy = :performedBy) AND "
                    + "(:startDate IS NULL OR t.createdAt >= :startDate) AND "
                    + "(:endDate IS NULL OR t.createdAt <= :endDate) "
                    + "ORDER BY t.createdAt DESC")
    Page<InventoryTransaction> findWithFilters(
            @Param("skuId") Long skuId,
            @Param("transactionType") TransactionType transactionType,
            @Param("referenceId") String referenceId,
            @Param("referenceType") String referenceType,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
