package com.miapeve.finance.service;

import com.miapeve.finance.dto.*;
import com.miapeve.finance.entity.Depense;
import com.miapeve.finance.entity.Semence;
import com.miapeve.finance.entity.Transaction;
import com.miapeve.finance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private final CompteRepository compteRepository;
    private final SemenceRepository semenceRepository;
    private final DepenseRepository depenseRepository;
    private final ProgrammeRepository programmeRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashboardService(CompteRepository compteRepository,
                             SemenceRepository semenceRepository,
                             DepenseRepository depenseRepository,
                             ProgrammeRepository programmeRepository,
                             TransactionRepository transactionRepository,
                             BudgetService budgetService) {
        this.compteRepository = compteRepository;
        this.semenceRepository = semenceRepository;
        this.depenseRepository = depenseRepository;
        this.programmeRepository = programmeRepository;
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        List<CompteDTO> comptes = compteRepository.findAll().stream()
                .map(c -> new CompteDTO(c.getId(), c.getNomCompte(), c.getTypeCompte(), c.getSolde()))
                .toList();

        List<Semence> semences = semenceRepository.findAll();
        List<Depense> depenses = depenseRepository.findAll();

        double soldeTotal = comptes.stream().mapToDouble(CompteDTO::solde).sum();
        double totalSemences = semences.stream().mapToDouble(Semence::getMontant).sum();
        double totalDepenses = depenses.stream().mapToDouble(Depense::getMontant).sum();

        return new DashboardSummaryDTO(
                soldeTotal,
                totalSemences,
                totalDepenses,
                comptes.size(),
                (int) programmeRepository.count(),
                comptes,
                buildEvolution(semences, depenses),
                buildActivites(semences, depenses, transactionRepository.findAllByOrderByDateCreationDesc()),
                budgetService.findForCurrentMonth()
        );
    }

    private List<MonthlyPointDTO> buildEvolution(List<Semence> semences, List<Depense> depenses) {
        List<MonthlyPointDTO> points = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth mois = current.minusMonths(i);
            double sommeSemences = semences.stream()
                    .filter(s -> s.getDateSemence() != null && YearMonth.from(s.getDateSemence()).equals(mois))
                    .mapToDouble(Semence::getMontant)
                    .sum();
            double sommeDepenses = depenses.stream()
                    .filter(d -> d.getDateDepense() != null && YearMonth.from(d.getDateDepense()).equals(mois))
                    .mapToDouble(Depense::getMontant)
                    .sum();

            String label = mois.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            points.add(new MonthlyPointDTO(label, sommeSemences, sommeDepenses));
        }

        return points;
    }

    private List<ActivityDTO> buildActivites(List<Semence> semences, List<Depense> depenses, List<Transaction> transactions) {
        List<ActivityDTO> activites = new ArrayList<>();

        transactions.forEach(t -> {
            boolean isCredit = "CREDIT".equals(t.getTypeTransaction());
            activites.add(new ActivityDTO(
                    "TRANSACTION",
                    (isCredit ? "Entrée" : "Sortie") + " — " + t.getCompte().getNomCompte(),
                    t.getDescription() != null && !t.getDescription().isBlank() ? t.getDescription() : "Transaction manuelle",
                    t.getMontant(),
                    isCredit,
                    t.getDateCreation()
            ));
        });

        semences.forEach(s -> activites.add(new ActivityDTO(
                "SEMENCE",
                "Semence — " + s.getCompte().getNomCompte(),
                s.getDescription() != null && !s.getDescription().isBlank() ? s.getDescription() : s.getTypeSemence(),
                s.getMontant(),
                true,
                s.getDateCreation() != null ? s.getDateCreation() : dateToDateTime(s.getDateSemence())
        )));

        depenses.forEach(d -> activites.add(new ActivityDTO(
                "DEPENSE",
                "Dépense — " + d.getCompte().getNomCompte(),
                d.getLibelle(),
                d.getMontant(),
                false,
                d.getDateCreation() != null ? d.getDateCreation() : dateToDateTime(d.getDateDepense())
        )));

        return activites.stream()
                .sorted((a, b) -> {
                    if (a.date() == null || b.date() == null) return 0;
                    return b.date().compareTo(a.date());
                })
                .limit(8)
                .toList();
    }

    private java.time.LocalDateTime dateToDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }
}
