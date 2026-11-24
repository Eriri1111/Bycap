package com.bycap.gateway.dto;

import java.util.UUID;

/**
 * Input DTO — Registering a new merchant employee.
 */
public record RegisterEmployeeDTO(
    String fullName,
    String email,
    UUID merchantId,
    String role
) {}
