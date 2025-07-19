package com.inventorymanagement.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class for logging setup.
 *
 * <p>This configuration enables: - AspectJ auto-proxy for performance logging - Provides utility
 * methods for different log types - Centralizes logging configuration
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {

    private static final Logger auditLogger = LoggerFactory.getLogger("audit");
    private static final Logger performanceLogger = LoggerFactory.getLogger("performance");

    /**
     * Logs audit events with correlation ID and user context.
     *
     * @param action   the action being performed
     * @param resource the resource being acted upon
     * @param userId   the user performing the action (can be null for anonymous)
     * @param details  additional details about the action
     */
    public static void logAuditEvent(String action, String resource, String userId, String details) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();

        String auditMessage =
                String.format(
                        "AUDIT|%s|%s|%s|%s|%s|%d",
                        action,
                        resource,
                        userId != null ? userId : "anonymous",
                        correlationId != null ? correlationId : "N/A",
                        details != null ? details : "",
                        System.currentTimeMillis());

        auditLogger.info(auditMessage);
    }

    /**
     * Logs audit events for entity operations.
     *
     * @param operation  CRUD operation (CREATE, READ, UPDATE, DELETE)
     * @param entityType type of entity (Category, Product, SKU, etc.)
     * @param entityId   ID of the entity
     * @param userId     user performing the operation
     */
    public static void logEntityAudit(
            String operation, String entityType, Object entityId, String userId) {
        String details = String.format("entityId:%s", entityId != null ? entityId.toString() : "N/A");
        logAuditEvent(operation, entityType, userId, details);
    }

    /**
     * Logs security-related events.
     *
     * @param event     security event type
     * @param userId    user involved in the event
     * @param ipAddress IP address (optional)
     * @param details   additional security details
     */
    public static void logSecurityEvent(
            String event, String userId, String ipAddress, String details) {
        String resource = "SECURITY";
        String securityDetails =
                String.format(
                        "ip:%s|%s", ipAddress != null ? ipAddress : "unknown", details != null ? details : "");
        logAuditEvent(event, resource, userId, securityDetails);
    }

    /**
     * Logs business rule violations or important business events.
     *
     * @param event   business event type
     * @param context business context
     * @param details event details
     */
    public static void logBusinessEvent(String event, String context, String details) {
        logAuditEvent(event, "BUSINESS", "system", String.format("context:%s|%s", context, details));
    }

    /**
     * Example usage demonstrating different audit log types.
     */
    public static class AuditExamples {

        public static void logCategoryCreation(Long categoryId, String userId) {
            logEntityAudit("CREATE", "Category", categoryId, userId);
        }

        public static void logProductUpdate(Long productId, String userId, String changes) {
            String details = String.format("entityId:%s|changes:%s", productId, changes);
            logAuditEvent("UPDATE", "Product", userId, details);
        }

        public static void logInventoryAdjustment(
                Long skuId, int quantity, String reason, String userId) {
            String details = String.format("entityId:%s|quantity:%d|reason:%s", skuId, quantity, reason);
            logAuditEvent("INVENTORY_ADJUST", "SKU", userId, details);
        }

        public static void logUnauthorizedAccess(String endpoint, String ipAddress) {
            logSecurityEvent("UNAUTHORIZED_ACCESS", "anonymous", ipAddress, "endpoint:" + endpoint);
        }

        public static void logDataExport(String exportType, String userId, int recordCount) {
            String details = String.format("type:%s|records:%d", exportType, recordCount);
            logAuditEvent("DATA_EXPORT", "SYSTEM", userId, details);
        }
    }
}
