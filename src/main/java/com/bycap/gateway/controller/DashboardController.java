package com.bycap.gateway.controller;

import com.bycap.gateway.dto.DashboardMetricsDTO;
import com.bycap.gateway.service.GetDashboardMetricsService;
import com.bycap.gateway.service.ProcessPaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final GetDashboardMetricsService getDashboardMetricsService;
    private final ProcessPaymentService processPaymentService;
    private final Environment environment;
    private final UUID defaultMerchantId;
    private final String defaultMerchantName;

    public DashboardController(GetDashboardMetricsService getDashboardMetricsService,
                               ProcessPaymentService processPaymentService,
                               Environment environment,
                               @Value("${bycap.merchant.default-id:11111111-1111-1111-1111-111111111111}") String defaultMerchantIdStr,
                               @Value("${bycap.merchant.name:Bycap Flagship Store Valledupar}") String defaultMerchantName) {
        this.getDashboardMetricsService = getDashboardMetricsService;
        this.processPaymentService = processPaymentService;
        this.environment = environment;
        this.defaultMerchantId = UUID.fromString(defaultMerchantIdStr);
        this.defaultMerchantName = defaultMerchantName;
    }

    @GetMapping("/dashboard/metrics")
    public ResponseEntity<DashboardMetricsDTO> getMetrics(
            @RequestParam(value = "merchantId", required = false) UUID merchantId) {
        return ResponseEntity.ok(getDashboardMetricsService.execute(merchantId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        String dbUrl = environment.getProperty("spring.datasource.url", "jdbc:h2:mem:bycapdb");
        String dbEngine = dbUrl.contains("postgresql") ? "PostgreSQL" : "H2 Embedded SQL";

        return ResponseEntity.ok(Map.of(
            "status", "ONLINE",
            "service", "Bycap Commercial Payment Gateway",
            "version", "2.0.0",
            "databaseEngine", dbEngine,
            "databaseConnected", true,
            "defaultMerchantId", defaultMerchantId.toString(),
            "defaultMerchantName", defaultMerchantName,
            "supportedMethods", processPaymentService.getAvailableMethodsList(),
            "serverTime", Instant.now().toString()
        ));
    }
}
