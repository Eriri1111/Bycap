package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SignatureVerificationService (RF-04, RN-05).
 */
class SignatureVerificationServiceTest {

    private static final String SECRET = "secret_test_key";
    private static final String EXPECTED_CHECKSUM =
        "ea9226793cdeb5a3a4912f4cee094581a4db05e82f9afcc2453cb2b3247a16ab";

    private SignatureVerificationService service;

    @BeforeEach
    void setUp() {
        service = new SignatureVerificationService(SECRET);
    }

    private WompiWebhookEventDTO eventWith(Long amountInCents, String checksum) {
        return new WompiWebhookEventDTO(
            "1234-1610641025-49201",
            "BYCAP-ORD-0001",
            "APPROVED",
            amountInCents,
            "COP",
            1530291411L,
            checksum
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Rejects null, empty, or blank secret")
    void constructorRejectsInvalidSecret(String invalidSecret) {
        assertThrows(IllegalArgumentException.class,
            () -> new SignatureVerificationService(invalidSecret));
    }

    @Test
    @DisplayName("Builds signed string in Wompi specification order")
    void buildSignedStringConcatenatesFieldsInOrder() {
        String signed = service.buildSignedString(eventWith(4490000L, null));
        assertEquals("1234-1610641025-49201APPROVED4490000COP1530291411secret_test_key", signed);
    }

    @Test
    @DisplayName("Computes expected lowercase hexadecimal SHA-256 checksum")
    void calculateChecksumMatchesKnownValue() {
        assertEquals(EXPECTED_CHECKSUM, service.calculateChecksum(eventWith(4490000L, null)));
    }

    @Test
    @DisplayName("Accepts valid checksum signature")
    void acceptsValidSignature() {
        assertTrue(service.isValidSignature(eventWith(4490000L, EXPECTED_CHECKSUM)));
    }

    @Test
    @DisplayName("Accepts valid signature ignoring case and whitespace")
    void acceptsValidSignatureIgnoringCaseAndSpaces() {
        String received = "  " + EXPECTED_CHECKSUM.toUpperCase() + " ";
        assertTrue(service.isValidSignature(eventWith(4490000L, received)));
    }

    @Test
    @DisplayName("Rejects signature if amount was tampered")
    void rejectsSignatureWhenAmountIsTampered() {
        assertFalse(service.isValidSignature(eventWith(1L, EXPECTED_CHECKSUM)));
    }

    @Test
    @DisplayName("Rejects signature computed with a different secret")
    void rejectsSignatureFromDifferentSecret() {
        SignatureVerificationService otherService = new SignatureVerificationService("other_secret");
        assertFalse(otherService.isValidSignature(eventWith(4490000L, EXPECTED_CHECKSUM)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Rejects webhook event if signature is missing")
    void rejectsMissingSignature(String missingChecksum) {
        assertFalse(service.isValidSignature(eventWith(4490000L, missingChecksum)));
    }
}
