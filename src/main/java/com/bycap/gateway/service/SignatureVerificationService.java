package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Calcula y verifica la firma SHA-256 de los webhooks de Wompi (RF-04, RN-05).
 *
 * GRASP: Pure Fabrication. El cálculo criptográfico no pertenece al negocio,
 * por eso vive en su propia clase y no en Transaction.
 * SOLID: SRP. Esta clase solo cambia si cambia el algoritmo de firma.
 */
public class SignatureVerificationService {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final String eventsSecret;

    /**
     * Crea el servicio con el secreto de eventos de Wompi.
     *
     * @param eventsSecret secreto compartido con Wompi.
     * @throws IllegalArgumentException si el secreto es nulo o está vacío.
     */
    public SignatureVerificationService(String eventsSecret) {
        if (eventsSecret == null || eventsSecret.isBlank()) {
            throw new IllegalArgumentException("The Wompi events secret cannot be empty.");
        }
        this.eventsSecret = eventsSecret;
    }

    /**
     * Arma la cadena que Wompi firma: id + estado + monto + moneda + timestamp + secreto.
     *
     * @param event evento del webhook.
     * @return cadena concatenada lista para el hash.
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
     * Calcula la firma SHA-256 que debería traer el evento.
     *
     * @param event evento del webhook.
     * @return hash SHA-256 en hexadecimal minúscula.
     */
    public String calculateChecksum(WompiWebhookEventDTO event) {
        return sha256Hex(buildSignedString(event));
    }

    /**
     * Indica si la firma recibida coincide con la calculada, sin importar mayúsculas.
     *
     * @param event evento del webhook con la firma recibida.
     * @return true si la firma es válida; false si falta o no coincide.
     */
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
