package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Computes and verifies the SHA-256 checksum of Wompi webhook events.
 *
 * Covers RF-04 and RN-05: a webhook may only change a transaction status
 * when its signature is valid.
 *
 * Signed string: wompiTransactionId + status + amountInCents + currency + timestamp + eventsSecret.
 * The digest is hex-encoded and compared with the received checksum, ignoring case.
 *
 * GRASP: Pure Fabrication. Hashing is not a business concept, so it lives in its own
 * class instead of polluting Transaction or Merchant.
 * SOLID: SRP. This class only changes if the signature algorithm changes.
 */
public class SignatureVerificationService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final String eventsSecret;

    /**
     * Creates the service with the Wompi events secret.
     *
     * @param eventsSecret Secret shared with Wompi to sign events. Cannot be blank.
     * @throws IllegalArgumentException if the secret is null or blank.
     */
    public SignatureVerificationService(String eventsSecret) {
        if (eventsSecret == null || eventsSecret.isBlank()) {
            throw new IllegalArgumentException("The Wompi events secret cannot be empty.");
        }
        this.eventsSecret = eventsSecret;
    }

    /**
     * Builds the exact string that Wompi signs for an event.
     *
     * @param event Webhook event.
     * @return Concatenated string, including the events secret.
     */
    public String buildSignedString(WompiWebhookEventDTO event) {
        return event.wompiTransactionId()
            + event.status()
            + event.amountInCents()
            + event.currency()
            + event.timestamp()
            + eventsSecret;
    }

    /**
     * Computes the SHA-256 checksum that a webhook event should carry.
     *
     * @param event Webhook event.
     * @return Lowercase hexadecimal SHA-256 digest (64 characters).
     */
    public String calculateChecksum(WompiWebhookEventDTO event) {
        return sha256Hex(buildSignedString(event));
    }

    /**
     * Checks whether the checksum received in the event matches the computed one.
     *
     * @param event Webhook event, including the received checksum.
     * @return true if the signature is valid; false if it is missing or does not match.
     */
    public boolean isValidSignature(WompiWebhookEventDTO event) {
        String received = event.checksum();
        if (received == null || received.isBlank()) {
            return false;
        }
        byte[] expectedBytes = calculateChecksum(event).getBytes(StandardCharsets.US_ASCII);
        byte[] receivedBytes = received.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        // Constant-time comparison to avoid leaking information through timing.
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
