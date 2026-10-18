package com.miapeve.finance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;

public interface ProgrammeService {
	  Programme enregistrer(Programme programme);
	    Optional<Programme> findById(Long id);
	    List<Programme> findAll();
	    List<Programme> findByDate(LocalDate date);
	    
	    // NOUVELLES METHODES
	    List<Programme> listeDesProgrammesParMois(LocalDate date);
	    List<Programme> listeDesProgrammesParMois(int mois, int annee);
	    List<Programme> listeDesProgrammesMoisEnCours();
	    List<Programme> listeDesProgrammesParMoisEtUtilisateur(LocalDate date, Long utilisateurId);
	    Boolean supprimer(Long id);
	    int countProgrammesParMois(LocalDate date);
	    int[] getStatistiquesMois(LocalDate date);
	    
	    // METHODES SUPPLEMENTAIRES
	    List<Programme> findProgrammesRecents(int limit);
	    List<Programme> findByUtilisateur(Long utilisateurId);
	    List<Programme> findByPeriode(LocalDate dateDebut, LocalDate dateFin);
		double calculerTotalSemencesMoisEnCours();
		int compterSemencesMoisEnCours();
		List<Semence> trouverAvecSemences(Long id);
}
