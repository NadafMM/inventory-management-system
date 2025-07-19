package com.inventorymanagement.category.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for Category entity.
 *
 * <p>Used for API requests and responses in category management operations. Supports hierarchical
 * category structures with unlimited nesting levels.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Category data transfer object for hierarchical category management")
public class CategoryDto {

    @Schema(
            description = "Unique identifier for the category",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(
            description = "Category name",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is required", groups = CreateGroup.class)
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;

    @Schema(description = "Category description", example = "Electronic devices and accessories")
    private String description;

    @Schema(
            description = "Full hierarchical path of the category",
            example = "/Electronics/Computers/Laptops",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String path;

    @Schema(description = "Parent category ID for hierarchical structure", example = "10")
    @JsonProperty("parent_id")
    private Long parentId;

    @Schema(
            description = "Parent category name",
            example = "Technology",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("parent_name")
    private String parentName;

    @Schema(
            description = "Hierarchy level (0 for root categories)",
            example = "2",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Min(value = 0, message = "Category level must be non-negative")
    private Integer level;

    @Schema(description = "Sort order for display purposes", example = "1")
    @JsonProperty("sort_order")
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;

    @Schema(description = "Whether the category is active", example = "true")
    @JsonProperty("is_active")
    private Boolean isActive;

    @Schema(
            description = "Additional metadata in JSON format",
            example = "{\"color\": \"blue\", \"icon\": \"electronics\"}")
    private String metadata;

    @Schema(
            description = "Number of direct child categories",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("children_count")
    private Integer childrenCount;

    @Schema(
            description = "Number of products in this category",
            example = "25",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("products_count")
    private Integer productsCount;

    @Schema(
            description = "Whether this is a root category (no parent)",
            example = "false",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("is_root")
    private Boolean isRoot;

    @Schema(
            description = "Whether this is a leaf category (no children)",
            example = "true",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("is_leaf")
    private Boolean isLeaf;

    @Schema(
            description = "List of category names in the path",
            example = "[\"Electronics\", \"Computers\", \"Laptops\"]",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("path_names")
    private List<String> pathNames;

    @Schema(
            description = "Category creation timestamp",
            example = "2025-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(
            description = "Category last update timestamp",
            example = "2025-01-15T14:45:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Schema(
            description = "Category deletion timestamp (soft delete)",
            example = "2025-01-15T16:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @Schema(
            description = "Version number for optimistic locking",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    // Constructors
    public CategoryDto() {}

    public CategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public CategoryDto(Long id, String name, String description, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
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

    public Integer getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public Integer getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(Integer productsCount) {
        this.productsCount = productsCount;
    }

    public Boolean getIsRoot() {
        return isRoot;
    }

    public void setIsRoot(Boolean isRoot) {
        this.isRoot = isRoot;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public List<String> getPathNames() {
        return pathNames;
    }

    public void setPathNames(List<String> pathNames) {
        this.pathNames = pathNames;
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
        CategoryDto that = (CategoryDto) obj;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, path);
    }

    @Override
    public String toString() {
        return "CategoryDto{"
                + "id="
                + id
                + ", name='"
                + name
                + '\''
                + ", level="
                + level
                + ", isActive="
                + isActive
                + ", childrenCount="
                + childrenCount
                + ", productsCount="
                + productsCount
                + '}';
    }

    /**
     * Validation group for create operations.
     */
    public interface CreateGroup {}

    /**
     * Validation group for update operations.
     */
    public interface UpdateGroup {}
}
