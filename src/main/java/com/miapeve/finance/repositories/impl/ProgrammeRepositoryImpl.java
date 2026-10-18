package com.miapeve.finance.repositories.impl;

import com.miapeve.finance.dao.ProgrammeDAO;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.repositories.ProgrammeRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class ProgrammeRepositoryImpl implements ProgrammeRepository {

    private final ProgrammeDAO programmeDAO;
    
    public ProgrammeRepositoryImpl() {
        this.programmeDAO = new ProgrammeDAO();
    }
    
    @Override
    public Programme enregistrer(Programme programme) {
        return programmeDAO.save(programme);
    }
    
    @Override
    public Optional<Programme> findById(Long id) {
        return programmeDAO.findById(id);
    }
    
    @Override
    public List<Programme> findAll() {
        return programmeDAO.findAll();
    }
    
    @Override
    public List<Programme> findByDate(LocalDate date) {
        return programmeDAO.findByDate(date);
    }
    
    // === IMPLEMENTATION DES NOUVELLES METHODES ===
    
    @Override
    public List<Programme> listeDesProgrammesParMois(LocalDate date) {
        // Utiliser la date pour déterminer le mois
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        return programmeDAO.findByPeriode(debutMois, finMois);
    }
    
    @Override
    public List<Programme> listeDesProgrammesParMois(int mois, int annee) {
        LocalDate date = LocalDate.of(annee, mois, 1);
        return listeDesProgrammesParMois(date);
    }
    
    @Override
    public List<Programme> listeDesProgrammesMoisEnCours() {
        return listeDesProgrammesParMois(LocalDate.now());
    }
    
    @Override
    public List<Programme> listeDesProgrammesParMoisEtUtilisateur(LocalDate date, Long utilisateurId) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        return programmeDAO.findByPeriodeEtUtilisateur(debutMois, finMois, utilisateurId);
    }
    
    @Override
    public void supprimer(Long id) {
        programmeDAO.delete(id);
    }
    
    @Override
    public int countProgrammesParMois(LocalDate date) {
        List<Programme> programmes = listeDesProgrammesParMois(date);
        return programmes.size();
    }
    
    @Override
    public int[] getStatistiquesMois(LocalDate date) {
        List<Programme> programmes = listeDesProgrammesParMois(date);
        
        int total = programmes.size();
        int avecSemences = (int) programmes.stream()
            .filter(p -> !p.getSemences().isEmpty())
            .count();
        int complets = (int) programmes.stream()
            .filter(p -> p.getHeureFin() != null && p.getHeureFin()!= null)
            .count();
            
        return new int[]{total, avecSemences, complets};
    }
    
    @Override
    public List<Programme> findProgrammesRecents(int limit) {
        List<Programme> allProgrammes = programmeDAO.findAll();
        return allProgrammes.stream()
            .sorted((p1, p2) -> p2.getDateProgramme().compareTo(p1.getDateProgramme()))
            .limit(limit)
            .toList();
    }
    
    @Override
    public List<Programme> findByUtilisateur(Long utilisateurId) {
        return programmeDAO.findByUtilisateur(utilisateurId);
    }
    
    @Override
    public List<Programme> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        return programmeDAO.findByPeriode(dateDebut, dateFin);
    }
}