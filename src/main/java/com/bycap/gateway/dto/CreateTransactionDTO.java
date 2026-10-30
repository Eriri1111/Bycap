package com.bycap.gateway.dto;

import java.util.UUID;

/**
 * DTO de Entrada — Creación de una nueva transacción de pago.
 *
 * @param merchantId        UUID del comercio que origina la transacción.
 * @param amountInCents     Monto en centavos (mínimo 100.000 = $1.000 COP).
 * @param paymentMethodCode Código del método: "CARD", "NFC", "PSE", "NEQUI".
 * @param customerEmail     Correo del cliente para notificaciones.
 * @param token             Token del instrumento de pago.
 */
public record CreateTransactionDTO(
    UUID merchantId,
    Long amountInCents,
    String paymentMethodCode,
    String customerEmail,
    String token
) {}
