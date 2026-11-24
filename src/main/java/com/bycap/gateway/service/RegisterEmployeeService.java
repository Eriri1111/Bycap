package com.bycap.gateway.service;

import com.bycap.gateway.dao.EmployeeRepository;
import com.bycap.gateway.dto.RegisterEmployeeDTO;
import com.bycap.gateway.exception.DuplicateEmployeeException;
import com.bycap.gateway.model.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service: Registering and managing merchant employees in Bycap Gateway.
 */
@Service
public class RegisterEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UUID defaultMerchantId;

    public RegisterEmployeeService(EmployeeRepository employeeRepository,
                                   @Value("${bycap.merchant.default-id:11111111-1111-1111-1111-111111111111}") String defaultMerchantIdStr) {
        this.employeeRepository = employeeRepository;
        this.defaultMerchantId = UUID.fromString(defaultMerchantIdStr);
    }

    @Transactional
    public Employee execute(RegisterEmployeeDTO dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("Employee email cannot be empty.");
        }
        employeeRepository.findByEmailIgnoreCase(dto.email()).ifPresent(e -> {
            throw new DuplicateEmployeeException(dto.email());
        });

        Employee.Role role;
        try {
            role = Employee.Role.valueOf(dto.role().toUpperCase().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid role '" + dto.role() + "'. Allowed roles: CASHIER, MERCHANT_ADMIN, SUPER_ADMIN.");
        }

        UUID merchantId = dto.merchantId() != null ? dto.merchantId() : defaultMerchantId;
        Employee newEmployee = new Employee(null, dto.fullName(), dto.email(), merchantId, role);
        return employeeRepository.save(newEmployee);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByMerchant(UUID merchantId) {
        UUID targetId = merchantId != null ? merchantId : defaultMerchantId;
        return employeeRepository.findByMerchantIdOrderByCreatedAtDesc(targetId);
    }

    @Transactional
    public Employee toggleEmployeeStatus(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));
        employee.toggleStatus();
        return employeeRepository.save(employee);
    }
}
