package com.bycap.gateway.dto;

import java.util.UUID;

/**
 * Input DTO — Creating a new payment transaction.
 */
public record CreateTransactionDTO(
    UUID merchantId,
    Long amountInCents,
    String paymentMethodCode,
    String customerEmail,
    String customerName,
    String token
) {}
