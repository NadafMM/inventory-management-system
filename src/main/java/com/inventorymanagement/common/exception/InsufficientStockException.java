package com.inventorymanagement.common.exception;

/**
 * Exception thrown when there is insufficient stock to fulfill a request. This is a specialized business exception for inventory operations.
 */
public class InsufficientStockException extends BusinessException {

    private final String skuCode;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    /**
     * Constructs a new insufficient stock exception with the specified details.
     *
     * @param skuCode           the SKU code that has insufficient stock
     * @param requestedQuantity the quantity that was requested
     * @param availableQuantity the quantity that is available
     */
    public InsufficientStockException(
            String skuCode, Integer requestedQuantity, Integer availableQuantity) {
        super(
                "INSUFFICIENT_STOCK",
                String.format(
                        "Insufficient stock for SKU %s: requested %d, available %d",
                        skuCode, requestedQuantity, availableQuantity));
        this.skuCode = skuCode;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Constructs a new insufficient stock exception with the specified message.
     *
     * @param message the detail message
     */
    public InsufficientStockException(String message) {
        super("INSUFFICIENT_STOCK", message);
        this.skuCode = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    /**
     * Gets the SKU code that has insufficient stock.
     *
     * @return the SKU code, or null if not set
     */
    public String getSkuCode() {
        return skuCode;
    }

    /**
     * Gets the quantity that was requested.
     *
     * @return the requested quantity, or null if not set
     */
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * Gets the quantity that is available.
     *
     * @return the available quantity, or null if not set
     */
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
