package com.miapeve.finance.service;

import com.miapeve.finance.dto.CompteDTO;
import com.miapeve.finance.dto.CompteRequest;
import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.repository.CompteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompteService {

    private final CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    public List<CompteDTO> findAll() {
        return compteRepository.findAll().stream().map(this::toDTO).toList();
    }

    public CompteDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    public CompteDTO create(CompteRequest request, Long utilisateurId) {
        Compte compte = new Compte();
        compte.setNomCompte(request.nomCompte());
        compte.setTypeCompte(request.typeCompte());
        compte.setSolde(request.solde());
        compte.setCreePar(utilisateurId);
        return toDTO(compteRepository.save(compte));
    }

    public CompteDTO update(Long id, CompteRequest request, Long utilisateurId) {
        Compte compte = getOrThrow(id);
        compte.setNomCompte(request.nomCompte());
        compte.setTypeCompte(request.typeCompte());
        compte.setSolde(request.solde());
        compte.setModifiePar(utilisateurId);
        compte.setDateModification(LocalDateTime.now());
        return toDTO(compteRepository.save(compte));
    }

    public void delete(Long id) {
        compteRepository.delete(getOrThrow(id));
    }

    private Compte getOrThrow(Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + id));
    }

    private CompteDTO toDTO(Compte compte) {
        return new CompteDTO(compte.getId(), compte.getNomCompte(), compte.getTypeCompte(), compte.getSolde());
    }
}
