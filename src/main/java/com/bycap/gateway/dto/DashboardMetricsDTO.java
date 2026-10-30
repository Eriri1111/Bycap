package com.bycap.gateway.dto;

import java.util.UUID;

/**
 * DTO de Salida — Métricas agregadas del dashboard de un comercio.
 *
 * @param merchantId                UUID del comercio consultado.
 * @param totalApprovedAmountCents  Suma total en centavos de transacciones APPROVED.
 * @param totalTransactionsCount    Número total de transacciones (todos los estados).
 * @param approvedCount             Número de transacciones APPROVED.
 * @param declinedCount             Número de transacciones DECLINED.
 */
public record DashboardMetricsDTO(
    UUID merchantId,
    long totalApprovedAmountCents,
    long totalTransactionsCount,
    long approvedCount,
    long declinedCount
) {}
