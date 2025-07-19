package com.inventorymanagement.category.model;

import com.inventorymanagement.common.model.BaseAuditEntity;
import com.inventorymanagement.product.model.Product;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Category entity representing hierarchical product categories. Uses path enumeration pattern for efficient hierarchy queries.
 */
@Entity
@Table(name = "categories")
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category path is required")
    @Size(max = 1000, message = "Category path must not exceed 1000 characters")
    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    @Min(value = 0, message = "Category level must be non-negative")
    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Min(value = 0, message = "Sort order must be non-negative")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Constructors
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Category(String name, String description, Category parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        if (parent != null) {
            this.level = parent.getLevel() + 1;
            parent.getChildren().add(this);
        }
    }

    // Business methods

    /**
     * Checks if this category is a root category (has no parent).
     *
     * @return true if this is a root category, false otherwise
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Checks if this category is a leaf category (has no children).
     *
     * @return true if this is a leaf category, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Gets the full path names of this category hierarchy.
     *
     * @return array of category names from root to this category
     */
    public String[] getPathNames() {
        if (path == null || path.isEmpty()) {
            return new String[]{name};
        }

        String[] pathIds = path.split("/");
        List<String> pathNames = new ArrayList<>();

        // This would typically be resolved with a service method
        // For now, we'll return the current category name
        pathNames.add(name);

        return pathNames.toArray(new String[0]);
    }

    /**
     * Adds a child category to this category.
     *
     * @param child the child category to add
     */
    public void addChild(Category child) {
        if (child != null) {
            children.add(child);
            child.setParent(this);
            child.setLevel(this.level + 1);
        }
    }

    /**
     * Removes a child category from this category.
     *
     * @param child the child category to remove
     */
    public void removeChild(Category child) {
        if (child != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * Deactivates this category and all its children.
     */
    public void deactivate() {
        this.isActive = false;
        children.forEach(Category::deactivate);
    }

    /**
     * Activates this category.
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
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
        Category category = (Category) obj;
        return Objects.equals(id, category.id)
                && Objects.equals(name, category.name)
                && Objects.equals(path, category.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, path);
    }

    @Override
    public String toString() {
        return "Category{"
                + "id="
                + id
                + ", name='"
                + name
                + '\''
                + ", path='"
                + path
                + '\''
                + ", level="
                + level
                + ", isActive="
                + isActive
                + '}';
    }
}
