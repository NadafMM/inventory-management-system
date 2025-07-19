package com.inventorymanagement.product.service;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.model.ProductDto;
import com.inventorymanagement.product.model.ProductMapper;
import com.inventorymanagement.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service class for managing Product entities and business operations. Provides comprehensive product management including validation, caching, and
 * business rule enforcement.
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_ENTITY_NAME = "Product";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(
            ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ===== CRUD OPERATIONS =====

    /**
     * Creates a new product with business rule validation.
     *
     * @param productDto the product data
     * @return the created product DTO
     * @throws ValidationException     if validation fails
     * @throws EntityNotFoundException if category not found
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public ProductDto createProduct(@Valid @NotNull ProductDto productDto) {
        logger.info("Creating new product: {}", productDto.getName());

        // Trim whitespace from text fields
        if (productDto.getName() != null) {
            productDto.setName(productDto.getName().trim());
        }
        if (productDto.getBrand() != null) {
            productDto.setBrand(productDto.getBrand().trim());
        }
        if (productDto.getDescription() != null) {
            productDto.setDescription(productDto.getDescription().trim());
        }

        validateProductForCreation(productDto);

        Product product = ProductMapper.toEntity(productDto);

        // Set up category relationship
        Category category = findCategoryById(productDto.getCategoryId());
        validateCategoryForProduct(category);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        logger.info("Successfully created product with ID: {}", savedProduct.getId());
        return ProductMapper.toDto(savedProduct);
    }

    /**
     * Updates an existing product with business rule validation.
     *
     * @param id         the product ID
     * @param productDto the updated product data
     * @return the updated product DTO
     * @throws EntityNotFoundException if product not found
     * @throws ValidationException     if validation fails
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public ProductDto updateProduct(@NotNull Long id, @Valid @NotNull ProductDto productDto) {
        logger.info("Updating product with ID: {}", id);

        Product existingProduct = findProductById(id);
        validateProductForUpdate(existingProduct, productDto);

        // Update basic fields
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setBrand(productDto.getBrand());
        existingProduct.setManufacturer(productDto.getManufacturer());
        existingProduct.setWeight(productDto.getWeight());
        existingProduct.setDimensions(productDto.getDimensions());
        existingProduct.setColor(productDto.getColor());
        existingProduct.setMaterial(productDto.getMaterial());
        existingProduct.setMetadata(productDto.getMetadata());

        // Handle category change
        if (shouldUpdateCategory(existingProduct, productDto)) {
            Category newCategory = findCategoryById(productDto.getCategoryId());
            validateCategoryForProduct(newCategory);
            existingProduct.setCategory(newCategory);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Successfully updated product with ID: {}", id);

        return ProductMapper.toDto(updatedProduct);
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id the product ID
     * @return the product DTO
     * @throws EntityNotFoundException if product not found
     */
    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(@NotNull Long id) {
        logger.debug("Retrieving product with ID: {}", id);
        Product product = findProductById(id);
        return ProductMapper.toDto(product);
    }

    /**
     * Retrieves all products with pagination.
     *
     * @param pageable pagination information
     * @return page of product DTOs
     */
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        logger.debug("Retrieving all products with pagination");
        Page<Product> products = productRepository.findAllActive(pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Soft deletes a product.
     *
     * @param id the product ID
     * @throws EntityNotFoundException if product not found
     * @throws BusinessException       if product has active SKUs
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public void deleteProduct(@NotNull Long id) {
        logger.info("Deleting product with ID: {}", id);

        Product product = findProductById(id);
        validateProductForDeletion(product);

        // Soft delete the product
        product.markAsDeleted();
        productRepository.save(product);

        logger.info("Successfully deleted product with ID: {}", id);
    }

    // ===== CATEGORY-BASED OPERATIONS =====

    /**
     * Retrieves all products in a specific category.
     *
     * @param categoryId the category ID
     * @param pageable   pagination information
     * @return page of product DTOs
     * @throws EntityNotFoundException if category not found
     */
    @Cacheable(
            value = "productCategories",
            key = "#categoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProductDto> getProductsByCategory(@NotNull Long categoryId, Pageable pageable) {
        logger.debug("Retrieving products for category ID: {}", categoryId);

        // Verify category exists
        findCategoryById(categoryId);

        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Retrieves all products in categories that match a path pattern (hierarchical).
     *
     * @param categoryPath the category path pattern
     * @return list of product DTOs
     */
    public List<ProductDto> getProductsByCategoryPath(@NotNull String categoryPath) {
        logger.debug("Retrieving products for category path: {}", categoryPath);

        if (!StringUtils.hasText(categoryPath)) {
            throw new ValidationException("categoryPath", "Category path cannot be empty");
        }

        List<Product> products = productRepository.findByCategoryPathStartingWith(categoryPath);
        return products.stream().map(ProductMapper::toDto).toList();
    }

    // ===== SEARCH AND FILTER OPERATIONS =====

    /**
     * Searches products by name (case-insensitive partial match).
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProductsByName(@NotNull String name, Pageable pageable) {
        logger.debug("Searching products by name: {}", name);

        if (!StringUtils.hasText(name)) {
            throw new ValidationException("name", "Search term cannot be empty");
        }

        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Searches products by brand.
     *
     * @param brand    the brand name
     * @param pageable pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProductsByBrand(@NotNull String brand, Pageable pageable) {
        logger.debug("Searching products by brand: {}", brand);

        if (!StringUtils.hasText(brand)) {
            throw new ValidationException("brand", "Brand name cannot be empty");
        }

        Page<Product> products = productRepository.findByBrandIgnoreCase(brand, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Searches products by manufacturer.
     *
     * @param manufacturer the manufacturer name
     * @param pageable     pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProductsByManufacturer(
            @NotNull String manufacturer, Pageable pageable) {
        logger.debug("Searching products by manufacturer: {}", manufacturer);

        if (!StringUtils.hasText(manufacturer)) {
            throw new ValidationException("manufacturer", "Manufacturer name cannot be empty");
        }

        Page<Product> products = productRepository.findByManufacturerIgnoreCase(manufacturer, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Searches products with multiple criteria.
     *
     * @param searchTerm   the search term for name/description (optional)
     * @param brand        the brand name (optional)
     * @param categoryId   the category ID (optional)
     * @param manufacturer the manufacturer name (optional)
     * @param isActive     the active status (optional)
     * @param pageable     pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProducts(
            String searchTerm,
            String brand,
            Long categoryId,
            String manufacturer,
            Boolean isActive,
            Pageable pageable) {
        logger.debug(
                "Searching products with criteria - searchTerm: {}, brand: {}, categoryId: {}, manufacturer: {}, isActive: {}",
                searchTerm,
                brand,
                categoryId,
                manufacturer,
                isActive);

        Page<Product> products =
                productRepository.findWithFilters(
                        searchTerm, categoryId, brand, manufacturer, isActive, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Searches products with extended criteria including price range.
     *
     * @param name         the search term for name (optional)
     * @param categoryId   the category ID (optional)
     * @param brand        the brand name (optional)
     * @param manufacturer the manufacturer name (optional)
     * @param minPrice     the minimum price (optional)
     * @param maxPrice     the maximum price (optional)
     * @param isActive     the active status (optional)
     * @param pageable     pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProducts(
            String name,
            Long categoryId,
            String brand,
            String manufacturer,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Pageable pageable) {
        logger.debug(
                "Searching products with extended criteria - name: {}, categoryId: {}, brand: {}, manufacturer: {}, minPrice: {}, maxPrice: {}, isActive: {}",
                name,
                categoryId,
                brand,
                manufacturer,
                minPrice,
                maxPrice,
                isActive);

        Page<Product> products =
                productRepository.findWithExtendedFilters(
                        name, categoryId, brand, manufacturer, minPrice, maxPrice, isActive, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Searches products by name or description.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProductsByNameOrDescription(
            @NotNull String query, Pageable pageable) {
        logger.debug("Searching products by name or description: {}", query);

        if (query == null) {
            throw new ValidationException("query", "search term cannot be null");
        }

        // Allow empty strings - they should return empty results
        Page<Product> products =
                productRepository.findByNameOrDescriptionContainingIgnoreCase(query, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Retrieves products by brand.
     *
     * @param brand    the brand name
     * @param pageable pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> getProductsByBrand(@NotNull String brand, Pageable pageable) {
        logger.debug("Retrieving products by brand: {}", brand);

        if (!StringUtils.hasText(brand)) {
            throw new ValidationException("brand", "Brand name cannot be empty");
        }

        Page<Product> products = productRepository.findByBrandIgnoreCase(brand, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Retrieves products by active status.
     *
     * @param isActive the active status
     * @param pageable pagination information
     * @return page of product DTOs
     */
    public Page<ProductDto> getProductsByStatus(boolean isActive, Pageable pageable) {
        logger.debug("Retrieving products by status: {}", isActive);
        Page<Product> products = productRepository.findByIsActive(isActive, pageable);
        return products.map(ProductMapper::toDto);
    }

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Activates a product.
     *
     * @param id the product ID
     * @throws EntityNotFoundException if product not found
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public void activateProduct(@NotNull Long id) {
        logger.info("Activating product with ID: {}", id);

        Product product = findProductById(id);

        // Validate that category is active
        if (!product.getCategory().getIsActive()) {
            throw new BusinessException("Cannot activate product in inactive category");
        }

        product.activate();
        productRepository.save(product);

        logger.info("Successfully activated product with ID: {}", id);
    }

    /**
     * Deactivates a product.
     *
     * @param id the product ID
     * @throws EntityNotFoundException if product not found
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public void deactivateProduct(@NotNull Long id) {
        logger.info("Deactivating product with ID: {}", id);

        Product product = findProductById(id);
        product.deactivate(); // This deactivates SKUs as well
        productRepository.save(product);

        logger.info("Successfully deactivated product with ID: {}", id);
    }

    /**
     * Moves a product to a different category.
     *
     * @param productId     the product ID
     * @param newCategoryId the new category ID
     * @throws EntityNotFoundException if product or category not found
     * @throws BusinessException       if category is invalid
     */
    @CacheEvict(
            value = {"products", "productCategories"},
            allEntries = true)
    public void moveProductToCategory(@NotNull Long productId, @NotNull Long newCategoryId) {
        logger.info("Moving product {} to category {}", productId, newCategoryId);

        Product product = findProductById(productId);
        Category newCategory = findCategoryById(newCategoryId);

        validateCategoryForProduct(newCategory);

        product.setCategory(newCategory);
        productRepository.save(product);

        logger.info("Successfully moved product {} to category {}", productId, newCategoryId);
    }

    /**
     * Gets product statistics for a category.
     *
     * @param categoryId the category ID
     * @return product statistics
     */
    public ProductStatistics getProductStatistics(@NotNull Long categoryId) {
        logger.debug("Getting product statistics for category: {}", categoryId);

        // Verify category exists
        findCategoryById(categoryId);

        long totalProducts = productRepository.countByCategoryId(categoryId);
        long activeProducts = productRepository.countActiveByCategoryId(categoryId);

        return new ProductStatistics(totalProducts, activeProducts, totalProducts - activeProducts);
    }

    /**
     * Gets all distinct brands.
     *
     * @return list of distinct brands
     */
    @Cacheable(value = "productBrands")
    public List<String> getAllBrands() {
        logger.debug("Retrieving all distinct brands");
        return productRepository.findAllDistinctBrands();
    }

    /**
     * Gets all distinct manufacturers.
     *
     * @return list of distinct manufacturers
     */
    @Cacheable(value = "productManufacturers")
    public List<String> getAllManufacturers() {
        logger.debug("Retrieving all distinct manufacturers");
        return productRepository.findAllDistinctManufacturers();
    }

    /**
     * Gets all active products with pagination.
     *
     * @param pageable pagination information
     * @return page of active product DTOs
     */
    public Page<ProductDto> getActiveProducts(Pageable pageable) {
        logger.debug("Retrieving all active products with pagination");
        Page<Product> products = productRepository.findByIsActive(true, pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Gets all products that are low on stock.
     *
     * @param pageable pagination information
     * @return page of product DTOs with low stock
     */
    public Page<ProductDto> getLowStockProducts(Pageable pageable) {
        logger.debug("Retrieving products with low stock");
        Page<Product> products = productRepository.findLowStockProducts(pageable);
        return products.map(ProductMapper::toDto);
    }

    /**
     * Gets all distinct brands.
     *
     * @return list of distinct brands
     */
    @Cacheable(value = "productBrands")
    public List<String> getDistinctBrands() {
        logger.debug("Retrieving all distinct brands");
        return productRepository.findAllDistinctBrands();
    }

    /**
     * Gets all distinct manufacturers.
     *
     * @return list of distinct manufacturers
     */
    @Cacheable(value = "productManufacturers")
    public List<String> getDistinctManufacturers() {
        logger.debug("Retrieving all distinct manufacturers");
        return productRepository.findAllDistinctManufacturers();
    }

    // ===== PRIVATE HELPER METHODS =====

    private Product findProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PRODUCT_ENTITY_NAME, id));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));
    }

    private void validateProductForCreation(ProductDto productDto) {
        if (productDto.getName() == null) {
            throw new ValidationException("name", "name cannot be null");
        }
        if (!StringUtils.hasText(productDto.getName())) {
            throw new ValidationException("name", "cannot be empty");
        }

        // Validate product name length
        if (productDto.getName().length() > 255) {
            throw new ValidationException("name", "product name too long");
        }

        // Validate invalid characters in product name
        if (productDto.getName().matches(".*[/\\\\|<>].*")) {
            throw new ValidationException("name", "product name contains invalid characters");
        }

        // Validate brand name length
        if (productDto.getBrand() != null && productDto.getBrand().length() > 100) {
            throw new ValidationException("brand", "brand name too long");
        }

        // Validate description length
        if (productDto.getDescription() != null && productDto.getDescription().length() > 5000) {
            throw new ValidationException("description", "description too long");
        }

        if (productDto.getCategoryId() == null) {
            throw new ValidationException("categoryId", "Category is required");
        }

        // Check for duplicate name in the same category
        Optional<Product> existing =
                productRepository.findByNameAndCategoryId(productDto.getName(), productDto.getCategoryId());
        if (existing.isPresent()) {
            throw new ValidationException(
                    "name", "Product with this name already exists in the category");
        }

        // Validate weight if provided
        if (productDto.getWeight() != null && productDto.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("weight", "Weight cannot be negative");
        }
    }

    private void validateProductForUpdate(Product existingProduct, ProductDto productDto) {
        if (!StringUtils.hasText(productDto.getName())) {
            throw new ValidationException("name", "Product name is required");
        }

        if (productDto.getCategoryId() == null) {
            throw new ValidationException("categoryId", "Category is required");
        }

        // Check for duplicate name in the same category (excluding current product)
        boolean nameExists =
                productRepository.existsByNameAndCategoryIdExcludingId(
                        productDto.getName(), productDto.getCategoryId(), existingProduct.getId());
        if (nameExists) {
            throw new ValidationException(
                    "name", "Product with this name already exists in the category");
        }

        // Validate weight if provided
        if (productDto.getWeight() != null && productDto.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("weight", "Weight cannot be negative");
        }
    }

    private void validateCategoryForProduct(Category category) {
        if (!category.getIsActive()) {
            throw new ValidationException("categoryId", "Cannot assign product to inactive category");
        }

        // Validate category depth - assuming max depth is 10
        if (category.getLevel() > 10) {
            throw new ValidationException("categoryId", "category depth exceeds maximum allowed level");
        }
    }

    private void validateProductForDeletion(Product product) {
        if (product.hasActiveSkus()) {
            throw new BusinessException("Cannot delete product with active SKUs");
        }
    }

    private boolean shouldUpdateCategory(Product existingProduct, ProductDto productDto) {
        return !existingProduct.getCategory().getId().equals(productDto.getCategoryId());
    }

    /**
     * Inner class for product statistics.
     */
    public static class ProductStatistics {

        private final long totalProducts;
        private final long activeProducts;
        private final long inactiveProducts;

        public ProductStatistics(long totalProducts, long activeProducts, long inactiveProducts) {
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.inactiveProducts = inactiveProducts;
        }

        public long getTotalProducts() {
            return totalProducts;
        }

        public long getActiveProducts() {
            return activeProducts;
        }

        public long getInactiveProducts() {
            return inactiveProducts;
        }
    }
}
