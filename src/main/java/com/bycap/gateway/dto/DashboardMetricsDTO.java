package com.bycap.gateway.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Output DTO — Aggregated commercial dashboard analytics for a merchant.
 */
public record DashboardMetricsDTO(
    UUID merchantId,
    String merchantName,
    long totalApprovedAmountCents,
    long totalTransactionsCount,
    long approvedCount,
    long declinedCount,
    long pendingCount,
    double approvalRate,
    long averageTicketCents,
    Map<String, Long> volumeByMethodCents,
    Map<String, Long> countByMethod
) {}
