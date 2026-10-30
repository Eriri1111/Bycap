package com.bycap.gateway.presentation.console;

import com.bycap.gateway.dto.CreateTransactionDTO;
import com.bycap.gateway.dto.DashboardMetricsDTO;
import com.bycap.gateway.dto.RegisterEmployeeDTO;
import com.bycap.gateway.dto.TransactionResponseDTO;
import com.bycap.gateway.service.GetDashboardMetricsService;
import com.bycap.gateway.service.ProcessPaymentService;
import com.bycap.gateway.service.RegisterEmployeeService;
import Util.DuplicateEmployeeException;

import java.util.Scanner;
import java.util.UUID;

/**
 * Menú Interactivo de Consola — Capa de Presentación.
 *
 * Responsabilidad:
 *   - Capturar y validar la entrada del usuario.
 *   - Invocar los servicios correspondientes de la capa de negocio.
 *   - Presentar los resultados y errores de forma legible.
 */
public class ConsoleMenuRunner {

    private static final String SEPARATOR = "==================================================";
    private static final String LINE      = "--------------------------------------------------";

    private final ProcessPaymentService      processPaymentService;
    private final RegisterEmployeeService    registerEmployeeService;
    private final GetDashboardMetricsService getDashboardMetricsService;

    public ConsoleMenuRunner(ProcessPaymentService processPaymentService,
                             RegisterEmployeeService registerEmployeeService,
                             GetDashboardMetricsService getDashboardMetricsService) {
        this.processPaymentService      = processPaymentService;
        this.registerEmployeeService    = registerEmployeeService;
        this.getDashboardMetricsService = getDashboardMetricsService;
    }

