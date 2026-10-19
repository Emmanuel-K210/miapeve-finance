package com.miapeve.finance.service;

import com.miapeve.finance.dto.DepenseDTO;
import com.miapeve.finance.dto.DepenseRequest;
import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.entity.Depense;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.DepenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final CompteRepository compteRepository;

    public DepenseService(DepenseRepository depenseRepository, CompteRepository compteRepository) {
        this.depenseRepository = depenseRepository;
        this.compteRepository = compteRepository;
    }

    @Transactional(readOnly = true)
    public List<DepenseDTO> findAll() {
        return depenseRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<DepenseDTO> findByCompte(Long compteId) {
        return depenseRepository.findByCompteId(compteId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public DepenseDTO create(DepenseRequest request, Long utilisateurId) {
        Compte compte = compteRepository.findById(request.compteId())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable: " + request.compteId()));

        double nouveauSolde = compte.getSolde() - request.montant();
        if (nouveauSolde < 0) {
            throw new IllegalArgumentException("Solde insuffisant sur le compte '" + compte.getNomCompte() + "'");
        }

        compte.setSolde(nouveauSolde);
        compteRepository.save(compte);

        Depense depense = new Depense();
        depense.setLibelle(request.libelle());
        depense.setMontant(request.montant());
        depense.setDateDepense(request.dateDepense());
        depense.setCompte(compte);
        depense.setCreePar(utilisateurId);

        return toDTO(depenseRepository.save(depense));
    }

    @Transactional
    public void delete(Long id) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dépense introuvable: " + id));

        Compte compte = depense.getCompte();
        compte.setSolde(compte.getSolde() + depense.getMontant());
        compteRepository.save(compte);

        depenseRepository.delete(depense);
    }

    private DepenseDTO toDTO(Depense depense) {
        return new DepenseDTO(
                depense.getId(),
                depense.getLibelle(),
                depense.getMontant(),
                depense.getDateDepense(),
                depense.getCompte().getId(),
                depense.getCompte().getNomCompte()
        );
    }
}
