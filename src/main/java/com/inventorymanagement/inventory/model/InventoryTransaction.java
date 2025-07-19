package com.inventorymanagement.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing inventory transactions (stock movements). Provides complete audit trail for all inventory changes.
 */
@Entity
@Table(
        name = "inventory_transactions",
        indexes = {
                @Index(name = "idx_transaction_sku", columnList = "sku_id"),
                @Index(name = "idx_transaction_type", columnList = "transaction_type"),
                @Index(name = "idx_transaction_date", columnList = "created_at"),
                @Index(name = "idx_transaction_reference", columnList = "reference_id,reference_type")
        })
@SuppressWarnings("DesignForExtension")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Size(max = 100, message = "Reference ID must not exceed 100 characters")
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Size(max = 50, message = "Reference type must not exceed 50 characters")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    @Column(name = "reason")
    private String reason;

    @Size(max = 100, message = "Performed by must not exceed 100 characters")
    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public InventoryTransaction() {}

    public InventoryTransaction(Sku sku, TransactionType transactionType, Integer quantity) {
        this.sku = sku;
        this.transactionType = transactionType;
        this.quantity = quantity;
    }

    public InventoryTransaction(
            Sku sku,
            TransactionType transactionType,
            Integer quantity,
            String referenceId,
            String referenceType,
            String reason,
            String performedBy) {
        this.sku = sku;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Business methods

    /**
     * Checks if this transaction increases stock.
     *
     * @return true if transaction type increases stock
     */
    public boolean isStockIncrease() {
        return transactionType == TransactionType.IN
                || (transactionType == TransactionType.ADJUSTMENT && quantity > 0);
    }

    /**
     * Checks if this transaction decreases stock.
     *
     * @return true if transaction type decreases stock
     */
    public boolean isStockDecrease() {
        return transactionType == TransactionType.OUT
                || (transactionType == TransactionType.ADJUSTMENT && quantity < 0);
    }

    /**
     * Checks if this transaction is a reservation.
     *
     * @return true if transaction type is reservation
     */
    public boolean isReservation() {
        return transactionType == TransactionType.RESERVED;
    }

    /**
     * Checks if this transaction is a release.
     *
     * @return true if transaction type is release
     */
    public boolean isRelease() {
        return transactionType == TransactionType.RELEASED;
    }

    /**
     * Gets the absolute quantity value.
     *
     * @return absolute quantity
     */
    public Integer getAbsoluteQuantity() {
        return Math.abs(quantity);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sku getSku() {
        return sku;
    }

    public void setSku(Sku sku) {
        this.sku = sku;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InventoryTransaction that = (InventoryTransaction) obj;
        return Objects.equals(id, that.id)
                && Objects.equals(sku, that.sku)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku, createdAt);
    }

    @Override
    public String toString() {
        return "InventoryTransaction{"
                + "id="
                + id
                + ", sku="
                + (sku != null ? sku.getSkuCode() : null)
                + ", transactionType="
                + transactionType
                + ", quantity="
                + quantity
                + ", referenceId='"
                + referenceId
                + '\''
                + ", createdAt="
                + createdAt
                + '}';
    }

    /**
     * Enum representing different types of inventory transactions.
     */
    @SuppressWarnings("DesignForExtension")
    public enum TransactionType {
        IN("Stock In"),
        OUT("Stock Out"),
        ADJUSTMENT("Adjustment"),
        RESERVED("Reserved"),
        RELEASED("Released");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
