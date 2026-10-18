package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByOrderByAnneeDescMoisDesc();
    List<Budget> findByMoisAndAnnee(int mois, int annee);
}
