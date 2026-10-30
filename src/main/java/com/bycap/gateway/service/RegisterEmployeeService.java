package com.bycap.gateway.service;

import com.bycap.gateway.dao.EmployeeRepository;
import com.bycap.gateway.dto.RegisterEmployeeDTO;
import com.bycap.gateway.model.Employee;
import Util.DuplicateEmployeeException;

/**
 * Servicio: Registro de un nuevo empleado en el sistema Bycap.
 *
 * Flujo:
 *   1. Verificar unicidad del email (lanza DuplicateEmployeeException si ya existe).
 *   2. Convertir el role String a enum Employee.Role.
 *   3. Instanciar la entidad Employee (la entidad valida sus propias invariantes).
 *   4. Persistir y retornar el empleado creado.
 */
public class RegisterEmployeeService {

    private final EmployeeRepository employeeRepository;

    public RegisterEmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Registra un nuevo empleado.
     *
     * @param dto Datos del nuevo empleado.
     * @return La entidad Employee persistida con su UUID asignado.
     * @throws DuplicateEmployeeException si el email ya está registrado.
     * @throws IllegalArgumentException   si el rol no es válido.
     */
    public Employee execute(RegisterEmployeeDTO dto) {
        employeeRepository.findByEmail(dto.email()).ifPresent(e -> {
            throw new DuplicateEmployeeException(dto.email());
        });

        Employee.Role role;
        try {
            role = Employee.Role.valueOf(dto.role().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "El rol '" + dto.role() + "' no es válido. Roles permitidos: CASHIER, MERCHANT_ADMIN, SUPER_ADMIN.");
        }

        Employee newEmployee = new Employee(null, dto.fullName(), dto.email(), dto.merchantId(), role);
        return employeeRepository.save(newEmployee);
    }
}
