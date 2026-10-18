package com.miapeve.finance.repositories;

import com.miapeve.finance.entites.Depense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepenseRepository {
    Depense enregistrer(Depense depense);
    Optional<Depense> findById(Long id);
    List<Depense> findAll();
    List<Depense> findByCompte(Long compteId);
    void supprimer(Long id);
    Double getTotalDepensesByCompte(Long compteId);
    Double getTotalDepenses();
    List<Depense> getDepenseByMonth(LocalDate deb , LocalDate fin);
}