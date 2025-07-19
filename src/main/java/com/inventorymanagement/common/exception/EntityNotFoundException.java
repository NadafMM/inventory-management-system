package com.inventorymanagement.common.exception;

/**
 * Exception thrown when a requested entity is not found in the system. This is a specialized business exception for entity lookup failures.
 */
public class EntityNotFoundException extends BusinessException {

    private final String entityType;
    private final Object entityId;

    /**
     * Constructs a new entity not found exception with the specified entity type and ID.
     *
     * @param entityType the type of entity that was not found
     * @param entityId   the ID of the entity that was not found
     */
    public EntityNotFoundException(String entityType, Object entityId) {
        super("ENTITY_NOT_FOUND", String.format("%s with ID %s not found", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Constructs a new entity not found exception with the specified message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
        this.entityType = null;
        this.entityId = null;
    }

    /**
     * Constructs a new entity not found exception with the specified entity type, ID, and message.
     *
     * @param entityType the type of entity that was not found
     * @param entityId   the ID of the entity that was not found
     * @param message    the detail message
     */
    public EntityNotFoundException(String entityType, Object entityId, String message) {
        super("ENTITY_NOT_FOUND", message);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Gets the entity type that was not found.
     *
     * @return the entity type, or null if not set
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the entity ID that was not found.
     *
     * @return the entity ID, or null if not set
     */
    public Object getEntityId() {
        return entityId;
    }
}
