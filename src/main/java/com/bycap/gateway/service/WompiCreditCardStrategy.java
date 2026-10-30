
package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;

import java.util.UUID;

public class WompiCreditCardStrategy implements PaymentStrategy {

    @Override
    public String getPaymentMethodIdentifier() {
        return "CARD";
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction, String token) {
        String wompiTxId = "wompi_tx_card_" + UUID.randomUUID().toString().substring(0, 8);
        
       return new TransactionResponseDTO(
            transaction.getId().toString(),
            transaction.getReference(),
            transaction.getAmountInCents(),
            "APPROVED",
            getPaymentMethodIdentifier()
        );
    }
}