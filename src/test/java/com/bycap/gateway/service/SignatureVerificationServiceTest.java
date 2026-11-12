package com.bycap.gateway.service;

import com.bycap.gateway.dto.WompiWebhookEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de SignatureVerificationService (RF-04, RN-05).
 *
 * GRASP: Pure Fabrication. Las pruebas viven en su propia clase, separadas del código de producción.
 * SOLID: SRP. Esta clase solo verifica el cálculo y la validación de la firma SHA-256.
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
    @DisplayName("Rechaza un secreto nulo, vacío o en blanco")
    void constructorRejectsInvalidSecret(String invalidSecret) {
        assertThrows(IllegalArgumentException.class,
            () -> new SignatureVerificationService(invalidSecret));
    }

    @Test
    @DisplayName("Arma la cadena firmada en el orden de Wompi")
    void buildSignedStringConcatenatesFieldsInOrder() {
        String signed = service.buildSignedString(eventWith(4490000L, null));

        assertEquals("1234-1610641025-49201APPROVED4490000COP1530291411secret_test_key", signed);
    }

    @Test
    @DisplayName("Calcula el SHA-256 esperado en hexadecimal minúscula")
    void calculateChecksumMatchesKnownValue() {
        assertEquals(EXPECTED_CHECKSUM, service.calculateChecksum(eventWith(4490000L, null)));
    }

    @Test
    @DisplayName("Acepta una firma correcta")
    void acceptsValidSignature() {
        assertTrue(service.isValidSignature(eventWith(4490000L, EXPECTED_CHECKSUM)));
    }

    @Test
    @DisplayName("Acepta una firma correcta en mayúsculas y con espacios")
    void acceptsValidSignatureIgnoringCaseAndSpaces() {
        String received = "  " + EXPECTED_CHECKSUM.toUpperCase() + " ";

        assertTrue(service.isValidSignature(eventWith(4490000L, received)));
    }

    @Test
    @DisplayName("Rechaza la firma si el monto fue alterado")
    void rejectsSignatureWhenAmountIsTampered() {
        assertFalse(service.isValidSignature(eventWith(1L, EXPECTED_CHECKSUM)));
    }

    @Test
    @DisplayName("Rechaza la firma si se calculó con otro secreto")
    void rejectsSignatureFromDifferentSecret() {
        SignatureVerificationService otherService = new SignatureVerificationService("other_secret");

        assertFalse(otherService.isValidSignature(eventWith(4490000L, EXPECTED_CHECKSUM)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Rechaza el evento si no trae firma")
    void rejectsMissingSignature(String missingChecksum) {
        assertFalse(service.isValidSignature(eventWith(4490000L, missingChecksum)));
    }
}
