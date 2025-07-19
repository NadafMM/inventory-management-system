package com.inventorymanagement.category.repository;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.common.repository.BaseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Category entity. Provides hierarchical query methods and category-specific operations.
 */
@Repository
public interface CategoryRepository extends BaseRepository<Category, Long> {

    // ===== HIERARCHICAL QUERIES =====

    /**
     * Finds all root categories (categories with no parent).
     *
     * @return list of root categories
     */
    @Query(
            "SELECT c FROM Category c WHERE c.parent IS NULL AND c.deletedAt IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findAllRootCategories();

    /**
     * Finds all root categories with pagination.
     *
     * @param pageable pagination information
     * @return page of root categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.deletedAt IS NULL")
    Page<Category> findAllRootCategories(Pageable pageable);

    /**
     * Finds all children of a specific category.
     *
     * @param parentId the parent category ID
     * @return list of child categories
     */
    @Query(
            "SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deletedAt IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * Finds all children of a specific category with pagination.
     *
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return page of child categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deletedAt IS NULL")
    Page<Category> findByParentId(@Param("parentId") Long parentId, Pageable pageable);

    /**
     * Finds all descendants of a category using path-based query.
     *
     * @param path the category path pattern
     * @return list of descendant categories
     */
    @Query(
            "SELECT c FROM Category c WHERE c.path LIKE CONCAT(:path, '%') AND c.deletedAt IS NULL ORDER BY c.level, c.sortOrder, c.name")
    List<Category> findAllDescendantsByPath(@Param("path") String path);

    /**
     * Finds all categories at a specific level.
     *
     * @param level the category level
     * @return list of categories at the specified level
     */
    @Query(
            "SELECT c FROM Category c WHERE c.level = :level AND c.deletedAt IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findByLevel(@Param("level") Integer level);

    /**
     * Finds all categories at a specific level with pagination.
     *
     * @param level    the category level
     * @param pageable pagination information
     * @return page of categories at the specified level
     */
    @Query("SELECT c FROM Category c WHERE c.level = :level AND c.deletedAt IS NULL")
    Page<Category> findByLevel(@Param("level") Integer level, Pageable pageable);

    /**
     * Finds the complete path from root to a specific category. Uses a simple approach by finding all categories that are ancestors of the target.
     *
     * @param categoryId the target category ID
     * @return list of categories from root to target
     */
    default List<Category> findPathToCategory(Long categoryId) {
        List<Category> path = new ArrayList<>();
        Optional<Category> current = findById(categoryId);

        while (current.isPresent() && current.get().getDeletedAt() == null) {
            path.add(0, current.get()); // Add to beginning to maintain order from root to target
            if (current.get().getParent() != null) {
                current = findById(current.get().getParent().getId());
            } else {
                break;
            }
        }

        return path;
    }

    // ===== SEARCH AND FILTER QUERIES =====

    /**
     * Searches categories by name (case-insensitive partial match).
     *
     * @param name the search term
     * @return list of matching categories
     */
    @Query(
            "SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Searches categories by name with pagination.
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return page of matching categories
     */
    @Query(
            "SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.deletedAt IS NULL")
    Page<Category> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Searches categories by name or description.
     *
     * @param searchTerm the search term
     * @return list of matching categories
     */
    @Query(
            "SELECT c FROM Category c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Searches categories by name or description with pagination.
     *
     * @param searchTerm the search term
     * @param pageable   pagination information
     * @return page of matching categories
     */
    @Query(
            "SELECT c FROM Category c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND c.deletedAt IS NULL")
    Page<Category> searchByNameOrDescription(
            @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Finds categories by active status.
     *
     * @param isActive the active status
     * @return list of categories with the specified status
     */
    @Query(
            "SELECT c FROM Category c WHERE c.isActive = :isActive AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> findByIsActive(@Param("isActive") Boolean isActive);

    /**
     * Finds categories by active status with pagination.
     *
     * @param isActive the active status
     * @param pageable pagination information
     * @return page of categories with the specified status
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = :isActive AND c.deletedAt IS NULL")
    Page<Category> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    // ===== BUSINESS LOGIC QUERIES =====

    /**
     * Finds categories that have products.
     *
     * @return list of categories with products
     */
    @Query(
            "SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.deletedAt IS NULL AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> findCategoriesWithProducts();

    /**
     * Finds categories that have no products.
     *
     * @return list of categories without products
     */
    @Query(
            "SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT p.category.id FROM Product p WHERE p.deletedAt IS NULL) AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> findCategoriesWithoutProducts();

    /**
     * Finds leaf categories (categories with no children).
     *
     * @return list of leaf categories
     */
    @Query(
            "SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT parent.id FROM Category parent WHERE parent.deletedAt IS NULL) AND c.deletedAt IS NULL ORDER BY c.name")
    List<Category> findLeafCategories();

    /**
     * Finds categories with children count.
     *
     * @return list of categories with their children count
     */
    @Query(
            "SELECT c, COUNT(child) as childCount FROM Category c LEFT JOIN c.children child WHERE c.deletedAt IS NULL AND (child.deletedAt IS NULL OR child IS NULL) GROUP BY c ORDER BY c.name")
    List<Object[]> findCategoriesWithChildrenCount();

    /**
     * Finds categories with product count.
     *
     * @return list of categories with their product count
     */
    @Query(
            "SELECT c, COUNT(p) as productCount FROM Category c LEFT JOIN c.products p WHERE c.deletedAt IS NULL AND (p.deletedAt IS NULL OR p IS NULL) GROUP BY c ORDER BY c.name")
    List<Object[]> findCategoriesWithProductCount();

    /**
     * Counts children of a specific category.
     *
     * @param parentId the parent category ID
     * @return number of children
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId AND c.deletedAt IS NULL")
    long countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Counts products in a specific category.
     *
     * @param categoryId the category ID
     * @return number of products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Finds categories by exact name (case-sensitive).
     *
     * @param name the category name
     * @return optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.deletedAt IS NULL")
    Optional<Category> findByName(@Param("name") String name);

    /**
     * Finds categories by exact name and parent (case-sensitive).
     *
     * @param name     the category name
     * @param parentId the parent category ID
     * @return optional containing the category if found
     */
    @Query(
            "SELECT c FROM Category c WHERE c.name = :name AND c.parent.id = :parentId AND c.deletedAt IS NULL")
    Optional<Category> findByNameAndParentId(
            @Param("name") String name, @Param("parentId") Long parentId);

    /**
     * Finds categories by exact name and no parent (root level).
     *
     * @param name the category name
     * @return optional containing the category if found
     */
    @Query(
            "SELECT c FROM Category c WHERE c.name = :name AND c.parent IS NULL AND c.deletedAt IS NULL")
    Optional<Category> findRootByName(@Param("name") String name);

    /**
     * Checks if a category name exists at the same level.
     *
     * @param name      the category name
     * @param parentId  the parent category ID (null for root level)
     * @param excludeId the category ID to exclude from the check
     * @return true if name exists
     */
    @Query(
            "SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND (:parentId IS NULL AND c.parent IS NULL OR c.parent.id = :parentId) AND c.id != :excludeId AND c.deletedAt IS NULL")
    boolean existsByNameAndParentIdExcludingId(
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("excludeId") Long excludeId);

    /**
     * Finds the maximum sort order for categories at the same level.
     *
     * @param parentId the parent category ID (null for root level)
     * @return maximum sort order
     */
    @Query(
            "SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE (:parentId IS NULL AND c.parent IS NULL OR c.parent.id = :parentId) AND c.deletedAt IS NULL")
    Integer findMaxSortOrderByParentId(@Param("parentId") Long parentId);

    /**
     * Advanced search with multiple filter criteria.
     *
     * @param name     filter by name (null to ignore)
     * @param parentId filter by parent ID (null to ignore)
     * @param level    filter by level (null to ignore)
     * @param isActive filter by active status (null to ignore)
     * @param pageable pagination information
     * @return page of matching categories
     */
    @Query(
            "SELECT c FROM Category c WHERE "
                    + "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
                    + "(:parentId IS NULL OR c.parent.id = :parentId) AND "
                    + "(:level IS NULL OR c.level = :level) AND "
                    + "(:isActive IS NULL OR c.isActive = :isActive) AND "
                    + "c.deletedAt IS NULL")
    Page<Category> findWithFilters(
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("level") Integer level,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Finds all active categories ordered by path for hierarchy display.
     *
     * @return list of active categories ordered by path
     */
    @Query(
            "SELECT c FROM Category c WHERE c.isActive = true AND c.deletedAt IS NULL ORDER BY c.path, c.sortOrder")
    List<Category> findAllActiveOrderedByPath();
}
