package com.inventorymanagement.product.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between Product entities and ProductDto objects. Provides static methods for entity-DTO conversion.
 */
public final class ProductMapper {

    private ProductMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a Product entity to ProductDto.
     *
     * @param product the Product entity to convert
     * @return ProductDto object or null if input is null
     */
    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setManufacturer(product.getManufacturer());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setColor(product.getColor());
        dto.setMaterial(product.getMaterial());
        dto.setIsActive(product.getIsActive());
        dto.setMetadata(product.getMetadata());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setDeletedAt(product.getDeletedAt());
        dto.setVersion(product.getVersion());

        // Set category information
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategoryPath(product.getCategory().getPath());
        }

        // Set computed fields
        dto.setSkuCount(product.getSkus().size());
        dto.setActiveSkuCount(product.getActiveSkuCount());
        dto.setTotalStockQuantity(product.getTotalStockQuantity());
        dto.setTotalAvailableQuantity(product.getTotalAvailableQuantity());
        dto.setIsLowOnStock(product.isLowOnStock());

        // Set price range
        BigDecimal[] priceRange = product.getPriceRange();
        if (priceRange != null) {
            dto.setMinPrice(priceRange[0]);
            dto.setMaxPrice(priceRange[1]);
        }

        return dto;
    }

    /**
     * Converts a ProductDto to Product entity. Note: This method creates a new entity and does not handle relationships.
     *
     * @param dto the ProductDto to convert
     * @return Product entity or null if input is null
     */
    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setManufacturer(dto.getManufacturer());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        product.setColor(dto.getColor());
        product.setMaterial(dto.getMaterial());
        product.setIsActive(dto.getIsActive());
        product.setMetadata(dto.getMetadata());
        product.setCreatedAt(dto.getCreatedAt());
        product.setUpdatedAt(dto.getUpdatedAt());
        product.setDeletedAt(dto.getDeletedAt());
        product.setVersion(dto.getVersion());

        return product;
    }

    /**
     * Updates an existing Product entity with data from ProductDto. Does not update ID, timestamps, or relationship fields.
     *
     * @param product the Product entity to update
     * @param dto     the ProductDto with updated data
     */
    public static void updateEntityFromDto(Product product, ProductDto dto) {
        if (product == null || dto == null) {
            return;
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBrand(dto.getBrand());
        product.setManufacturer(dto.getManufacturer());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        product.setColor(dto.getColor());
        product.setMaterial(dto.getMaterial());
        product.setIsActive(dto.getIsActive());
        product.setMetadata(dto.getMetadata());
    }

    /**
     * Converts a list of Product entities to a list of ProductDto objects.
     *
     * @param products the list of Product entities to convert
     * @return list of ProductDto objects
     */
    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }

        return products.stream().map(ProductMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Converts a list of ProductDto objects to a list of Product entities.
     *
     * @param dtos the list of ProductDto objects to convert
     * @return list of Product entities
     */
    public static List<Product> toEntityList(List<ProductDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream().map(ProductMapper::toEntity).collect(Collectors.toList());
    }

    /**
     * Creates a summary ProductDto with minimal information. Useful for nested objects or list views.
     *
     * @param product the Product entity to convert
     * @return ProductDto with summary information
     */
    public static ProductDto toSummaryDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setIsActive(product.getIsActive());
        dto.setActiveSkuCount(product.getActiveSkuCount());
        dto.setTotalStockQuantity(product.getTotalStockQuantity());
        dto.setIsLowOnStock(product.isLowOnStock());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Set price range
        BigDecimal[] priceRange = product.getPriceRange();
        if (priceRange != null) {
            dto.setMinPrice(priceRange[0]);
            dto.setMaxPrice(priceRange[1]);
        }

        return dto;
    }

    /**
     * Creates a ProductDto for catalog display with category information.
     *
     * @param product the Product entity to convert
     * @return ProductDto with catalog information
     */
    public static ProductDto toCatalogDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = toSummaryDto(product);
        dto.setDescription(product.getDescription());
        dto.setManufacturer(product.getManufacturer());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setColor(product.getColor());
        dto.setMaterial(product.getMaterial());
        dto.setTotalAvailableQuantity(product.getTotalAvailableQuantity());

        if (product.getCategory() != null) {
            dto.setCategoryPath(product.getCategory().getPath());
        }

        return dto;
    }
}
