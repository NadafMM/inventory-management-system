package com.inventorymanagement.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data Transfer Object for SKU entity. Provides comprehensive inventory information including stock levels, pricing, and computed fields.
 */
@SuppressWarnings("DesignForExtension")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkuDto {

    private Long id;

    @NotBlank(message = "SKU code is required")
    @Size(max = 50, message = "SKU code must not exceed 50 characters")
    @JsonProperty("sku_code")
    private String skuCode;

    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("category_name")
    private String categoryName;

    @Size(max = 100, message = "Variant name must not exceed 100 characters")
    @JsonProperty("variant_name")
    private String variantName;

    @Size(max = 20, message = "Size must not exceed 20 characters")
    private String size;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be non-negative")
    private BigDecimal cost;

    @Min(value = 0, message = "Stock quantity must be non-negative")
    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @Min(value = 0, message = "Reserved quantity must be non-negative")
    @JsonProperty("reserved_quantity")
    private Integer reservedQuantity;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    @Min(value = 0, message = "Reorder point must be non-negative")
    @JsonProperty("reorder_point")
    private Integer reorderPoint;

    @Min(value = 0, message = "Reorder quantity must be non-negative")
    @JsonProperty("reorder_quantity")
    private Integer reorderQuantity;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @JsonProperty("is_active")
    private Boolean isActive;

    private String metadata;

    @JsonProperty("is_low_on_stock")
    private Boolean isLowOnStock;

    @JsonProperty("is_out_of_stock")
    private Boolean isOutOfStock;

    @JsonProperty("profit_margin")
    private BigDecimal profitMargin;

    @JsonProperty("stock_value")
    private BigDecimal stockValue;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    private Long version;

    // Constructors
    public SkuDto() {}

    public SkuDto(String skuCode, Long productId, BigDecimal price) {
        this.skuCode = skuCode;
        this.productId = productId;
        this.price = price;
    }

    public SkuDto(
            Long id,
            String skuCode,
            Long productId,
            String variantName,
            BigDecimal price,
            Boolean isActive) {
        this.id = id;
        this.skuCode = skuCode;
        this.productId = productId;
        this.variantName = variantName;
        this.price = price;
        this.isActive = isActive;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
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

    public Boolean getIsLowOnStock() {
        return isLowOnStock;
    }

    public void setIsLowOnStock(Boolean isLowOnStock) {
        this.isLowOnStock = isLowOnStock;
    }

    public Boolean getIsOutOfStock() {
        return isOutOfStock;
    }

    public void setIsOutOfStock(Boolean isOutOfStock) {
        this.isOutOfStock = isOutOfStock;
    }

    public BigDecimal getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(BigDecimal profitMargin) {
        this.profitMargin = profitMargin;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SkuDto skuDto = (SkuDto) obj;
        return Objects.equals(id, skuDto.id)
                && Objects.equals(skuCode, skuDto.skuCode)
                && Objects.equals(productId, skuDto.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, skuCode, productId);
    }

    @Override
    public String toString() {
        return "SkuDto{"
                + "id="
                + id
                + ", skuCode='"
                + skuCode
                + '\''
                + ", productName='"
                + productName
                + '\''
                + ", variantName='"
                + variantName
                + '\''
                + ", price="
                + price
                + ", stockQuantity="
                + stockQuantity
                + ", availableQuantity="
                + availableQuantity
                + ", isActive="
                + isActive
                + '}';
    }
}
