package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Computes and verifies the SHA-256 signature of Wompi webhooks (RF-04, RN-05).
 *
 * GRASP: Pure Fabrication. Cryptographic hashing is isolated from domain entities.
 * SOLID: SRP. Only changes if the signature specification changes.
 */
@Service
public class SignatureVerificationService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final String eventsSecret;

    public SignatureVerificationService(
            @Value("${bycap.wompi.events-secret:prod_events_bycap_secret_2026}") String eventsSecret) {
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
