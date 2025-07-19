package com.inventorymanagement.common.testdata;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.product.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory class for creating test data objects with sensible defaults.
 */
public class TestDataFactory {

    private static final AtomicLong idCounter = new AtomicLong(1);

    // Category Factory
    public static CategoryBuilder category() {
        return new CategoryBuilder();
    }

    // Product Factory
    public static ProductBuilder product() {
        return new ProductBuilder();
    }

    // SKU Factory
    public static SkuBuilder sku() {
        return new SkuBuilder();
    }

    public static class CategoryBuilder {

        private Long id = idCounter.getAndIncrement();
        private String name = "Test Category " + UUID.randomUUID().toString().substring(0, 8);
        private String description = "Test category description";
        private String path = "/" + id;
        private Integer level = 0;
        private Category parent = null;
        private List<Category> children = new ArrayList<>();
        private Boolean isActive = true;

        public CategoryBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public CategoryBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public CategoryBuilder withLevel(Integer level) {
            this.level = level;
            return this;
        }

        public CategoryBuilder withParent(Category parent) {
            this.parent = parent;
            return this;
        }

        public CategoryBuilder withChildren(List<Category> children) {
            this.children = children;
            return this;
        }

        public CategoryBuilder inactive() {
            this.isActive = false;
            return this;
        }

        public Category build() {
            Category category = new Category();
            category.setId(id);
            category.setName(name);
            category.setDescription(description);
            category.setPath(path);
            category.setLevel(level);
            category.setParent(parent);
            category.setChildren(children);
            category.setIsActive(isActive);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            return category;
        }
    }

    public static class ProductBuilder {

        private Long id = idCounter.getAndIncrement();
        private String name = "Test Product " + UUID.randomUUID().toString().substring(0, 8);
        private String description = "Test product description";
        private String brand = "Test Brand";
        private Category category = null;
        private Boolean isActive = true;

        public ProductBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ProductBuilder withBrand(String brand) {
            this.brand = brand;
            return this;
        }

        public ProductBuilder withCategory(Category category) {
            this.category = category;
            return this;
        }

        public ProductBuilder inactive() {
            this.isActive = false;
            return this;
        }

        public Product build() {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setDescription(description);
            product.setBrand(brand);
            product.setCategory(category != null ? category : category().build());
            product.setIsActive(isActive);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            return product;
        }
    }

    public static class SkuBuilder {

        private final BigDecimal cost = BigDecimal.valueOf(50.00);
        private Long id = idCounter.getAndIncrement();
        private String skuCode = "SKU-" + UUID.randomUUID().toString().substring(0, 8);
        private Product product = null;
        private BigDecimal price = BigDecimal.valueOf(99.99);
        private Integer stockQuantity = 100;
        private Integer reorderPoint = 20;
        private Integer reorderQuantity = 50;
        private Boolean isActive = true;

        public SkuBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public SkuBuilder withSkuCode(String skuCode) {
            this.skuCode = skuCode;
            return this;
        }

        public SkuBuilder withProduct(Product product) {
            this.product = product;
            return this;
        }

        public SkuBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public SkuBuilder withStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
            return this;
        }

        public SkuBuilder withReorderPoint(Integer reorderPoint) {
            this.reorderPoint = reorderPoint;
            return this;
        }

        public SkuBuilder withReorderQuantity(Integer reorderQuantity) {
            this.reorderQuantity = reorderQuantity;
            return this;
        }

        public SkuBuilder inactive() {
            this.isActive = false;
            return this;
        }

        public Sku build() {
            Sku sku = new Sku();
            sku.setId(id);
            sku.setSkuCode(skuCode);
            sku.setProduct(product != null ? product : product().build());
            sku.setPrice(price);
            sku.setCost(cost);
            sku.setStockQuantity(stockQuantity);
            sku.setReorderPoint(reorderPoint);
            sku.setReorderQuantity(reorderQuantity);
            sku.setIsActive(isActive);
            sku.setCreatedAt(LocalDateTime.now());
            sku.setUpdatedAt(LocalDateTime.now());
            return sku;
        }
    }
}
