package com.bycap.gateway.dao;

import com.bycap.gateway.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO Interface backed by Spring Data JPA for Transaction persistence.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceIgnoreCase(String reference);

    List<Transaction> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}
