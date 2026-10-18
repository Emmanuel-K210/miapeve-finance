package com.miapeve.finance.service;

import com.miapeve.finance.dto.BudgetDTO;
import com.miapeve.finance.dto.BudgetRequest;
import com.miapeve.finance.entity.Budget;
import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.repository.BudgetRepository;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.DepenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CompteRepository compteRepository;
    private final DepenseRepository depenseRepository;

    public BudgetService(BudgetRepository budgetRepository, CompteRepository compteRepository, DepenseRepository depenseRepository) {
        this.budgetRepository = budgetRepository;
        this.compteRepository = compteRepository;
        this.depenseRepository = depenseRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> findAll() {
        return budgetRepository.findAllByOrderByAnneeDescMoisDesc().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> findForCurrentMonth() {
        YearMonth now = YearMonth.now();
        return budgetRepository.findByMoisAndAnnee(now.getMonthValue(), now.getYear()).stream().map(this::toDTO).toList();
    }

    @Transactional
    public BudgetDTO create(BudgetRequest request, Long utilisateurId) {
        Compte compte = compteRepository.findById(request.compteId())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + request.compteId()));

        Budget budget = new Budget();
        budget.setLibelle(request.libelle());
        budget.setCompte(compte);
        budget.setMontantPrevu(request.montantPrevu());
        budget.setMois(request.mois());
        budget.setAnnee(request.annee());
        budget.setCreePar(utilisateurId);

        return toDTO(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(Long id) {
        budgetRepository.deleteById(id);
    }

    private BudgetDTO toDTO(Budget budget) {
        YearMonth periode = YearMonth.of(budget.getAnnee(), budget.getMois());
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        double consomme = depenseRepository
                .findByCompteIdAndDateDepenseBetween(budget.getCompte().getId(), debut, fin)
                .stream()
                .mapToDouble(d -> d.getMontant())
                .sum();

        return new BudgetDTO(
                budget.getId(),
                budget.getLibelle(),
                budget.getCompte().getId(),
                budget.getCompte().getNomCompte(),
                budget.getMontantPrevu(),
                consomme,
                budget.getMois(),
                budget.getAnnee()
        );
    }
}
