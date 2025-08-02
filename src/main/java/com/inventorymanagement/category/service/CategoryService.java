package com.inventorymanagement.category.service;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.category.model.CategoryMapper;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * Service class for managing Category entities and business operations. Provides comprehensive category management including hierarchical operations,
 * validation, and caching.
 */
@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private static final String CATEGORY_ENTITY_NAME = "Category";
    private static final int MAX_CATEGORY_DEPTH = 10;

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = null;
    }

    // ===== CRUD OPERATIONS =====

    /**
     * Creates a new category with business rule validation.
     *
     * @param categoryDto the category data
     * @return the created category DTO
     * @throws ValidationException if validation fails
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public CategoryDto createCategory(@Valid @NotNull CategoryDto categoryDto) {
        logger.info("Creating new category: {}", categoryDto.getName());

        // Trim whitespace from name
        if (categoryDto.getName() != null) {
            categoryDto.setName(categoryDto.getName().trim());
        }

        validateCategoryForCreation(categoryDto);

        Category category = CategoryMapper.toEntity(categoryDto);

        // Set up hierarchy if parent is specified
        if (categoryDto.getParentId() != null) {
            Category parent = findCategoryById(categoryDto.getParentId());
            validateParentCategory(parent);
            setupCategoryHierarchy(category, parent);
        } else {
            // Root category setup
            category.setLevel(0);
            category.setPath("/temp/"); // Temporary path for validation
        }

        Category savedCategory = categoryRepository.save(category);

        // Update path after saving to get the ID
        if (savedCategory.getParent() == null) {
            savedCategory.setPath("/" + savedCategory.getId() + "/");
        } else {
            // Fetch the parent fresh from database to ensure we have the latest path
            Category parent =
                    categoryRepository.findById(savedCategory.getParent().getId()).orElseThrow();
            savedCategory.setPath(parent.getPath() + savedCategory.getId() + "/");
        }
        savedCategory = categoryRepository.save(savedCategory);

        logger.info("Successfully created category with ID: {}", savedCategory.getId());
        return CategoryMapper.toDto(savedCategory);
    }

    /**
     * Updates an existing category with business rule validation.
     *
     * @param id          the category ID
     * @param categoryDto the updated category data
     * @return the updated category DTO
     * @throws EntityNotFoundException if category not found
     * @throws ValidationException     if validation fails
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public CategoryDto updateCategory(@NotNull Long id, @Valid @NotNull CategoryDto categoryDto) {
        logger.info("Updating category with ID: {}", id);

        Category existingCategory = findCategoryById(id);
        validateCategoryForUpdate(existingCategory, categoryDto);

        // Update basic fields - only update non-null fields for partial updates
        if (categoryDto.getName() != null) {
            existingCategory.setName(categoryDto.getName());
        }
        if (categoryDto.getDescription() != null) {
            existingCategory.setDescription(categoryDto.getDescription());
        }
        if (categoryDto.getSortOrder() != null) {
            existingCategory.setSortOrder(categoryDto.getSortOrder());
        }
        if (categoryDto.getMetadata() != null) {
            existingCategory.setMetadata(categoryDto.getMetadata());
        }

        // Handle parent change
        if (shouldUpdateParent(existingCategory, categoryDto)) {
            updateCategoryParent(existingCategory, categoryDto.getParentId());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        logger.info("Successfully updated category with ID: {}", id);

        return CategoryMapper.toDto(updatedCategory);
    }

    /**
     * Retrieves a category by ID.
     *
     * @param id the category ID
     * @return the category DTO
     * @throws EntityNotFoundException if category not found
     */
    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(@NotNull Long id) {
        logger.debug("Retrieving category with ID: {}", id);
        Category category = findCategoryById(id);
        return CategoryMapper.toDto(category);
    }

    /**
     * Retrieves all categories with pagination.
     *
     * @param pageable pagination information
     * @return page of category DTOs
     */
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        logger.debug("Retrieving all categories with pagination");
        Page<Category> categories = categoryRepository.findAllActive(pageable);
        return categories.map(CategoryMapper::toDto);
    }

    /**
     * Soft deletes a category and all its children.
     *
     * @param id the category ID
     * @throws EntityNotFoundException if category not found
     * @throws BusinessException       if category has products
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public void deleteCategory(@NotNull Long id) {
        logger.info("Deleting category with ID: {}", id);

        Category category = findCategoryById(id);
        validateCategoryForDeletion(category);

        // Soft delete the category and all its children
        softDeleteCategoryTree(category);

        logger.info("Successfully deleted category with ID: {}", id);
    }

    // ===== HIERARCHICAL OPERATIONS =====

    /**
     * Retrieves all root categories (categories with no parent).
     *
     * @return list of root category DTOs
     */
    @Cacheable(value = "categoryHierarchy", key = "'rootCategories'")
    public List<CategoryDto> getRootCategories() {
        logger.debug("Retrieving all root categories");
        List<Category> rootCategories = categoryRepository.findAllRootCategories();
        return rootCategories.stream().map(CategoryMapper::toDto).toList();
    }

    /**
     * Retrieves all children of a specific category.
     *
     * @param parentId the parent category ID
     * @return list of child category DTOs
     * @throws EntityNotFoundException if parent category not found
     */
    @Cacheable(value = "categoryHierarchy", key = "'children:' + #parentId")
    public List<CategoryDto> getChildCategories(@NotNull Long parentId) {
        logger.debug("Retrieving children for category ID: {}", parentId);

        // Verify parent exists
        findCategoryById(parentId);

        List<Category> children = categoryRepository.findByParentId(parentId);
        return children.stream().map(CategoryMapper::toDto).toList();
    }

    /**
     * Retrieves the complete category path from root to the specified category.
     *
     * @param categoryId the target category ID
     * @return list of category DTOs representing the path
     * @throws EntityNotFoundException if category not found
     */
    @Cacheable(value = "categoryHierarchy", key = "'path:' + #categoryId")
    public List<CategoryDto> getCategoryPath(@NotNull Long categoryId) {
        logger.debug("Retrieving category path for ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        List<Category> pathCategories = categoryRepository.findPathToCategory(categoryId);

        return pathCategories.stream().map(CategoryMapper::toDto).toList();
    }

    /**
     * Moves a category to a new parent.
     *
     * @param categoryId  the category to move
     * @param newParentId the new parent category ID (null for root)
     * @throws EntityNotFoundException if category or parent not found
     * @throws BusinessException       if move would create a cycle
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public void moveCategoryToParent(@NotNull Long categoryId, Long newParentId) {
        logger.info("Moving category {} to parent {}", categoryId, newParentId);

        Category category = findCategoryById(categoryId);

        if (newParentId != null) {
            Category newParent = findCategoryById(newParentId);
            validateCategoryMove(category, newParent);
            setupCategoryHierarchy(category, newParent);
        } else {
            // Moving to root level
            category.setParent(null);
            category.setLevel(0);
            category.setPath("/" + category.getId() + "/");
        }

        // Update all descendant paths
        updateDescendantPaths(category);

        categoryRepository.save(category);
        logger.info("Successfully moved category {} to parent {}", categoryId, newParentId);
    }

    // ===== SEARCH AND FILTER OPERATIONS =====

    /**
     * Searches categories by name (case-insensitive partial match).
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return page of matching category DTOs
     */
    public Page<CategoryDto> searchCategoriesByName(@NotNull String name, Pageable pageable) {
        logger.debug("Searching categories by name: {}", name);

        if (!StringUtils.hasText(name)) {
            throw new ValidationException("name", "Search term cannot be empty");
        }

        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        return categories.map(CategoryMapper::toDto);
    }

    /**
     * Retrieves categories by active status.
     *
     * @param isActive the active status
     * @param pageable pagination information
     * @return page of category DTOs
     */
    public Page<CategoryDto> getCategoriesByStatus(boolean isActive, Pageable pageable) {
        logger.debug("Retrieving categories by status: {}", isActive);
        Page<Category> categories = categoryRepository.findByIsActive(isActive, pageable);
        return categories.map(CategoryMapper::toDto);
    }

    /**
     * Searches categories with multiple filter criteria.
     *
     * @param name     filter by name (optional)
     * @param parentId filter by parent ID (optional)
     * @param level    filter by level (optional)
     * @param isActive filter by active status (optional)
     * @param pageable pagination information
     * @return page of matching category DTOs
     */
    public Page<CategoryDto> searchCategories(
            String name, Long parentId, Integer level, Boolean isActive, Pageable pageable) {
        logger.debug(
                "Searching categories with filters - name: {}, parentId: {}, level: {}, isActive: {}",
                name,
                parentId,
                level,
                isActive);

        Page<Category> categories =
                categoryRepository.findWithFilters(name, parentId, level, isActive, pageable);
        return categories.map(CategoryMapper::toDto);
    }

    /**
     * Retrieves category hierarchy as a tree structure.
     *
     * @param rootId the root category ID (optional, null for all root categories)
     * @return list of category DTOs with hierarchical structure
     */
    @Cacheable(
            value = "categoryHierarchy",
            key = "'hierarchy:' + (#rootId != null ? #rootId : 'all')")
    public List<CategoryDto> getCategoryHierarchy(Long rootId) {
        logger.debug("Retrieving category hierarchy with root ID: {}", rootId);

        List<Category> categories;
        if (rootId != null) {
            // Get hierarchy starting from specific root
            Category rootCategory = findCategoryById(rootId);
            categories = categoryRepository.findAllDescendantsByPath(rootCategory.getPath());
        } else {
            // Get all root categories and their descendants
            categories = categoryRepository.findAllActiveOrderedByPath();
        }

        return categories.stream().map(CategoryMapper::toDto).toList();
    }

    // ===== BUSINESS LOGIC METHODS =====

    /**
     * Activates a category and optionally its children.
     *
     * @param id              the category ID
     * @param includeChildren whether to activate children as well
     * @throws EntityNotFoundException if category not found
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public void activateCategory(@NotNull Long id, boolean includeChildren) {
        logger.info("Activating category with ID: {} (includeChildren: {})", id, includeChildren);

        Category category = findCategoryById(id);
        category.activate();

        if (includeChildren) {
            activateChildrenRecursively(category);
        }

        categoryRepository.save(category);
        logger.info("Successfully activated category with ID: {}", id);
    }

    /**
     * Deactivates a category and all its children.
     *
     * @param id the category ID
     * @throws EntityNotFoundException if category not found
     */
    @CacheEvict(
            value = {"categories", "categoryHierarchy"},
            allEntries = true)
    public void deactivateCategory(@NotNull Long id) {
        logger.info("Deactivating category with ID: {}", id);

        Category category = findCategoryById(id);
        category.deactivate(); // This deactivates children as well

        categoryRepository.save(category);
        logger.info("Successfully deactivated category with ID: {}", id);
    }

    // ===== PRIVATE HELPER METHODS =====

    private Category findCategoryById(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_ENTITY_NAME, id));
    }

    private void validateCategoryForCreation(CategoryDto categoryDto) {
        if (!StringUtils.hasText(categoryDto.getName())) {
            throw new ValidationException("name", "Category name is required");
        }

        // Validate name length
        if (categoryDto.getName().length() > 255) {
            throw new ValidationException("name", "Category name is too long");
        }

        // Check for invalid characters
        if (categoryDto.getName().contains("<")
                || categoryDto.getName().contains(">")
                || categoryDto.getName().contains("&")
                || categoryDto.getName().contains("\"")
                || categoryDto.getName().contains("'")
                || categoryDto.getName().contains("/")
                || categoryDto.getName().contains("\\")) {
            throw new ValidationException("name", "Category name contains invalid characters");
        }

        // Validate sort order boundaries
        if (categoryDto.getSortOrder() != null && categoryDto.getSortOrder() < 0) {
            throw new ValidationException("sortOrder", "Sort order must be non-negative");
        }

        // Check for duplicate name at the same level
        if (categoryDto.getParentId() != null) {
            Optional<Category> existing =
                    categoryRepository.findByNameAndParentId(
                            categoryDto.getName(), categoryDto.getParentId());
            if (existing.isPresent()) {
                throw new ValidationException("name", "Category name already exists at this level");
            }
        } else {
            Optional<Category> existing = categoryRepository.findRootByName(categoryDto.getName());
            if (existing.isPresent()) {
                throw new ValidationException("name", "Root category with this name already exists");
            }
        }
    }

    private void validateCategoryForUpdate(Category existingCategory, CategoryDto categoryDto) {
        if (!StringUtils.hasText(categoryDto.getName())) {
            throw new ValidationException("name", "Category name is required");
        }

        // Check for duplicate name at the same level (excluding current category)
        if (categoryDto.getParentId() != null) {
            boolean nameExists =
                    categoryRepository.existsByNameAndParentIdExcludingId(
                            categoryDto.getName(), categoryDto.getParentId(), existingCategory.getId());
            if (nameExists) {
                throw new ValidationException("name", "Category name already exists at this level");
            }
        } else {
            Optional<Category> existing = categoryRepository.findRootByName(categoryDto.getName());
            if (existing.isPresent() && !existing.get().getId().equals(existingCategory.getId())) {
                throw new ValidationException("name", "Root category with this name already exists");
            }
        }
    }

    private void validateParentCategory(Category parent) {
        if (!parent.getIsActive()) {
            throw new BusinessException("Cannot create category under inactive parent");
        }

        if (parent.getLevel() >= MAX_CATEGORY_DEPTH) {
            throw new ValidationException("parentId", "Maximum category depth exceeded");
        }
    }

    private void validateCategoryForDeletion(Category category) {
        // Check for products directly in this category
        long productCount = categoryRepository.countProductsByCategoryId(category.getId());
        if (productCount > 0) {
            throw new BusinessException("Cannot delete category with existing products");
        }

        // Check for products in any descendant categories
        List<Category> descendants = categoryRepository.findAllDescendantsByPath(category.getPath());
        for (Category descendant : descendants) {
            if (!descendant.getId().equals(category.getId())) {
                long descendantProductCount =
                        categoryRepository.countProductsByCategoryId(descendant.getId());
                if (descendantProductCount > 0) {
                    throw new BusinessException(
                            "Cannot delete category with products in descendant categories");
                }
            }
        }
    }

    private void validateCategoryMove(Category category, Category newParent) {
        // Check if trying to set parent as self
        if (category.getId().equals(newParent.getId())) {
            throw new ValidationException("parentId", "Category cannot be its own parent");
        }

        // Check if move would create a cycle
        if (isAncestor(category, newParent)) {
            throw new ValidationException("parentId", "Cannot move category to its own descendant");
        }

        validateParentCategory(newParent);
    }

    private boolean isAncestor(Category potentialAncestor, Category category) {
        Category current = category.getParent();
        while (current != null) {
            if (current.getId().equals(potentialAncestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void setupCategoryHierarchy(Category category, Category parent) {
        category.setParent(parent);
        category.setLevel(parent.getLevel() + 1);
        // Set temporary path for validation, will be updated after saving when we have
        // the ID
        if (category.getId() != null) {
            category.setPath(parent.getPath() + category.getId() + "/");
        } else {
            category.setPath("/temp/"); // Temporary path for validation
        }
    }

    private boolean shouldUpdateParent(Category existingCategory, CategoryDto categoryDto) {
        Long currentParentId =
                existingCategory.getParent() != null ? existingCategory.getParent().getId() : null;
        return !java.util.Objects.equals(currentParentId, categoryDto.getParentId());
    }

    private void updateCategoryParent(Category category, Long newParentId) {
        if (newParentId != null) {
            Category newParent = findCategoryById(newParentId);
            validateCategoryMove(category, newParent);
            setupCategoryHierarchy(category, newParent);
        } else {
            category.setParent(null);
            category.setLevel(0);
            category.setPath("/" + category.getId() + "/");
        }

        updateDescendantPaths(category);
    }

    private void updateDescendantPaths(Category category) {
        List<Category> descendants = categoryRepository.findAllDescendantsByPath(category.getPath());
        for (Category descendant : descendants) {
            if (!descendant.getId().equals(category.getId())) {
                // Recalculate path based on parent
                Category parent = descendant.getParent();
                if (parent != null) {
                    descendant.setPath(parent.getPath() + descendant.getId() + "/");
                    descendant.setLevel(parent.getLevel() + 1);
                }
            }
        }
    }

    private void softDeleteCategoryTree(Category category) {
        category.markAsDeleted();

        // Recursively soft delete all children
        List<Category> children = categoryRepository.findByParentId(category.getId());
        for (Category child : children) {
            softDeleteCategoryTree(child);
        }
    }

    private void activateChildrenRecursively(Category category) {
        List<Category> children = categoryRepository.findByParentId(category.getId());
        for (Category child : children) {
            child.activate();
            activateChildrenRecursively(child);
        }
    }
}
