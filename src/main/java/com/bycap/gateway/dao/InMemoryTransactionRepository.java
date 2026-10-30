package com.bycap.gateway.dao;

import com.bycap.gateway.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación DAO en Memoria para Transacciones.
 *
 * Permite ejecutar y probar toda la lógica del sistema sin configurar
 * una base de datos real. Para Fase 2 se reemplaza por JpaTransactionRepository
 * sin cambiar ninguna clase de servicio o controlador.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<UUID, Transaction> store = new HashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        store.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findByReference(String reference) {
        if (reference == null || reference.isBlank()) return Optional.empty();
        return store.values().stream()
            .filter(t -> reference.equalsIgnoreCase(t.getReference()))
            .findFirst();
    }

    @Override
    public List<Transaction> findByMerchantId(UUID merchantId) {
        if (merchantId == null) return Collections.emptyList();
        return store.values().stream()
            .filter(t -> merchantId.equals(t.getMerchantId()))
            .collect(Collectors.toList());
    }
}
