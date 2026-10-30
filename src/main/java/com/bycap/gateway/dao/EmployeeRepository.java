package com.bycap.gateway.dao;

import com.bycap.gateway.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz DAO: Contrato de persistencia para Empleados.
 *
 * Define las operaciones de acceso a datos para la entidad Employee.
 * Las implementaciones concretas (InMemory, JPA) se encuentran
 * en el mismo paquete dao, siguiendo la convención tradicional.
 */
public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByMerchantId(UUID merchantId);
}
