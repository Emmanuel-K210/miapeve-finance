package com.miapeve.finance.repositories;

import com.miapeve.finance.entites.Compte;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CompteRepository {
    Compte enregistrer(Compte compte);
    Optional<Compte> findById(Long id);
    Optional<Compte> findByType(String typeCompte);
    List<Compte> findAll();
    void supprimer(Long id);
    boolean updateSolde(Long compteId, Double nouveauSolde);
    Double getSoldeTotal();
	void delete(Long id);
	boolean crediter(Long compteId, Double montant, Long userId);
	boolean debiter(Long compteId, Double montant, Long userId);
	boolean transfert(Long compteCrediterId,Long compteDebiterId,BigDecimal montant,Long utilisateurId);
}