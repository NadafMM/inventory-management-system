package com.inventorymanagement.common.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

/**
 * Base repository interface providing common functionality for all entities. Includes soft delete support and common query methods.
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Finds all active (non-deleted) entities.
     *
     * @return list of active entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    List<T> findAllActive();

    /**
     * Finds all active (non-deleted) entities with pagination.
     *
     * @param pageable pagination information
     * @return page of active entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    Page<T> findAllActive(Pageable pageable);

    /**
     * Finds an active (non-deleted) entity by ID.
     *
     * @param id the entity ID
     * @return optional containing the entity if found and active
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<T> findActiveById(@Param("id") ID id);

    /**
     * Checks if an active entity exists by ID.
     *
     * @param id the entity ID
     * @return true if an active entity exists with the given ID
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    boolean existsActiveById(@Param("id") ID id);

    /**
     * Counts all active (non-deleted) entities.
     *
     * @return count of active entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    long countActive();

    /**
     * Soft deletes an entity by setting the deletedAt timestamp.
     *
     * @param id the entity ID to soft delete
     * @return number of entities updated
     */
    @Modifying
    @Query(
            "UPDATE #{#entityName} e SET e.deletedAt = :deletedAt, e.updatedAt = :updatedAt WHERE e.id ="
                    + " :id")
    int softDeleteById(
            @Param("id") ID id,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Restores a soft-deleted entity by clearing the deletedAt timestamp.
     *
     * @param id the entity ID to restore
     * @return number of entities updated
     */
    @Modifying
    @Query(
            "UPDATE #{#entityName} e SET e.deletedAt = NULL, e.updatedAt = :updatedAt WHERE e.id = :id")
    int restoreById(@Param("id") ID id, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Finds all soft-deleted entities.
     *
     * @return list of soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    List<T> findAllDeleted();

    /**
     * Finds all soft-deleted entities with pagination.
     *
     * @param pageable pagination information
     * @return page of soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NOT NULL")
    Page<T> findAllDeleted(Pageable pageable);

    /**
     * Finds entities created after a specific date.
     *
     * @param date the date to search from
     * @return list of entities created after the date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt > :date AND e.deletedAt IS NULL")
    List<T> findActiveCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Finds entities updated after a specific date.
     *
     * @param date the date to search from
     * @return list of entities updated after the date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt > :date AND e.deletedAt IS NULL")
    List<T> findActiveUpdatedAfter(@Param("date") LocalDateTime date);
}
