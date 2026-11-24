package com.bycap.gateway.config;

import com.bycap.gateway.dao.EmployeeRepository;
import com.bycap.gateway.dao.TransactionRepository;
import com.bycap.gateway.model.Employee;
import com.bycap.gateway.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Populates realistic initial merchant data into the SQL database if empty,
 * enabling immediate dashboard visualization and live testing.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            EmployeeRepository employeeRepository,
            TransactionRepository transactionRepository,
            @Value("${bycap.merchant.default-id:11111111-1111-1111-1111-111111111111}") String defaultMerchantIdStr) {

        return args -> {
            UUID merchantId = UUID.fromString(defaultMerchantIdStr);

            if (employeeRepository.count() == 0) {
                List<Employee> initialEmployees = List.of(
                    new Employee(null, "Alfonso David Araujo", "alfonso.araujo@bycap.io", merchantId, Employee.Role.SUPER_ADMIN),
                    new Employee(null, "Camila Andrea Morales", "camila.morales@bycap.io", merchantId, Employee.Role.MERCHANT_ADMIN),
                    new Employee(null, "Santiago Mendoza", "santiago.pos@bycap.io", merchantId, Employee.Role.CASHIER),
                    new Employee(null, "Valentina Herrera", "valentina.nfc@bycap.io", merchantId, Employee.Role.CASHIER)
                );
                employeeRepository.saveAll(initialEmployees);
            }

            if (transactionRepository.count() == 0) {
                Transaction t1 = new Transaction(null, merchantId, "BYCAP-ORD-2026001", 18500000L, "COP", "NFC", "laura.gomez@gmail.com", "Laura Gomez");
                t1.setGatewayTransactionId("wompi_nfc_98a12f4c");
                t1.markAsApproved();
                t1.setCreatedAt(LocalDateTime.now().minusHours(6));

                Transaction t2 = new Transaction(null, merchantId, "BYCAP-ORD-2026002", 42000000L, "COP", "CARD", "carlos.ruiz@outlook.com", "Carlos Ruiz");
                t2.setGatewayTransactionId("wompi_card_77b41e09");
                t2.markAsApproved();
                t2.setCreatedAt(LocalDateTime.now().minusHours(5));

                Transaction t3 = new Transaction(null, merchantId, "BYCAP-ORD-2026003", 9500000L, "COP", "NEQUI", "sofia.castro@gmail.com", "Sofia Castro");
                t3.setGatewayTransactionId("wompi_nequi_31c88d12");
                t3.markAsApproved();
                t3.setCreatedAt(LocalDateTime.now().minusHours(4));

                Transaction t4 = new Transaction(null, merchantId, "BYCAP-ORD-2026004", 31000000L, "COP", "PSE", "andres.perez@empresa.co", "Andres Perez");
                t4.setGatewayTransactionId("wompi_pse_55d90a21");
                t4.markAsApproved();
                t4.setCreatedAt(LocalDateTime.now().minusHours(3));

                Transaction t5 = new Transaction(null, merchantId, "BYCAP-ORD-2026005", 14500000L, "COP", "CARD", "maria.fernanda@gmail.com", "Maria Fernanda");
                t5.setGatewayTransactionId("wompi_card_10f33b87");
                t5.markAsDeclined();
                t5.setCreatedAt(LocalDateTime.now().minusHours(2));

                Transaction t6 = new Transaction(null, merchantId, "BYCAP-ORD-2026006", 26800000L, "COP", "NFC", "daniel.ortiz@icloud.com", "Daniel Ortiz");
                t6.setGatewayTransactionId("wompi_nfc_64e21c90");
                t6.markAsApproved();
                t6.setCreatedAt(LocalDateTime.now().minusMinutes(45));

                Transaction t7 = new Transaction(null, merchantId, "BYCAP-ORD-2026007", 7900000L, "COP", "NEQUI", "paula.diaz@gmail.com", "Paula Diaz");
                t7.setGatewayTransactionId("wompi_nequi_82a19f04");
                t7.markAsApproved();
                t7.setCreatedAt(LocalDateTime.now().minusMinutes(15));

                transactionRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6, t7));
            }
        };
    }
}
