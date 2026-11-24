package com.bycap.gateway.dto;

import java.time.LocalDateTime;

/**
 * Output DTO — Payment transaction processing result & ledger record.
 */
public record TransactionResponseDTO(
    String transactionId,
    String reference,
    Long amountInCents,
    String currency,
    String status,
    String paymentMethod,
    String customerEmail,
    String customerName,
    String gatewayTransactionId,
    LocalDateTime createdAt
) {}
