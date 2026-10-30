package com.bycap.gateway.model;

import java.util.UUID;

/**
 * Entidad principal: Transacción de Pago.
 *
 * Reglas de Negocio encapsuladas:
 *   - RN-01: Montos en centavos (Long) para evitar errores de punto flotante.
 *   - RN-02: Monto mínimo $1.000 COP = 100.000 centavos.
 *   - RN-03: Referencia única e idempotente generada externamente.
 *   - RN-04: Ciclo de vida estricto: PENDING → APPROVED | DECLINED | ERROR.
 */
public class Transaction {

    private final UUID id;
    private final UUID merchantId;
    private final String reference;
    private final Long amountInCents;
    private final String currency;
    private TransactionStatus status;
    private final String paymentMethodCode;

    public enum TransactionStatus {
        PENDING, APPROVED, DECLINED, ERROR
    }

    public Transaction(UUID id, UUID merchantId, String reference,
                       Long amountInCents, String currency, String paymentMethodCode) {

        if (amountInCents == null || amountInCents < 100_000L) {
            throw new IllegalArgumentException(
                "El monto mínimo permitido por transacción es de $1.000 COP (100.000 centavos). " +
                "Monto recibido: " + (amountInCents != null ? amountInCents : "nulo") + " centavos.");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser nulo.");
        }
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("La referencia de la transacción no puede estar vacía.");
        }
        if (paymentMethodCode == null || paymentMethodCode.isBlank()) {
            throw new IllegalArgumentException("El código del método de pago no puede estar vacío.");
        }

        this.id                = id != null ? id : UUID.randomUUID();
        this.merchantId        = merchantId;
        this.reference         = reference;
        this.amountInCents     = amountInCents;
        this.currency          = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "COP";
        this.status            = TransactionStatus.PENDING;
        this.paymentMethodCode = paymentMethodCode.toUpperCase();
    }

    public void markAsApproved() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden aprobar transacciones en estado PENDING. Estado actual: " + this.status);
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void markAsDeclined() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden rechazar transacciones en estado PENDING. Estado actual: " + this.status);
        }
        this.status = TransactionStatus.DECLINED;
    }

    public void markAsError() { this.status = TransactionStatus.ERROR; }

    public UUID getId()                  { return id; }
    public UUID getMerchantId()          { return merchantId; }
    public String getReference()         { return reference; }
    public Long getAmountInCents()       { return amountInCents; }
    public String getCurrency()          { return currency; }
    public TransactionStatus getStatus() { return status; }
    public String getPaymentMethodCode() { return paymentMethodCode; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%s, ref='%s', amount=%d centavos, status=%s, method='%s'}",
                id, reference, amountInCents, status, paymentMethodCode);
    }
}
