package com.bycap.gateway;

import com.bycap.gateway.dao.EmployeeRepository;
import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.dao.InMemoryEmployeeRepository;
import com.bycap.gateway.dao.InMemoryTransactionRepository;
import com.bycap.gateway.service.PaymentStrategy;
import com.bycap.gateway.service.WompiCreditCardStrategy;
import com.bycap.gateway.service.WompiNfcContactlessStrategy;
import com.bycap.gateway.service.ProcessPaymentService;
import com.bycap.gateway.service.RegisterEmployeeService;
import com.bycap.gateway.service.GetDashboardMetricsService;
import com.bycap.gateway.presentation.console.ConsoleMenuRunner;

import java.util.List;

/**
 * Punto de Entrada y Raíz de Composición (Composition Root) de Bycap Gateway.
 */
public class BycapGatewayApplication {

    public static void main(String[] args) {

        // =====================================================================
        // 1. INFRAESTRUCTURA / DAO — Repositorios (Fase 1: InMemory)
        // =====================================================================
        TransactionRepository transactionRepository = new InMemoryTransactionRepository();
        EmployeeRepository    employeeRepository    = new InMemoryEmployeeRepository();

        // =====================================================================
        // 2. ESTRATEGIAS DE PAGO (Patrón Strategy)
        // =====================================================================
        List<PaymentStrategy> paymentStrategies = List.of(
            new WompiCreditCardStrategy(),   // CARD
            new WompiNfcContactlessStrategy()  // NFC
        );

        // =====================================================================
        // 3. CAPA DE SERVICIO — Lógica de Negocio
        // =====================================================================
        ProcessPaymentService      processPaymentService      = new ProcessPaymentService(transactionRepository, paymentStrategies);
        RegisterEmployeeService    registerEmployeeService    = new RegisterEmployeeService(employeeRepository);
        GetDashboardMetricsService getDashboardMetricsService = new GetDashboardMetricsService(transactionRepository);

        // =====================================================================
        // 4. PRESENTACIÓN — Menú de Consola (Fase 1)
        // =====================================================================
        ConsoleMenuRunner consoleMenuRunner = new ConsoleMenuRunner(
            processPaymentService,
            registerEmployeeService,
            getDashboardMetricsService
        );

        consoleMenuRunner.startMenu();
    }
}