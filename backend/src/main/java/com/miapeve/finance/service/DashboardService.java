package com.miapeve.finance.service;

import com.miapeve.finance.dto.CompteDTO;
import com.miapeve.finance.dto.DashboardSummaryDTO;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.DepenseRepository;
import com.miapeve.finance.repository.ProgrammeRepository;
import com.miapeve.finance.repository.SemenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final CompteRepository compteRepository;
    private final SemenceRepository semenceRepository;
    private final DepenseRepository depenseRepository;
    private final ProgrammeRepository programmeRepository;

    public DashboardService(CompteRepository compteRepository,
                             SemenceRepository semenceRepository,
                             DepenseRepository depenseRepository,
                             ProgrammeRepository programmeRepository) {
        this.compteRepository = compteRepository;
        this.semenceRepository = semenceRepository;
        this.depenseRepository = depenseRepository;
        this.programmeRepository = programmeRepository;
    }

    public DashboardSummaryDTO getSummary() {
        List<CompteDTO> comptes = compteRepository.findAll().stream()
                .map(c -> new CompteDTO(c.getId(), c.getNomCompte(), c.getTypeCompte(), c.getSolde()))
                .toList();

        double soldeTotal = comptes.stream().mapToDouble(CompteDTO::solde).sum();
        double totalSemences = semenceRepository.findAll().stream().mapToDouble(s -> s.getMontant()).sum();
        double totalDepenses = depenseRepository.findAll().stream().mapToDouble(d -> d.getMontant()).sum();

        return new DashboardSummaryDTO(
                soldeTotal,
                totalSemences,
                totalDepenses,
                comptes.size(),
                (int) programmeRepository.count(),
                comptes
        );
    }
}
