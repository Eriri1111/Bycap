package com.bycap.gateway.service;

import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.dto.CreateTransactionDTO;
import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.dto.WompiWebhookEventDTO;
import com.bycap.gateway.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service: Processing payments & handling Wompi webhook state updates in Bycap Gateway.
 */
@Service
public class ProcessPaymentService {

    private final TransactionRepository transactionRepository;
    private final List<PaymentStrategy> paymentStrategies;
    private final SignatureVerificationService signatureVerificationService;
    private final UUID defaultMerchantId;

    public ProcessPaymentService(TransactionRepository transactionRepository,
                                 List<PaymentStrategy> paymentStrategies,
                                 SignatureVerificationService signatureVerificationService,
                                 @Value("${bycap.merchant.default-id:11111111-1111-1111-1111-111111111111}") String defaultMerchantIdStr) {
        this.transactionRepository = transactionRepository;
        this.paymentStrategies = paymentStrategies;
        this.signatureVerificationService = signatureVerificationService;
        this.defaultMerchantId = UUID.fromString(defaultMerchantIdStr);
    }

    @Transactional
    public TransactionResponseDTO execute(CreateTransactionDTO dto) {
        if (dto.paymentMethodCode() == null || dto.paymentMethodCode().isBlank()) {
            throw new IllegalArgumentException("Payment method code is required.");
        }

        PaymentStrategy strategy = paymentStrategies.stream()
            .filter(s -> s.getPaymentMethodIdentifier().equalsIgnoreCase(dto.paymentMethodCode().trim()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported payment method: " + dto.paymentMethodCode() + ". Supported: " + getAvailableMethods()));

        UUID merchantId = dto.merchantId() != null ? dto.merchantId() : defaultMerchantId;
        String uniqueRef = "BYCAP-ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            merchantId,
            uniqueRef,
            dto.amountInCents(),
            "COP",
            dto.paymentMethodCode().toUpperCase(),
            dto.customerEmail(),
            dto.customerName()
        );

        TransactionResponseDTO response = strategy.processPayment(transaction, dto.token());
        transaction.setGatewayTransactionId(response.gatewayTransactionId());

        if ("APPROVED".equalsIgnoreCase(response.status())) {
            transaction.markAsApproved();
        } else {
            transaction.markAsDeclined();
        }

        Transaction saved = transactionRepository.save(transaction);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByMerchant(UUID merchantId) {
        UUID targetId = merchantId != null ? merchantId : defaultMerchantId;
        return transactionRepository.findByMerchantIdOrderByCreatedAtDesc(targetId)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public TransactionResponseDTO processWebhookEvent(WompiWebhookEventDTO event) {
        if (!signatureVerificationService.isValidSignature(event)) {
            throw new IllegalArgumentException("Invalid SHA-256 webhook signature checksum.");
        }

        Transaction tx = transactionRepository.findByReferenceIgnoreCase(event.reference())
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found for reference: " + event.reference()));

        Transaction.TransactionStatus newStatus;
        try {
            newStatus = Transaction.TransactionStatus.valueOf(event.status().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported webhook transaction status: " + event.status());
        }

        tx.applyWebhookStatus(newStatus, event.wompiTransactionId());
        Transaction saved = transactionRepository.save(tx);
        return toDTO(saved);
    }

    public List<String> getAvailableMethodsList() {
        return paymentStrategies.stream()
            .map(PaymentStrategy::getPaymentMethodIdentifier)
            .toList();
    }

    private String getAvailableMethods() {
        return String.join(", ", getAvailableMethodsList());
    }

    public TransactionResponseDTO toDTO(Transaction tx) {
        return new TransactionResponseDTO(
            tx.getId().toString(),
            tx.getReference(),
            tx.getAmountInCents(),
            tx.getCurrency(),
            tx.getStatus().name(),
            tx.getPaymentMethodCode(),
            tx.getCustomerEmail(),
            tx.getCustomerName(),
            tx.getGatewayTransactionId(),
            tx.getCreatedAt()
        );
    }
}
