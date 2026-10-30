package com.bycap.gateway.service;

import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.dto.DashboardMetricsDTO;
import com.bycap.gateway.model.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de calcular las métricas del dashboard comercial
 * para un comercio específico a partir de las transacciones registradas.
 */
public class GetDashboardMetricsService {

    private final TransactionRepository transactionRepository;

    public GetDashboardMetricsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calcula y retorna las métricas transaccionales del comercio.
     *
     * @param merchantId ID del comercio.
     * @return DTO con los totales, montos aprobados, rechazados y conteos.
     */
    public DashboardMetricsDTO execute(UUID merchantId) {
        List<Transaction> transactions = transactionRepository.findByMerchantId(merchantId);

        long totalTransactionsCount = transactions.size();
        long approvedCount = 0;
        long declinedCount = 0;
        long totalApprovedAmountCents = 0;

        for (Transaction tx : transactions) {
            // Evaluamos según el estado de la transacción
            if (tx.getStatus() == Transaction.TransactionStatus.APPROVED) {
                approvedCount++;
                totalApprovedAmountCents += tx.getAmountInCents();
            } else if (tx.getStatus() == Transaction.TransactionStatus.DECLINED) {
                declinedCount++;
            }
        }

        return new DashboardMetricsDTO(
            merchantId,
            totalTransactionsCount,
            approvedCount,
            declinedCount,
            totalApprovedAmountCents
        );
    }
}