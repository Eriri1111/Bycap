package com.bycap.gateway.dao;

import com.bycap.gateway.model.Employee;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación DAO en Memoria para Empleados.
 *
 * Permite ejecutar y probar toda la lógica del sistema sin configurar
 * una base de datos real. Para Fase 2 se reemplaza por JpaEmployeeRepository
 * sin cambiar ninguna clase de servicio o controlador.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<UUID, Employee> store = new HashMap<>();

    @Override
    public Employee save(Employee employee) {
        store.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        String normalized = email.toLowerCase().trim();
        return store.values().stream()
            .filter(e -> normalized.equals(e.getEmail()))
            .findFirst();
    }

    @Override
    public List<Employee> findByMerchantId(UUID merchantId) {
        if (merchantId == null) return Collections.emptyList();
        return store.values().stream()
            .filter(e -> merchantId.equals(e.getMerchantId()))
            .collect(Collectors.toList());
    }
}
