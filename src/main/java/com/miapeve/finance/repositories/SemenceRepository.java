package com.miapeve.finance.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Semence;

public interface SemenceRepository {
    Semence enregistrer(Semence semence);
    Optional<Semence> findById(Long id);
    List<Semence> findAll();
    List<Semence> findByProgramme(Long programmeId);
    List<Semence> findByCompte(Long compteId);
    void supprimer(Long id);
    Double getTotalSemencesByCompte(Long compteId);
    double calculerTotalSemencesMoisEnCours();
    int compterSemencesMoisEnCours();
    List<Semence> findByPeriode(LocalDate deb, LocalDate fin);
}