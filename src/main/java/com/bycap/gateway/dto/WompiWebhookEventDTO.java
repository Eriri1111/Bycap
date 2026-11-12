package com.bycap.gateway.dto;

/**
 * Input DTO: a transaction event sent by Wompi through a webhook.
 *
 * Holds only the fields needed to verify the SHA-256 signature (RN-05)
 * and to locate the Bycap transaction it refers to.
 *
 * @param wompiTransactionId Transaction ID assigned by Wompi (e.g. "12345-1698182938-98231").
 * @param reference          Bycap unique reference (e.g. "BYCAP-ORD-1700000000000").
 * @param status             Status reported by Wompi: "APPROVED", "DECLINED" or "ERROR".
 * @param amountInCents      Amount in cents (RN-01).
 * @param currency           ISO 4217 currency code, always "COP" (RN-02).
 * @param timestamp          Event timestamp in Unix seconds.
 * @param checksum           SHA-256 checksum sent by Wompi, in hexadecimal.
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
