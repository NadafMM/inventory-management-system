package com.inventorymanagement.inventory.model;

import com.inventorymanagement.common.model.BaseAuditEntity;
import com.inventorymanagement.product.model.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a Stock Keeping Unit (SKU) in the inventory system. Tracks individual product variations with their stock levels and pricing.
 */
@Entity
@Table(
        name = "skus",
        indexes = {
                @Index(name = "idx_sku_product", columnList = "product_id"),
                @Index(name = "idx_sku_code", columnList = "sku_code", unique = true),
                @Index(name = "idx_sku_barcode", columnList = "barcode"),
                @Index(name = "idx_sku_stock", columnList = "stock_quantity,reorder_point"),
                @Index(name = "idx_sku_active", columnList = "is_active")
        })
@SuppressWarnings("DesignForExtension")
public class Sku extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU code is required")
    @Size(max = 50, message = "SKU code must not exceed 50 characters")
    @Column(name = "sku_code", nullable = false, unique = true, length = 50)
    private String skuCode;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Size(max = 100, message = "Variant name must not exceed 100 characters")
    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Size(max = 20, message = "Size must not exceed 20 characters")
    @Column(name = "size", length = 20)
    private String size;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(name = "color", length = 50)
    private String color;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be non-negative")
    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Min(value = 0, message = "Stock quantity must be non-negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Min(value = 0, message = "Reserved quantity must be non-negative")
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Min(value = 0, message = "Available quantity must be non-negative")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Min(value = 0, message = "Reorder point must be non-negative")
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 0;

    @Min(value = 0, message = "Reorder quantity must be non-negative")
    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity = 0;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    @Column(name = "barcode", length = 100)
    private String barcode;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @OneToMany(mappedBy = "sku", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> inventoryTransactions = new ArrayList<>();

    // Constructors
    public Sku() {}

    public Sku(String skuCode, Product product, BigDecimal price) {
        this.skuCode = skuCode;
        this.product = product;
        this.price = price;
        updateAvailableQuantity();
    }

    public Sku(String skuCode, Product product, String variantName, BigDecimal price) {
        this.skuCode = skuCode;
        this.product = product;
        this.variantName = variantName;
        this.price = price;
        updateAvailableQuantity();
    }

    // Business methods

    /**
     * Updates the available quantity based on current stock and reserved quantities.
     */
    private void updateAvailableQuantity() {
        this.availableQuantity = Math.max(0, stockQuantity - reservedQuantity);
    }

    /**
     * Gets the available quantity (stock - reserved).
     *
     * @return available quantity
     */
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Checks if the SKU is below its reorder point.
     *
     * @return true if stock is at or below reorder point
     */
    public boolean isLowOnStock() {
        return stockQuantity <= reorderPoint;
    }

    /**
     * Checks if the SKU is out of stock.
     *
     * @return true if stock quantity is zero
     */
    public boolean isOutOfStock() {
        return stockQuantity == 0;
    }

    /**
     * Checks if the SKU has available stock for sale.
     *
     * @return true if available quantity is greater than zero
     */
    public boolean hasAvailableStock() {
        return getAvailableQuantity() > 0;
    }

    /**
     * Adjusts the stock quantity by the specified amount.
     *
     * @param adjustment the amount to adjust (positive for increase, negative for decrease)
     * @return the new stock quantity
     */
    public Integer adjustStock(Integer adjustment) {
        this.stockQuantity = Math.max(0, this.stockQuantity + adjustment);
        updateAvailableQuantity();
        return this.stockQuantity;
    }

    /**
     * Reserves the specified quantity from available stock.
     *
     * @param quantity the quantity to reserve
     * @return true if reservation was successful, false if insufficient stock
     */
    public boolean reserveStock(Integer quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (availableQuantity >= quantity) {
            this.reservedQuantity += quantity;
            updateAvailableQuantity();
            return true;
        }

        return false;
    }

    /**
     * Releases the specified quantity from reserved stock.
     *
     * @param quantity the quantity to release
     * @return the actual quantity released
     */
    public Integer releaseReservedStock(Integer quantity) {
        if (quantity <= 0) {
            return 0;
        }

        int actualRelease = Math.min(quantity, this.reservedQuantity);
        this.reservedQuantity -= actualRelease;
        updateAvailableQuantity();
        return actualRelease;
    }

    /**
     * Fulfills an order by reducing both stock and reserved quantities.
     *
     * @param quantity the quantity to fulfill
     * @return true if fulfillment was successful, false if insufficient reserved stock
     */
    public boolean fulfillOrder(Integer quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (this.reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.stockQuantity -= quantity;
            updateAvailableQuantity();
            return true;
        }

        return false;
    }

    /**
     * Calculates the profit margin for this SKU.
     *
     * @return profit margin as a percentage, or null if cost is not set
     */
    public BigDecimal getProfitMargin() {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal profit = price.subtract(cost);
        return profit
                .divide(price, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Gets the total value of current stock.
     *
     * @return total stock value at current price
     */
    public BigDecimal getStockValue() {
        return price.multiply(BigDecimal.valueOf(stockQuantity));
    }

    /**
     * Deactivates this SKU.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activates this SKU.
     */
    public void activate() {
        this.isActive = true;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        updateAvailableQuantity();
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        updateAvailableQuantity();
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public List<InventoryTransaction> getInventoryTransactions() {
        return inventoryTransactions;
    }

    public void setInventoryTransactions(List<InventoryTransaction> inventoryTransactions) {
        this.inventoryTransactions = inventoryTransactions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        Sku sku = (Sku) obj;
        return Objects.equals(id, sku.id)
                && Objects.equals(skuCode, sku.skuCode)
                && Objects.equals(product, sku.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, skuCode, product);
    }

    @Override
    public String toString() {
        return "Sku{"
                + "id="
                + id
                + ", skuCode='"
                + skuCode
                + '\''
                + ", product="
                + (product != null ? product.getName() : null)
                + ", variantName='"
                + variantName
                + '\''
                + ", stockQuantity="
                + stockQuantity
                + ", price="
                + price
                + ", isActive="
                + isActive
                + '}';
    }
}
