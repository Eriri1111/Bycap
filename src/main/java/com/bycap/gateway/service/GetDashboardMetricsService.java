package com.bycap.gateway.service;

import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.dto.DashboardMetricsDTO;
import com.bycap.gateway.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for calculating real-time commercial dashboard KPIs
 * and payment method breakdowns from database records.
 */
@Service
public class GetDashboardMetricsService {

    private final TransactionRepository transactionRepository;
    private final UUID defaultMerchantId;
    private final String defaultMerchantName;

    public GetDashboardMetricsService(TransactionRepository transactionRepository,
                                      @Value("${bycap.merchant.default-id:11111111-1111-1111-1111-111111111111}") String defaultMerchantIdStr,
                                      @Value("${bycap.merchant.name:Bycap Flagship Store Valledupar}") String defaultMerchantName) {
        this.transactionRepository = transactionRepository;
        this.defaultMerchantId = UUID.fromString(defaultMerchantIdStr);
        this.defaultMerchantName = defaultMerchantName;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDTO execute(UUID merchantId) {
        UUID targetId = merchantId != null ? merchantId : defaultMerchantId;
        List<Transaction> transactions = transactionRepository.findByMerchantIdOrderByCreatedAtDesc(targetId);

        long totalTransactionsCount = transactions.size();
        long approvedCount = 0;
        long declinedCount = 0;
        long pendingCount = 0;
        long totalApprovedAmountCents = 0;

        Map<String, Long> volumeByMethodCents = new LinkedHashMap<>();
        volumeByMethodCents.put("CARD", 0L);
        volumeByMethodCents.put("NFC", 0L);
        volumeByMethodCents.put("PSE", 0L);
        volumeByMethodCents.put("NEQUI", 0L);

        Map<String, Long> countByMethod = new LinkedHashMap<>();
        countByMethod.put("CARD", 0L);
        countByMethod.put("NFC", 0L);
        countByMethod.put("PSE", 0L);
        countByMethod.put("NEQUI", 0L);

        for (Transaction tx : transactions) {
            String method = tx.getPaymentMethodCode().toUpperCase();
            countByMethod.merge(method, 1L, Long::sum);

            if (tx.getStatus() == Transaction.TransactionStatus.APPROVED) {
                approvedCount++;
                totalApprovedAmountCents += tx.getAmountInCents();
                volumeByMethodCents.merge(method, tx.getAmountInCents(), Long::sum);
            } else if (tx.getStatus() == Transaction.TransactionStatus.DECLINED) {
                declinedCount++;
            } else if (tx.getStatus() == Transaction.TransactionStatus.PENDING) {
                pendingCount++;
            }
        }

        double approvalRate = totalTransactionsCount > 0
            ? Math.round((approvedCount * 1000.0) / totalTransactionsCount) / 10.0
            : 0.0;

        long averageTicketCents = approvedCount > 0
            ? totalApprovedAmountCents / approvedCount
            : 0L;

        return new DashboardMetricsDTO(
            targetId,
            defaultMerchantName,
            totalApprovedAmountCents,
            totalTransactionsCount,
            approvedCount,
            declinedCount,
            pendingCount,
            approvalRate,
            averageTicketCents,
            volumeByMethodCents,
            countByMethod
        );
    }
}
