package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;

/**
 * Interfaz Strategy — Métodos de Pago.
 *
 * Define el contrato que deben cumplir todos los adaptadores de pago
 * (tarjeta, NFC, PSE, NEQUI, etc.).
 *
 * Para agregar un nuevo método de pago:
 *   1. Crear una clase en este paquete que implemente esta interfaz.
 *   2. Registrarla en BycapGatewayApplication.
 *   3. NO modificar ProcessPaymentService.
 */
public interface PaymentStrategy {
    String getPaymentMethodIdentifier();
    TransactionResponseDTO processPayment(Transaction transaction, String token);
}