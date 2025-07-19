package com.inventorymanagement.product.model;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.common.model.BaseAuditEntity;
import com.inventorymanagement.inventory.model.Sku;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Product entity representing products in the inventory system. Products belong to categories and can have multiple SKUs.
 */
@Entity
@Table(name = "products")
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be non-negative")
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    @Column(name = "dimensions", length = 50)
    private String dimensions;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(name = "color", length = 50)
    private String color;

    @Size(max = 100, message = "Material must not exceed 100 characters")
    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sku> skus = new ArrayList<>();

    // Constructors
    public Product() {}

    public Product(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Product(
            String name, String description, Category category, String brand, String manufacturer) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.manufacturer = manufacturer;
    }

    // Business methods

    /**
     * Checks if the product has any active SKUs.
     *
     * @return true if the product has active SKUs, false otherwise
     */
    public boolean hasActiveSkus() {
        return skus.stream().anyMatch(sku -> sku.getIsActive() && !sku.isDeleted());
    }

    /**
     * Gets the count of active SKUs for this product.
     *
     * @return number of active SKUs
     */
    public int getActiveSkuCount() {
        return (int) skus.stream().filter(sku -> sku.getIsActive() && !sku.isDeleted()).count();
    }

    /**
     * Gets the total stock quantity across all SKUs.
     *
     * @return total stock quantity
     */
    public int getTotalStockQuantity() {
        return skus.stream()
                .filter(sku -> sku.getIsActive() && !sku.isDeleted())
                .mapToInt(Sku::getStockQuantity)
                .sum();
    }

    /**
     * Gets the total available quantity across all SKUs.
     *
     * @return total available quantity
     */
    public int getTotalAvailableQuantity() {
        return skus.stream()
                .filter(sku -> sku.getIsActive() && !sku.isDeleted())
                .mapToInt(Sku::getAvailableQuantity)
                .sum();
    }

    /**
     * Checks if the product is low on stock based on reorder points.
     *
     * @return true if any SKU is below its reorder point
     */
    public boolean isLowOnStock() {
        return skus.stream()
                .filter(sku -> sku.getIsActive() && !sku.isDeleted())
                .anyMatch(sku -> sku.getStockQuantity() <= sku.getReorderPoint());
    }

    /**
     * Gets the price range for this product across all SKUs.
     *
     * @return array containing [minPrice, maxPrice], or null if no active SKUs
     */
    public BigDecimal[] getPriceRange() {
        List<BigDecimal> prices =
                skus.stream()
                        .filter(sku -> sku.getIsActive() && !sku.isDeleted())
                        .map(Sku::getPrice)
                        .filter(Objects::nonNull)
                        .toList();

        if (prices.isEmpty()) {
            return null;
        }

        BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        return new BigDecimal[]{minPrice, maxPrice};
    }

    /**
     * Adds a SKU to this product.
     *
     * @param sku the SKU to add
     */
    public void addSku(Sku sku) {
        if (sku != null) {
            skus.add(sku);
            sku.setProduct(this);
        }
    }

    /**
     * Removes a SKU from this product.
     *
     * @param sku the SKU to remove
     */
    public void removeSku(Sku sku) {
        if (sku != null) {
            skus.remove(sku);
            sku.setProduct(null);
        }
    }

    /**
     * Deactivates this product and all its SKUs.
     */
    public void deactivate() {
        this.isActive = false;
        skus.forEach(Sku::deactivate);
    }

    /**
     * Activates this product.
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
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
        Product product = (Product) obj;
        return Objects.equals(id, product.id)
                && Objects.equals(name, product.name)
                && Objects.equals(category, product.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, category);
    }

    @Override
    public String toString() {
        return "Product{"
                + "id="
                + id
                + ", name='"
                + name
                + '\''
                + ", category="
                + (category != null ? category.getName() : null)
                + ", brand='"
                + brand
                + '\''
                + ", isActive="
                + isActive
                + '}';
    }
}
