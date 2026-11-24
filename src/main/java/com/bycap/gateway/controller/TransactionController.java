package com.bycap.gateway.controller;

import com.bycap.gateway.dto.CreateTransactionDTO;
import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.dto.WompiWebhookEventDTO;
import com.bycap.gateway.service.ProcessPaymentService;
import com.bycap.gateway.service.SignatureVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final ProcessPaymentService processPaymentService;
    private final SignatureVerificationService signatureVerificationService;

    public TransactionController(ProcessPaymentService processPaymentService,
                                 SignatureVerificationService signatureVerificationService) {
        this.processPaymentService = processPaymentService;
        this.signatureVerificationService = signatureVerificationService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> listTransactions(
            @RequestParam(value = "merchantId", required = false) UUID merchantId) {
        return ResponseEntity.ok(processPaymentService.getTransactionsByMerchant(merchantId));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody CreateTransactionDTO dto) {
        TransactionResponseDTO created = processPaymentService.execute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/webhook/wompi")
    public ResponseEntity<TransactionResponseDTO> receiveWompiWebhook(@RequestBody WompiWebhookEventDTO event) {
        TransactionResponseDTO updated = processPaymentService.processWebhookEvent(event);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/webhook/sign-preview")
    public ResponseEntity<Map<String, String>> previewWebhookSignature(@RequestBody WompiWebhookEventDTO event) {
        String signedString = signatureVerificationService.buildSignedString(event);
        String checksum = signatureVerificationService.calculateChecksum(event);
        return ResponseEntity.ok(Map.of(
            "signedPayload", signedString,
            "sha256Checksum", checksum
        ));
    }
}
