package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompteIdOrderByDateCreationDesc(Long compteId);
    List<Transaction> findAllByOrderByDateCreationDesc();
}
