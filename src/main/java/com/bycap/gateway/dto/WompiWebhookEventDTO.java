package com.bycap.gateway.dto;

/**
 * Input DTO — Incoming Wompi webhook event payload.
 */
public record WompiWebhookEventDTO(
    String wompiTransactionId,
    String reference,
    String status,
    Long amountInCents,
    String currency,
    long timestamp,
    String checksum
) {}
