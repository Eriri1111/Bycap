package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WompiNequiStrategy implements PaymentStrategy {

    @Override
    public String getPaymentMethodIdentifier() {
        return "NEQUI";
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction, String token) {
        String nequiTxId = "wompi_nequi_" + UUID.randomUUID().toString().substring(0, 8);
        boolean declined = token != null && token.toLowerCase().contains("decline");
        String status = declined ? "DECLINED" : "APPROVED";

        return new TransactionResponseDTO(
            transaction.getId().toString(),
            transaction.getReference(),
            transaction.getAmountInCents(),
            transaction.getCurrency(),
            status,
            getPaymentMethodIdentifier(),
            transaction.getCustomerEmail(),
            transaction.getCustomerName(),
            nequiTxId,
            transaction.getCreatedAt()
        );
    }
}
