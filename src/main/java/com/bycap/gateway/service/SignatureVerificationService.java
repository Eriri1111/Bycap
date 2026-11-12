package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public class SignatureVerificationService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final String eventsSecret;

    public SignatureVerificationService(String eventsSecret) {
        if (eventsSecret == null || eventsSecret.isBlank()) {
            throw new IllegalArgumentException("The Wompi events secret cannot be empty.");
        }
        this.eventsSecret = eventsSecret;
    }

    public String buildSignedString(WompiWebhookEventDTO event) {
        return event.wompiTransactionId()
            + event.status()
            + event.amountInCents()
            + event.currency()
            + event.timestamp()
            + eventsSecret;
    }

    public String calculateChecksum(WompiWebhookEventDTO event) {
        return sha256Hex(buildSignedString(event));
    }

    public boolean isValidSignature(WompiWebhookEventDTO event) {
        String received = event.checksum();
        if (received == null || received.isBlank()) {
            return false;
        }
        byte[] expectedBytes = calculateChecksum(event).getBytes(StandardCharsets.US_ASCII);
        byte[] receivedBytes = received.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expectedBytes, receivedBytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available in this JVM.", e);
        }
    }
}
