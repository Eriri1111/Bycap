package com.bycap.gateway.dto;

/**
 * DTO de Salida — Resultado del procesamiento de una transacción.
 *
 * @param transactionId UUID de la transacción como String.
 * @param reference     Referencia única (ej. "BYCAP-ORD-1700000000000").
 * @param amountInCents Monto procesado en centavos.
 * @param status        Estado final: "APPROVED", "DECLINED" o "ERROR".
 * @param paymentMethod Código del método utilizado: "CARD", "NFC", etc.
 */
public record TransactionResponseDTO(
    String transactionId,
    String reference,
    Long amountInCents,
    String status,
    String paymentMethod
) {}
