package com.miapeve.finance.service;

import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface CompteService {
    Compte enregistrer(Compte compte);
    Optional<Compte> findById(Long id);
    Optional<Compte> findByType(String typeCompte);
    List<Compte> findAll();
    void supprimer(Long id);
    boolean updateSolde(Long compteId, Double nouveauSolde);
    Double getSoldeTotal();
	boolean crediter(Long compteId, Double montant, Utilisateur utilisateur);
	boolean debiter(Long compteId, Double montant, Utilisateur utilisateur);
}