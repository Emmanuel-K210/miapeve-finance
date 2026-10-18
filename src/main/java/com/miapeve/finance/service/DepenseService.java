package com.miapeve.finance.service;

import com.miapeve.finance.entites.Depense;
import java.util.List;
import java.util.Optional;

public interface DepenseService {
    Depense enregistrer(Depense depense);
    Optional<Depense> findById(Long id);
    List<Depense> findAll();
    List<Depense> findByCompte(Long compteId);
    List<Depense> findByLibelle(String libelle);
    void supprimer(Long id);
    Double getTotalDepensesByCompte(Long compteId);
    Double getTotalDepenses();
}