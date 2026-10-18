package com.miapeve.finance.service;

import com.miapeve.finance.dto.SemenceDTO;
import com.miapeve.finance.dto.SemenceRequest;
import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.entity.Programme;
import com.miapeve.finance.entity.Semence;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.ProgrammeRepository;
import com.miapeve.finance.repository.SemenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SemenceService {

    private final SemenceRepository semenceRepository;
    private final CompteRepository compteRepository;
    private final ProgrammeRepository programmeRepository;

    public SemenceService(SemenceRepository semenceRepository, CompteRepository compteRepository, ProgrammeRepository programmeRepository) {
        this.semenceRepository = semenceRepository;
        this.compteRepository = compteRepository;
        this.programmeRepository = programmeRepository;
    }

    @Transactional(readOnly = true)
    public List<SemenceDTO> findAll() {
        return semenceRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SemenceDTO> findByCompte(Long compteId) {
        return semenceRepository.findByCompteId(compteId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public SemenceDTO create(SemenceRequest request, Long utilisateurId) {
        Compte compte = compteRepository.findById(request.compteId())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + request.compteId()));
        Programme programme = programmeRepository.findById(request.programmeId())
                .orElseThrow(() -> new IllegalArgumentException("Programme introuvable: " + request.programmeId()));

        compte.setSolde(compte.getSolde() + request.montant());
        compteRepository.save(compte);

        Semence semence = new Semence();
        semence.setTypeSemence(request.typeSemence());
        semence.setMontant(request.montant());
        semence.setDateSemence(request.dateSemence());
        semence.setDescription(request.description());
        semence.setProgramme(programme);
        semence.setCompte(compte);
        semence.setCreePar(utilisateurId);

        return toDTO(semenceRepository.save(semence));
    }

    @Transactional
    public void delete(Long id) {
        Semence semence = semenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semence introuvable: " + id));

        Compte compte = semence.getCompte();
        compte.setSolde(compte.getSolde() - semence.getMontant());
        compteRepository.save(compte);

        semenceRepository.delete(semence);
    }

    private SemenceDTO toDTO(Semence semence) {
        return new SemenceDTO(
                semence.getId(),
                semence.getTypeSemence(),
                semence.getMontant(),
                semence.getDateSemence(),
                semence.getDescription(),
                semence.getProgramme().getId(),
                semence.getProgramme().getTypeProgramme(),
                semence.getCompte().getId(),
                semence.getCompte().getNomCompte()
        );
    }
}
