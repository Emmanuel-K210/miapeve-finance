package com.miapeve.finance.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.repositories.ProgrammeRepository;
import com.miapeve.finance.repositories.SemenceRepository;
import com.miapeve.finance.repositories.impl.ProgrammeRepositoryImpl;
import com.miapeve.finance.repositories.impl.SemenceRepositoryImpl;
import com.miapeve.finance.service.ProgrammeService;

public class ProgrammeServiceImpl implements ProgrammeService{
    private final ProgrammeRepository programmeRepository;
    private final SemenceRepository semenceRepository;
    
    public ProgrammeServiceImpl() {
        this.programmeRepository = new ProgrammeRepositoryImpl();
        this.semenceRepository = new SemenceRepositoryImpl();
    }
    
    
    @Override
    public Programme enregistrer(Programme programme) {
        return programmeRepository.enregistrer(programme);
    }
    
    @Override
    public Optional<Programme> findById(Long id) {
        return programmeRepository.findById(id);
    }
    
    @Override
    public List<Programme> findAll() {
        List<Programme>programmes= programmeRepository.findAll();
        return programmes;
    }
    
    @Override
    public List<Programme> findByDate(LocalDate date) {
        return programmeRepository.findByDate(date);
    }
    
    // === IMPLEMENTATION DES NOUVELLES METHODES ===
    
    @Override
    public List<Programme> listeDesProgrammesParMois(LocalDate date) {
        // Utiliser la date pour déterminer le mois
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate debutMois = yearMonth.atDay(1);
        LocalDate finMois = yearMonth.atEndOfMonth();
        
        return programmeRepository.findByPeriode(debutMois, finMois);
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
      
        return programmeRepository.listeDesProgrammesParMoisEtUtilisateur(date, utilisateurId);
    }
    
    @Override
    public Boolean supprimer(Long id) {
    	try {    		
    		programmeRepository.supprimer(id);
    		return true;
    	}catch (Exception e) {
			return false;
		}
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
        List<Programme> allProgrammes = programmeRepository.findAll();
        return allProgrammes.stream()
            .sorted((p1, p2) -> p2.getDateProgramme().compareTo(p1.getDateProgramme()))
            .limit(limit)
            .toList();
    }
    
    @Override
    public List<Programme> findByUtilisateur(Long utilisateurId) {
        return programmeRepository.findByUtilisateur(utilisateurId);
    }
    
    @Override
    public List<Programme> findByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        return programmeRepository.findByPeriode(dateDebut, dateFin);
    }


	@Override
	public double calculerTotalSemencesMoisEnCours() {
		
		return semenceRepository.calculerTotalSemencesMoisEnCours();
	}


	@Override
	public int compterSemencesMoisEnCours() {
		
		return semenceRepository.compterSemencesMoisEnCours();
	}


	@Override
	public List<Semence> trouverAvecSemences(Long id) {
		
		return semenceRepository.findByProgramme(id);
	}
}