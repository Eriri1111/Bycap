package com.bycap.gateway.dto;

public record WompiWebhookEventDTO(
    String wompiTransactionId,
    String reference,
    String status,
    Long amountInCents,
    String currency,
    long timestamp,
    String checksum
) {}
