package com.miapeve.finance.service.impl;

import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.repositories.CompteRepository;
import com.miapeve.finance.repositories.impl.CompteRepositoryImpl;
import com.miapeve.finance.service.CompteService;

public class CompteServiceImpl implements CompteService {

    private final CompteRepository compteRepository;
    
    public CompteServiceImpl() {
        this.compteRepository = new CompteRepositoryImpl();
    }
    
    @Override
    public Compte enregistrer(Compte compte) {
        return compteRepository.enregistrer(compte);
    }
    
    @Override
    public Optional<Compte> findById(Long id) {
        return compteRepository.findById(id);
    }
    
    @Override
    public Optional<Compte> findByType(String typeCompte) {
        return compteRepository.findByType(typeCompte);
    }
    
    @Override
    public List<Compte> findAll() {
        return compteRepository.findAll();
    }
    
    @Override
    public void supprimer(Long id) {
    	compteRepository.delete(id);
    }
    
    @Override
    public boolean crediter(Long compteId, Double montant,Utilisateur utilisateur) {
        return compteRepository.crediter(compteId, montant,utilisateur.getId());
    }
    
    @Override
    public boolean debiter(Long compteId, Double montant,Utilisateur utilisateur) {
        return compteRepository.debiter(compteId, montant,utilisateur.getId());
    }
    
    @Override
    public boolean updateSolde(Long compteId, Double nouveauSolde) {
        return compteRepository.updateSolde(compteId, nouveauSolde);
    }
    
    @Override
    public Double getSoldeTotal() {
        return compteRepository.findAll().stream()
                .mapToDouble(compte->compte.getSolde().doubleValue())
                .sum();
    }
}