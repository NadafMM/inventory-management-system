package com.inventorymanagement.inventory.service;

import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.inventory.model.InventoryTransaction;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.inventory.repository.InventoryTransactionRepository;
import com.inventorymanagement.inventory.repository.SkuRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service class for managing inventory transactions and operations. Provides comprehensive inventory tracking and audit trail functionality.
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryTransactionRepository transactionRepository;
    private final SkuRepository skuRepository;

    @Autowired
    public InventoryService(
            InventoryTransactionRepository transactionRepository, SkuRepository skuRepository) {
        this.transactionRepository = transactionRepository;
        this.skuRepository = skuRepository;
    }

    // ===== INVENTORY TRANSACTION OPERATIONS =====

    /**
     * Records a stock-in transaction.
     *
     * @param skuId       the SKU ID
     * @param quantity    the quantity received
     * @param referenceId the reference ID for the transaction
     * @param reason      the reason for the stock-in
     * @param performedBy who performed the transaction
     * @return the created transaction
     */
    public InventoryTransaction recordStockIn(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String reason,
            String performedBy) {
        logger.info("Recording stock-in transaction for SKU ID: {}, quantity: {}", skuId, quantity);

        validateTransactionParameters(quantity, "Stock-in");
        validatePerformerName(performedBy);
        validateReferenceId(referenceId);
        validateReason(reason);

        Sku sku = findSkuById(skuId);

        InventoryTransaction transaction =
                new InventoryTransaction(
                        sku,
                        InventoryTransaction.TransactionType.IN,
                        quantity,
                        referenceId,
                        "STOCK_IN",
                        reason,
                        performedBy);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        logger.info("Successfully recorded stock-in transaction with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * Records a stock-out transaction.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity shipped/sold
     * @param referenceId   the reference ID for the transaction
     * @param referenceType the reference type (e.g., "ORDER", "SHIPMENT")
     * @param reason        the reason for the stock-out
     * @param performedBy   who performed the transaction
     * @return the created transaction
     */
    public InventoryTransaction recordStockOut(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info("Recording stock-out transaction for SKU ID: {}, quantity: {}", skuId, quantity);

        validateTransactionParameters(quantity, "Stock-out");
        validatePerformerName(performedBy);
        validateReferenceId(referenceId);
        validateReason(reason);

        Sku sku = findSkuById(skuId);

        InventoryTransaction transaction =
                new InventoryTransaction(
                        sku,
                        InventoryTransaction.TransactionType.OUT,
                        quantity,
                        referenceId,
                        referenceType,
                        reason,
                        performedBy);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        logger.info(
                "Successfully recorded stock-out transaction with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * Records a stock adjustment transaction.
     *
     * @param skuId         the SKU ID
     * @param adjustment    the adjustment amount (positive or negative)
     * @param referenceType the reference type for the adjustment
     * @param reason        the reason for the adjustment
     * @param performedBy   who performed the adjustment
     * @return the created transaction
     */
    public InventoryTransaction recordStockAdjustment(
            @NotNull Long skuId,
            @NotNull Integer adjustment,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info(
                "Recording stock adjustment transaction for SKU ID: {}, adjustment: {}", skuId, adjustment);

        if (adjustment == 0) {
            throw new ValidationException("adjustment", "Stock adjustment cannot be zero");
        }

        Sku sku = findSkuById(skuId);

        InventoryTransaction transaction =
                new InventoryTransaction(
                        sku,
                        InventoryTransaction.TransactionType.ADJUSTMENT,
                        adjustment,
                        null,
                        referenceType,
                        reason,
                        performedBy);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        logger.info(
                "Successfully recorded stock adjustment transaction with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * Records a stock reservation transaction.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity reserved
     * @param referenceId   the reference ID for the reservation
     * @param referenceType the reference type (e.g., "ORDER", "ALLOCATION")
     * @param reason        the reason for the reservation
     * @param performedBy   who performed the reservation
     * @return the created transaction
     */
    public InventoryTransaction recordStockReservation(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info(
                "Recording stock reservation transaction for SKU ID: {}, quantity: {}", skuId, quantity);

        validateTransactionParameters(quantity, "Stock reservation");
        validatePerformerName(performedBy);
        validateReferenceId(referenceId);
        validateReason(reason);

        Sku sku = findSkuById(skuId);

        InventoryTransaction transaction =
                new InventoryTransaction(
                        sku,
                        InventoryTransaction.TransactionType.RESERVED,
                        quantity,
                        referenceId,
                        referenceType,
                        reason,
                        performedBy);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        logger.info(
                "Successfully recorded stock reservation transaction with ID: {}",
                savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * Records a stock release transaction.
     *
     * @param skuId         the SKU ID
     * @param quantity      the quantity released
     * @param referenceId   the reference ID for the release
     * @param referenceType the reference type
     * @param reason        the reason for the release
     * @param performedBy   who performed the release
     * @return the created transaction
     */
    public InventoryTransaction recordStockRelease(
            @NotNull Long skuId,
            @NotNull Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        logger.info(
                "Recording stock release transaction for SKU ID: {}, quantity: {}", skuId, quantity);

        validateTransactionParameters(quantity, "Stock release");
        validatePerformerName(performedBy);
        validateReferenceId(referenceId);
        validateReason(reason);

        Sku sku = findSkuById(skuId);

        InventoryTransaction transaction =
                new InventoryTransaction(
                        sku,
                        InventoryTransaction.TransactionType.RELEASED,
                        quantity,
                        referenceId,
                        referenceType,
                        reason,
                        performedBy);

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);

        logger.info(
                "Successfully recorded stock release transaction with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    // ===== TRANSACTION QUERY OPERATIONS =====

    /**
     * Retrieves all transactions for a specific SKU.
     *
     * @param skuId    the SKU ID
     * @param pageable pagination information
     * @return page of inventory transactions
     */
    public Page<InventoryTransaction> getTransactionsBySkuId(@NotNull Long skuId, Pageable pageable) {
        logger.debug("Retrieving transactions for SKU ID: {}", skuId);

        // Verify SKU exists
        findSkuById(skuId);

        return transactionRepository.findBySkuId(skuId, pageable);
    }

    /**
     * Retrieves all transactions for a specific SKU within a date range.
     *
     * @param skuId     the SKU ID
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination information
     * @return page of inventory transactions
     */
    public Page<InventoryTransaction> getTransactionsBySkuIdAndDateRange(
            @NotNull Long skuId,
            @NotNull LocalDateTime startDate,
            @NotNull LocalDateTime endDate,
            Pageable pageable) {
        logger.debug(
                "Retrieving transactions for SKU ID: {} between {} and {}", skuId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("dateRange", "Start date cannot be after end date");
        }

        // Verify SKU exists
        findSkuById(skuId);

        return transactionRepository.findBySkuIdAndCreatedAtBetween(
                skuId, startDate, endDate, pageable);
    }

    /**
     * Retrieves all transactions of a specific type.
     *
     * @param transactionType the transaction type
     * @param pageable        pagination information
     * @return page of inventory transactions
     */
    public Page<InventoryTransaction> getTransactionsByType(
            @NotNull InventoryTransaction.TransactionType transactionType, Pageable pageable) {
        logger.debug("Retrieving transactions of type: {}", transactionType);

        return transactionRepository.findByTransactionType(transactionType, pageable);
    }

    /**
     * Retrieves all transactions within a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination information
     * @return page of inventory transactions
     */
    public Page<InventoryTransaction> getTransactionsByDateRange(
            @NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate, Pageable pageable) {
        logger.debug("Retrieving transactions between {} and {}", startDate, endDate);

        if (startDate == null) {
            throw new ValidationException("startDate", "start date cannot be null");
        }
        if (endDate == null) {
            throw new ValidationException("endDate", "end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("dateRange", "Start date cannot be after end date");
        }

        return transactionRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Retrieves transactions by reference ID.
     *
     * @param referenceId the reference ID
     * @param pageable    pagination information
     * @return page of inventory transactions
     */
    public Page<InventoryTransaction> getTransactionsByReferenceId(
            @NotNull String referenceId, Pageable pageable) {
        logger.debug("Retrieving transactions for reference ID: {}", referenceId);

        if (referenceId == null) {
            throw new ValidationException("referenceId", "Reference ID cannot be null");
        }
        if (!StringUtils.hasText(referenceId)) {
            throw new ValidationException("referenceId", "Reference ID cannot be empty");
        }

        return transactionRepository.findByReferenceId(referenceId, pageable);
    }

    // ===== INVENTORY ANALYSIS OPERATIONS =====

    /**
     * Calculates the total stock movement for a SKU within a date range.
     *
     * @param skuId     the SKU ID
     * @param startDate the start date
     * @param endDate   the end date
     * @return stock movement summary
     */
    public StockMovementSummary getStockMovementSummary(
            @NotNull Long skuId, @NotNull LocalDateTime startDate, @NotNull LocalDateTime endDate) {
        logger.debug(
                "Calculating stock movement summary for SKU ID: {} between {} and {}",
                skuId,
                startDate,
                endDate);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("dateRange", "Start date cannot be after end date");
        }

        // Verify SKU exists
        findSkuById(skuId);

        List<InventoryTransaction> transactions =
                transactionRepository.findBySkuIdAndDateRange(skuId, startDate, endDate);

        long totalIn = 0;
        long totalOut = 0;
        long totalAdjustments = 0;
        long totalReserved = 0;
        long totalReleased = 0;

        for (InventoryTransaction transaction : transactions) {
            switch (transaction.getTransactionType()) {
                case IN:
                    totalIn += transaction.getQuantity();
                    break;
                case OUT:
                    totalOut += transaction.getQuantity();
                    break;
                case ADJUSTMENT:
                    totalAdjustments += transaction.getQuantity();
                    break;
                case RESERVED:
                    totalReserved += transaction.getQuantity();
                    break;
                case RELEASED:
                    totalReleased += transaction.getQuantity();
                    break;
            }
        }

        return new StockMovementSummary(
                totalIn, totalOut, totalAdjustments, totalReserved, totalReleased);
    }

    /**
     * Gets the current stock level for a SKU.
     *
     * @param skuId the SKU ID
     * @return current stock information
     */
    public CurrentStockInfo getCurrentStockInfo(@NotNull Long skuId) {
        logger.debug("Getting current stock info for SKU ID: {}", skuId);

        Sku sku = findSkuById(skuId);

        return new CurrentStockInfo(
                sku.getStockQuantity(),
                sku.getReservedQuantity(),
                sku.getAvailableQuantity(),
                sku.getReorderPoint(),
                sku.getReorderQuantity(),
                sku.isLowOnStock(),
                sku.isOutOfStock());
    }

    // ===== PRIVATE HELPER METHODS =====

    private Sku findSkuById(Long id) {
        return skuRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("SKU", id));
    }

    private void validateTransactionParameters(Integer quantity, String operation) {
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("quantity", operation + " quantity must be positive");
        }
    }

    private void validatePerformerName(String performedBy) {
        if (performedBy == null) {
            throw new ValidationException("performedBy", "performer cannot be null");
        }
        if (performedBy.trim().isEmpty()) {
            throw new ValidationException("performedBy", "performer cannot be empty");
        }
    }

    private void validateReferenceId(String referenceId) {
        if (referenceId != null && referenceId.length() > 255) {
            throw new ValidationException("referenceId", "reference ID too long");
        }
    }

    private void validateReason(String reason) {
        if (reason != null && reason.length() > 1000) {
            throw new ValidationException("reason", "reason too long");
        }
    }

    // ===== INNER CLASSES =====

    /**
     * Summary of stock movements for a SKU within a date range.
     */
    public static class StockMovementSummary {

        private final long totalIn;
        private final long totalOut;
        private final long totalAdjustments;
        private final long totalReserved;
        private final long totalReleased;
        private final long netMovement;

        public StockMovementSummary(
                long totalIn,
                long totalOut,
                long totalAdjustments,
                long totalReserved,
                long totalReleased) {
            this.totalIn = totalIn;
            this.totalOut = totalOut;
            this.totalAdjustments = totalAdjustments;
            this.totalReserved = totalReserved;
            this.totalReleased = totalReleased;
            this.netMovement = totalIn - totalOut + totalAdjustments;
        }

        public long getTotalIn() {
            return totalIn;
        }

        public long getTotalOut() {
            return totalOut;
        }

        public long getTotalAdjustments() {
            return totalAdjustments;
        }

        public long getTotalReserved() {
            return totalReserved;
        }

        public long getTotalReleased() {
            return totalReleased;
        }

        public long getNetMovement() {
            return netMovement;
        }
    }

    /**
     * Current stock information for a SKU.
     */
    public static class CurrentStockInfo {

        private final int stockQuantity;
        private final int reservedQuantity;
        private final int availableQuantity;
        private final int reorderPoint;
        private final int reorderQuantity;
        private final boolean isLowOnStock;
        private final boolean isOutOfStock;

        public CurrentStockInfo(
                int stockQuantity,
                int reservedQuantity,
                int availableQuantity,
                int reorderPoint,
                int reorderQuantity,
                boolean isLowOnStock,
                boolean isOutOfStock) {
            this.stockQuantity = stockQuantity;
            this.reservedQuantity = reservedQuantity;
            this.availableQuantity = availableQuantity;
            this.reorderPoint = reorderPoint;
            this.reorderQuantity = reorderQuantity;
            this.isLowOnStock = isLowOnStock;
            this.isOutOfStock = isOutOfStock;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }

        public int getReservedQuantity() {
            return reservedQuantity;
        }

        public int getAvailableQuantity() {
            return availableQuantity;
        }

        public int getReorderPoint() {
            return reorderPoint;
        }

        public int getReorderQuantity() {
            return reorderQuantity;
        }

        public boolean isLowOnStock() {
            return isLowOnStock;
        }

        public boolean isOutOfStock() {
            return isOutOfStock;
        }
    }
}
