package com.bycap.gateway.dao;

import com.bycap.gateway.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz DAO: Contrato de persistencia para Transacciones.
 *
 * Define las operaciones de acceso a datos para la entidad Transaction.
 * Las implementaciones concretas (InMemory, JPA) se encuentran
 * en el mismo paquete dao, siguiendo la convención tradicional.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByReference(String reference);

    List<Transaction> findByMerchantId(UUID merchantId);
}