    /**
     * Arranca el menú interactivo.
     * Mantiene un UUID de comercio fijo durante la sesión para simular un comercio de prueba.
     */
    public void startMenu() {
        Scanner scanner        = new Scanner(System.in);
        UUID currentMerchantId = UUID.randomUUID(); // Comercio de prueba fijo para la sesión
        boolean running        = true;

        printWelcomeBanner(currentMerchantId);

        while (running) {
            printMainMenu();
            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> handleRegisterEmployee(scanner, currentMerchantId);
                case "2" -> handleCreateTransaction(scanner, currentMerchantId);
                case "3" -> handleShowDashboard(currentMerchantId);
                case "4" -> {
                    System.out.println("\n  Cerrando sesión de Bycap Gateway... ¡Hasta pronto!");
                    System.out.println(SEPARATOR);
                    running = false;
                }
                default -> System.out.println("\n  ⚠️  Opción no válida. Ingrese un número del 1 al 4.");
            }
        }
        scanner.close();
    }

    // =========================================================================
    // Manejadores de Opciones del Menú
    // =========================================================================

    private void handleRegisterEmployee(Scanner scanner, UUID merchantId) {
        System.out.println("\n" + LINE);
        System.out.println("  REGISTRAR NUEVO EMPLEADO");
        System.out.println(LINE);

        System.out.print("  Nombre completo      : ");
        String name = scanner.nextLine().trim();

        System.out.print("  Email corporativo    : ");
        String email = scanner.nextLine().trim();

        System.out.println("  Roles disponibles    : CASHIER | MERCHANT_ADMIN | SUPER_ADMIN");
        System.out.print("  Rol del empleado     : ");
        String role = scanner.nextLine().trim();

        try {
            var employee = registerEmployeeService.execute(
                new RegisterEmployeeDTO(name, email, merchantId, role)
            );
            System.out.println("\n  ✅ Empleado registrado exitosamente.");
            System.out.println("  ├─ ID     : " + employee.getId());
            System.out.println("  ├─ Nombre : " + employee.getFullName());
            System.out.println("  ├─ Email  : " + employee.getEmail());
            System.out.println("  └─ Rol    : " + employee.getRole());
        } catch (DuplicateEmployeeException e) {
            System.out.println("\n  ❌ Error de negocio: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n  ❌ Datos inválidos: " + e.getMessage());
        }
    }

    private void handleCreateTransaction(Scanner scanner, UUID merchantId) {
        System.out.println("\n" + LINE);
        System.out.println("  CREAR NUEVA TRANSACCIÓN");
        System.out.println(LINE);

        System.out.print("  Monto en pesos COP (ej. 50000): $");
        long amountCop;
        try {
            amountCop = Long.parseLong(scanner.nextLine().trim().replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            System.out.println("\n  ❌ Monto inválido. Ingrese solo números (ej. 50000).");
            return;
        }
        long amountInCents = amountCop * 100; // Conversión a centavos (RN-01)

        System.out.println("  Métodos disponibles  : CARD | NFC");
        System.out.print("  Método de pago       : ");
        String method = scanner.nextLine().trim().toUpperCase();

        try {
            TransactionResponseDTO response = processPaymentService.execute(
                new CreateTransactionDTO(merchantId, amountInCents, method, "cliente@ejemplo.com", "tok_test_sandbox_1234")
            );
            System.out.println("\n  ✅ Transacción procesada exitosamente.");
            System.out.println("  ├─ ID         : " + response.transactionId());
            System.out.println("  ├─ Referencia : " + response.reference());
            System.out.printf ("  ├─ Monto      : $%,.2f COP%n", response.amountInCents() / 100.0);
            System.out.println("  ├─ Estado     : " + response.status());
            System.out.println("  └─ Método     : " + response.paymentMethod());
        } catch (IllegalArgumentException e) {
            System.out.println("\n  ❌ Error en la transacción: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  ❌ Error inesperado: " + e.getMessage());
        }
    }

    private void handleShowDashboard(UUID merchantId) {
        DashboardMetricsDTO metrics = getDashboardMetricsService.execute(merchantId);
        double totalCop = metrics.totalApprovedAmountCents() / 100.0;

        System.out.println("\n" + SEPARATOR);
        System.out.println("      📊 DASHBOARD DE RENDIMIENTO COMERCIAL");
        System.out.println(SEPARATOR);
        System.out.println("  Comercio ID          : " + metrics.merchantId());
        System.out.println(LINE);
        System.out.printf ("  Volumen Aprobado     : $%,.2f COP%n", totalCop);
        System.out.println("  Total Transacciones    : " + metrics.totalTransactionsCount());
        System.out.println("  ├─ ✅ Aprobadas        : " + metrics.approvedCount());
        System.out.println("  └─ ❌ Rechazadas       : " + metrics.declinedCount());

        if (metrics.totalTransactionsCount() > 0) {
            double approvalRate = (metrics.approvedCount() * 100.0) / metrics.totalTransactionsCount();
            System.out.printf ("  Tasa de Aprobación     : %.1f%%%n", approvalRate);
        } else {
            System.out.println("  Tasa de Aprobación     : N/A (sin transacciones)");
        }
        System.out.println(SEPARATOR);
    }

    private void printWelcomeBanner(UUID merchantId) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("        BYCAP COMMERCIAL PAYMENT GATEWAY");
        System.out.println("        Programación III — UPC  |  v1.0.0");
        System.out.println(SEPARATOR);
        System.out.println("  Sesión iniciada para Comercio ID:");
        System.out.println("  " + merchantId);
        System.out.println(SEPARATOR);
    }

    private void printMainMenu() {
        System.out.println("\n" + LINE);
        System.out.println("  MENÚ PRINCIPAL");
        System.out.println(LINE);
        System.out.println("  [1] Registrar Empleado / Usuario del Comercio");
        System.out.println("  [2] Crear Transacción (Tarjeta / NFC)");
        System.out.println("  [3] Ver Dashboard de Métricas Transaccionales");
        System.out.println("  [4] Salir");
        System.out.println(LINE);
        System.out.print("  Seleccione una opción: ");
    }
}