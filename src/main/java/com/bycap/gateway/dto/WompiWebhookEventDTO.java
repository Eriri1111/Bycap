package com.bycap.gateway.dto;

/**
 * DTO de entrada con los datos de un evento de transacción enviado por Wompi.
 *
 * @param wompiTransactionId ID de la transacción en Wompi.
 * @param reference          referencia única de Bycap (BYCAP-ORD-...).
 * @param status             estado reportado por Wompi: APPROVED, DECLINED o ERROR.
 * @param amountInCents      monto en centavos.
 * @param currency           moneda, siempre COP.
 * @param timestamp          fecha del evento en segundos Unix.
 * @param checksum           firma SHA-256 recibida, en hexadecimal.
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
