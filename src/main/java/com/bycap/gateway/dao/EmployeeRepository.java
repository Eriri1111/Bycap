package com.bycap.gateway.dao;

import com.bycap.gateway.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO Interface backed by Spring Data JPA for Employee persistence.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    List<Employee> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}
