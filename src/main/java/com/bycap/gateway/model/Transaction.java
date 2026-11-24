package com.bycap.gateway.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core Domain Entity: Payment Transaction.
 *
 * Encapsulated Business Rules:
 *   - RN-01: Amounts stored in cents (Long) to avoid floating-point rounding errors.
 *   - RN-02: Minimum transaction amount is $1,000 COP = 100,000 cents.
 *   - RN-03: Unique idempotent order reference (e.g., BYCAP-ORD-...).
 *   - RN-04: Strict lifecycle transitions: PENDING -> APPROVED | DECLINED | ERROR.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "amount_in_cents", nullable = false)
    private Long amountInCents;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "payment_method_code", nullable = false, length = 30)
    private String paymentMethodCode;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionStatus {
        PENDING, APPROVED, DECLINED, ERROR
    }

    protected Transaction() {
        // Required by JPA
    }

    public Transaction(UUID id, UUID merchantId, String reference,
                       Long amountInCents, String currency, String paymentMethodCode) {
        this(id, merchantId, reference, amountInCents, currency, paymentMethodCode, "customer@bycap.io", "Guest Customer");
    }

    public Transaction(UUID id, UUID merchantId, String reference,
                       Long amountInCents, String currency, String paymentMethodCode,
                       String customerEmail, String customerName) {

        if (amountInCents == null || amountInCents < 100_000L) {
            throw new IllegalArgumentException(
                "Minimum allowed amount per transaction is $1,000 COP (100,000 cents). " +
                "Received: " + (amountInCents != null ? amountInCents : "null") + " cents.");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null.");
        }
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Transaction reference cannot be empty.");
        }
        if (paymentMethodCode == null || paymentMethodCode.isBlank()) {
            throw new IllegalArgumentException("Payment method code cannot be empty.");
        }

        this.id                = id != null ? id : UUID.randomUUID();
        this.merchantId        = merchantId;
        this.reference         = reference;
        this.amountInCents     = amountInCents;
        this.currency          = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "COP";
        this.status            = TransactionStatus.PENDING;
        this.paymentMethodCode = paymentMethodCode.toUpperCase();
        this.customerEmail     = (customerEmail != null && !customerEmail.isBlank()) ? customerEmail.trim() : "customer@bycap.io";
        this.customerName      = (customerName != null && !customerName.isBlank()) ? customerName.trim() : "Walk-in Customer";
        this.createdAt         = LocalDateTime.now();
    }

    public void markAsApproved() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be approved. Current status: " + this.status);
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void markAsDeclined() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be declined. Current status: " + this.status);
        }
        this.status = TransactionStatus.DECLINED;
    }

    public void markAsError() {
        this.status = TransactionStatus.ERROR;
    }

    public void applyWebhookStatus(TransactionStatus newStatus, String gatewayTxId) {
        this.status = newStatus;
        if (gatewayTxId != null && !gatewayTxId.isBlank()) {
            this.gatewayTransactionId = gatewayTxId;
        }
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getId()                        { return id; }
    public UUID getMerchantId()                { return merchantId; }
    public String getReference()               { return reference; }
    public Long getAmountInCents()             { return amountInCents; }
    public String getCurrency()                { return currency; }
    public TransactionStatus getStatus()       { return status; }
    public String getPaymentMethodCode()       { return paymentMethodCode; }
    public String getCustomerEmail()           { return customerEmail; }
    public String getCustomerName()            { return customerName; }
    public String getGatewayTransactionId()    { return gatewayTransactionId; }
    public LocalDateTime getCreatedAt()        { return createdAt; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%s, ref='%s', amount=%d cents, status=%s, method='%s'}",
                id, reference, amountInCents, status, paymentMethodCode);
    }
}
