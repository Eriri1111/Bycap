package com.bycap.gateway.dto;

import java.util.UUID;

/**
 * DTO de Entrada — Registro de un nuevo empleado.
 *
 * @param fullName   Nombre completo del empleado.
 * @param email      Correo electrónico corporativo (debe ser único).
 * @param merchantId UUID del comercio al que pertenecerá.
 * @param role       Rol en mayúsculas: "CASHIER", "MERCHANT_ADMIN", "SUPER_ADMIN".
 */
public record RegisterEmployeeDTO(
    String fullName,
    String email,
    UUID merchantId,
    String role
) {}
