/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bycap.gateway.service;

import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.model.Transaction;

import java.util.UUID;

public class WompiNfcContactlessStrategy implements PaymentStrategy {

    @Override
    public String getPaymentMethodIdentifier() {
        return "NFC";
    }

    @Override
    public TransactionResponseDTO processPayment(Transaction transaction, String token) {
        String nfcTokenId = "tok_nfc_" + UUID.randomUUID().toString().substring(0, 8);

        return new TransactionResponseDTO(
            transaction.getId().toString(),
            transaction.getReference(),
            transaction.getAmountInCents(),
            "APPROVED",
            getPaymentMethodIdentifier()
        );
    }
}