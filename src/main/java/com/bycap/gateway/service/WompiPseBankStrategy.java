package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WompiPseBankStrategy implements PaymentStrategy {

    @Override
    public String getPaymentMethodIdentifier() {
        return "PSE";
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction, String token) {
        String pseTxId = "wompi_pse_" + UUID.randomUUID().toString().substring(0, 8);
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
            pseTxId,
            transaction.getCreatedAt()
        );
    }
}
