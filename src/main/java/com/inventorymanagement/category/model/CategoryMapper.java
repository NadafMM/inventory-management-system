package com.inventorymanagement.category.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between Category entities and CategoryDto objects. Provides static methods for entity-DTO conversion.
 */
public final class CategoryMapper {

    private CategoryMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a Category entity to CategoryDto.
     *
     * @param category the Category entity to convert
     * @return CategoryDto object or null if input is null
     */
    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setPath(category.getPath());
        dto.setLevel(category.getLevel());
        dto.setSortOrder(category.getSortOrder());
        dto.setIsActive(category.getIsActive());
        dto.setMetadata(category.getMetadata());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setDeletedAt(category.getDeletedAt());
        dto.setVersion(category.getVersion());

        // Set parent information
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Set computed fields
        dto.setChildrenCount(category.getChildren().size());
        dto.setProductsCount(category.getProducts().size());
        dto.setIsRoot(category.isRoot());
        dto.setIsLeaf(category.isLeaf());
        dto.setPathNames(List.of(category.getPathNames()));

        return dto;
    }

    /**
     * Converts a CategoryDto to Category entity. Note: This method creates a new entity and does not handle relationships.
     *
     * @param dto the CategoryDto to convert
     * @return Category entity or null if input is null
     */
    public static Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setPath(dto.getPath());
        category.setLevel(dto.getLevel());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true); // Default to true
        category.setMetadata(dto.getMetadata());
        category.setCreatedAt(dto.getCreatedAt());
        category.setUpdatedAt(dto.getUpdatedAt());
        category.setDeletedAt(dto.getDeletedAt());
        category.setVersion(dto.getVersion());

        return category;
    }

    /**
     * Updates an existing Category entity with data from CategoryDto. Does not update ID, timestamps, or relationship fields.
     *
     * @param category the Category entity to update
     * @param dto      the CategoryDto with updated data
     */
    public static void updateEntityFromDto(Category category, CategoryDto dto) {
        if (category == null || dto == null) {
            return;
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder());
        category.setIsActive(dto.getIsActive());
        category.setMetadata(dto.getMetadata());
    }

    /**
     * Converts a list of Category entities to a list of CategoryDto objects.
     *
     * @param categories the list of Category entities to convert
     * @return list of CategoryDto objects
     */
    public static List<CategoryDto> toDtoList(List<Category> categories) {
        if (categories == null) {
            return null;
        }

        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Converts a list of CategoryDto objects to a list of Category entities.
     *
     * @param dtos the list of CategoryDto objects to convert
     * @return list of Category entities
     */
    public static List<Category> toEntityList(List<CategoryDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream().map(CategoryMapper::toEntity).collect(Collectors.toList());
    }

    /**
     * Creates a summary CategoryDto with minimal information. Useful for nested objects or list views.
     *
     * @param category the Category entity to convert
     * @return CategoryDto with summary information
     */
    public static CategoryDto toSummaryDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setLevel(category.getLevel());
        dto.setIsActive(category.getIsActive());
        dto.setIsRoot(category.isRoot());
        dto.setIsLeaf(category.isLeaf());
        dto.setChildrenCount(category.getChildren().size());
        dto.setProductsCount(category.getProducts().size());

        return dto;
    }

    /**
     * Creates a CategoryDto for hierarchy display with parent information.
     *
     * @param category the Category entity to convert
     * @return CategoryDto with hierarchy information
     */
    public static CategoryDto toHierarchyDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = toSummaryDto(category);
        dto.setPath(category.getPath());
        dto.setPathNames(List.of(category.getPathNames()));

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }
}
