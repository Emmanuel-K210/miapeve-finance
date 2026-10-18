package com.miapeve.finance.service;

import com.miapeve.finance.entites.Semence;
import java.util.List;
import java.util.Optional;

public interface SemenceService {
    Semence enregistrer(Semence semence);
    Optional<Semence> findById(Long id);
    List<Semence> findAll();
    List<Semence> findByProgramme(Long programmeId);
    List<Semence> findByCompte(Long compteId);
    void supprimer(Long id);
    Double getTotalSemencesByCompte(Long compteId);
}