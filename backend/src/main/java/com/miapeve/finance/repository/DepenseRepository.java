package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Depense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {
    List<Depense> findByCompteId(Long compteId);
}
