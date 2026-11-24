package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;

/**
 * Strategy Interface — Payment Methods.
 *
 * Defines the contract for all payment adapters (CARD, NFC, PSE, NEQUI).
 * Adding a new payment method only requires creating a new @Component implementing
 * this interface — zero changes needed in ProcessPaymentService (Open/Closed Principle).
 */
public interface PaymentStrategy {
    String getPaymentMethodIdentifier();
    TransactionResponseDTO processPayment(Transaction transaction, String token);
}
