package com.bycap.gateway.service;

import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.dto.CreateTransactionDTO;
import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Servicio: Procesamiento de un pago a través del gateway Bycap.
 *
 * Flujo:
 *   1. Generar referencia única idempotente (RN-03).
 *   2. Instanciar Transaction en estado PENDING (valida monto mínimo RN-02).
 *   3. Resolver la PaymentStrategy por código de método (polimorfismo).
 *   4. Delegar el procesamiento al adaptador concreto.
 *   5. Persistir la transacción con su estado final.
 *   6. Retornar el DTO de respuesta.
 */
public class ProcessPaymentService {

    private final TransactionRepository transactionRepository;
    private final List<PaymentStrategy> paymentStrategies;

    public ProcessPaymentService(TransactionRepository transactionRepository,
                                  List<PaymentStrategy> paymentStrategies) {
        this.transactionRepository = transactionRepository;
        this.paymentStrategies     = paymentStrategies;
    }

    /**
     * Procesa un pago.
     *
     * @param dto Datos de la transacción.
     * @return DTO con el ID, referencia y estado final.
     * @throws IllegalArgumentException si el monto es inválido o el método no está soportado.
     */
    public TransactionResponseDTO execute(CreateTransactionDTO dto) {
        PaymentStrategy strategy = paymentStrategies.stream()
            .filter(s -> s.getPaymentMethodIdentifier().equalsIgnoreCase(dto.paymentMethodCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Método de pago no soportado: " + dto.paymentMethodCode()));

       Transaction transaction = new Transaction(
            UUID.randomUUID(),
            dto.merchantId(),
            "BYCAP-ORD-" + System.currentTimeMillis(),
            dto.amountInCents(),
            "COP",
            dto.customerEmail()
        );

        TransactionResponseDTO response = strategy.processPayment(transaction, dto.token());

        if ("APPROVED".equalsIgnoreCase(response.status())) {
            transaction.markAsApproved();
        } else {
            transaction.markAsDeclined();
        }

        transactionRepository.save(transaction);

       return response;
    }

    private String getAvailableMethods() {
        return paymentStrategies.stream()
            .map(PaymentStrategy::getPaymentMethodIdentifier)
            .reduce((a, b) -> a + ", " + b)
            .orElse("ninguno registrado");
    }
}
