package com.inventorymanagement.inventory.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class for converting between Sku entities and SkuDto objects. Provides static methods for entity-DTO conversion.
 */
public final class SkuMapper {

    private SkuMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a Sku entity to SkuDto.
     *
     * @param sku the Sku entity to convert
     * @return SkuDto object or null if input is null
     */
    public static SkuDto toDto(Sku sku) {
        if (sku == null) {
            return null;
        }

        SkuDto dto = new SkuDto();
        dto.setId(sku.getId());
        dto.setSkuCode(sku.getSkuCode());
        dto.setVariantName(sku.getVariantName());
        dto.setSize(sku.getSize());
        dto.setColor(sku.getColor());
        dto.setPrice(sku.getPrice());
        dto.setCost(sku.getCost());
        dto.setStockQuantity(sku.getStockQuantity());
        dto.setReservedQuantity(sku.getReservedQuantity());
        dto.setReorderPoint(sku.getReorderPoint());
        dto.setReorderQuantity(sku.getReorderQuantity());
        dto.setBarcode(sku.getBarcode());
        dto.setLocation(sku.getLocation());
        dto.setIsActive(sku.getIsActive());
        dto.setMetadata(sku.getMetadata());
        dto.setCreatedAt(sku.getCreatedAt());
        dto.setUpdatedAt(sku.getUpdatedAt());
        dto.setDeletedAt(sku.getDeletedAt());
        dto.setVersion(sku.getVersion());

        // Set product information
        if (sku.getProduct() != null) {
            dto.setProductId(sku.getProduct().getId());
            dto.setProductName(sku.getProduct().getName());

            if (sku.getProduct().getCategory() != null) {
                dto.setCategoryName(sku.getProduct().getCategory().getName());
            }
        }

        // Set computed fields
        dto.setAvailableQuantity(sku.getAvailableQuantity());
        dto.setIsLowOnStock(sku.isLowOnStock());
        dto.setIsOutOfStock(sku.isOutOfStock());
        dto.setProfitMargin(sku.getProfitMargin());
        dto.setStockValue(sku.getStockValue());

        return dto;
    }

    /**
     * Converts a SkuDto to Sku entity. Note: This method creates a new entity and does not handle relationships.
     *
     * @param dto the SkuDto to convert
     * @return Sku entity or null if input is null
     */
    public static Sku toEntity(SkuDto dto) {
        if (dto == null) {
            return null;
        }

        Sku sku = new Sku();
        sku.setId(dto.getId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setVariantName(dto.getVariantName());
        sku.setSize(dto.getSize());
        sku.setColor(dto.getColor());
        sku.setPrice(dto.getPrice());
        sku.setCost(dto.getCost());
        sku.setStockQuantity(dto.getStockQuantity());
        sku.setReservedQuantity(dto.getReservedQuantity());
        sku.setReorderPoint(dto.getReorderPoint());
        sku.setReorderQuantity(dto.getReorderQuantity());
        sku.setBarcode(dto.getBarcode());
        sku.setLocation(dto.getLocation());
        sku.setIsActive(dto.getIsActive());
        sku.setMetadata(dto.getMetadata());
        sku.setCreatedAt(dto.getCreatedAt());
        sku.setUpdatedAt(dto.getUpdatedAt());
        sku.setDeletedAt(dto.getDeletedAt());
        sku.setVersion(dto.getVersion());

        return sku;
    }

    /**
     * Updates an existing Sku entity with data from SkuDto. Does not update ID, timestamps, or relationship fields.
     *
     * @param sku the Sku entity to update
     * @param dto the SkuDto with updated data
     */
    public static void updateEntityFromDto(Sku sku, SkuDto dto) {
        if (sku == null || dto == null) {
            return;
        }

        sku.setSkuCode(dto.getSkuCode());
        sku.setVariantName(dto.getVariantName());
        sku.setSize(dto.getSize());
        sku.setColor(dto.getColor());
        sku.setPrice(dto.getPrice());
        sku.setCost(dto.getCost());
        sku.setStockQuantity(dto.getStockQuantity());
        sku.setReservedQuantity(dto.getReservedQuantity());
        sku.setReorderPoint(dto.getReorderPoint());
        sku.setReorderQuantity(dto.getReorderQuantity());
        sku.setBarcode(dto.getBarcode());
        sku.setLocation(dto.getLocation());
        sku.setIsActive(dto.getIsActive());
        sku.setMetadata(dto.getMetadata());
    }

    /**
     * Converts a list of Sku entities to a list of SkuDto objects.
     *
     * @param skus the list of Sku entities to convert
     * @return list of SkuDto objects
     */
    public static List<SkuDto> toDtoList(List<Sku> skus) {
        if (skus == null) {
            return null;
        }

        return skus.stream().map(SkuMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Converts a list of SkuDto objects to a list of Sku entities.
     *
     * @param dtos the list of SkuDto objects to convert
     * @return list of Sku entities
     */
    public static List<Sku> toEntityList(List<SkuDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream().map(SkuMapper::toEntity).collect(Collectors.toList());
    }

    /**
     * Creates a summary SkuDto with minimal information. Useful for nested objects or list views.
     *
     * @param sku the Sku entity to convert
     * @return SkuDto with summary information
     */
    public static SkuDto toSummaryDto(Sku sku) {
        if (sku == null) {
            return null;
        }

        SkuDto dto = new SkuDto();
        dto.setId(sku.getId());
        dto.setSkuCode(sku.getSkuCode());
        dto.setVariantName(sku.getVariantName());
        dto.setPrice(sku.getPrice());
        dto.setStockQuantity(sku.getStockQuantity());
        dto.setAvailableQuantity(sku.getAvailableQuantity());
        dto.setIsActive(sku.getIsActive());
        dto.setIsLowOnStock(sku.isLowOnStock());
        dto.setIsOutOfStock(sku.isOutOfStock());

        if (sku.getProduct() != null) {
            dto.setProductId(sku.getProduct().getId());
            dto.setProductName(sku.getProduct().getName());
        }

        return dto;
    }

    /**
     * Creates a SkuDto for inventory display with stock information.
     *
     * @param sku the Sku entity to convert
     * @return SkuDto with inventory information
     */
    public static SkuDto toInventoryDto(Sku sku) {
        if (sku == null) {
            return null;
        }

        SkuDto dto = toSummaryDto(sku);
        dto.setReservedQuantity(sku.getReservedQuantity());
        dto.setReorderPoint(sku.getReorderPoint());
        dto.setReorderQuantity(sku.getReorderQuantity());
        dto.setLocation(sku.getLocation());
        dto.setBarcode(sku.getBarcode());
        dto.setStockValue(sku.getStockValue());
        dto.setProfitMargin(sku.getProfitMargin());

        if (sku.getProduct() != null && sku.getProduct().getCategory() != null) {
            dto.setCategoryName(sku.getProduct().getCategory().getName());
        }

        return dto;
    }
}
