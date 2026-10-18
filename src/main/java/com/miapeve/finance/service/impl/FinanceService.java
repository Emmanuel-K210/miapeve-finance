package com.miapeve.finance.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.dao.TransactionDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Depense;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.entites.Transaction;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.enums.TypeCompte;
import com.miapeve.finance.enums.TypeTransaction;
import com.miapeve.finance.repositories.CompteRepository;
import com.miapeve.finance.repositories.DepenseRepository;
import com.miapeve.finance.repositories.ProgrammeRepository;
import com.miapeve.finance.repositories.SemenceRepository;
import com.miapeve.finance.repositories.UtilisateurRepository;
import com.miapeve.finance.repositories.impl.CompteRepositoryImpl;
import com.miapeve.finance.repositories.impl.DepenseRepositoryImpl;
import com.miapeve.finance.repositories.impl.ProgrammeRepositoryImpl;
import com.miapeve.finance.repositories.impl.SemenceRepositoryImpl;
import com.miapeve.finance.repositories.impl.UtilisateurRepositoryImpl;

public class FinanceService {
    private CompteRepository compteRepo;
    private DepenseRepository depenseRepo;
    private SemenceRepository semenceRepo;
    private ProgrammeRepository programmeRepo;
    private UtilisateurRepository utilisateurRepo;
    private final TransactionDAO transactionDAO;
    
    
    public FinanceService() {
        this.compteRepo = new CompteRepositoryImpl();
        this.depenseRepo = new DepenseRepositoryImpl();
        this.semenceRepo = new SemenceRepositoryImpl();
        this.programmeRepo = new ProgrammeRepositoryImpl();
        this.utilisateurRepo = new UtilisateurRepositoryImpl();
        transactionDAO = new TransactionDAO();
    }
    
    public Compte findCompteByType(TypeCompte type) {
    	return compteRepo.findByType(type.name()).orElseThrow();
    }
    
    // === METHODES COMPTES ===
    public Double getSoldeTotal() {
        return compteRepo.getSoldeTotal();
    }
    
    public List<Compte> getAllComptes() {
        return compteRepo.findAll();
    }
    
    public List<Utilisateur> getUtilisateurs(){
    	return utilisateurRepo.findAll();
    }
    
    public boolean crediterCompte(Long compteId, Double montant,Utilisateur usr) {
        return compteRepo.crediter(compteId, montant,usr.getId());
    }
    
    public boolean debiterCompte(Long compteId, Double montant,Utilisateur usr) {
        return compteRepo.debiter(compteId, montant,usr.getId());
    }
    
    public Compte createCompte(Compte compte) {
        return compteRepo.enregistrer(compte);
    }
    
    // === METHODES DEPENSES ===
    public Double getTotalDepenses() {
        return depenseRepo.getTotalDepenses();
    }
    
    public List<Depense> getAllDepenses() {
        return depenseRepo.findAll();
    }
    
    
    public List<Semence> getSemenceByPeriode(LocalDate deb,LocalDate fin){
    	if(semenceRepo.findByPeriode(deb, fin).isEmpty()) {
    		return List.of();
    	}
    	return semenceRepo.findByPeriode(deb, fin);
    }
    
    public List<Depense> getDepensesByCompte(Long compteId) {
        return depenseRepo.findByCompte(compteId);
    }
    
    public double getMontantByCompte(TypeCompte compteType) throws Exception {
    	return compteRepo
    			.findByType(compteType.name())
    			.orElseThrow(()->new Exception("Se compte n'existe pas"))
    			.getSolde()
    			.doubleValue();
    }
    
    public List<Depense> getdepenseByMonth(LocalDate deb , LocalDate fin){
    	return depenseRepo.getDepenseByMonth(deb, fin);
    }
    
    public Depense createDepense(Depense depense) {
        return depenseRepo.enregistrer(depense);
    }
    
    public boolean deleteDepense(Long depenseId) {
        try {
            depenseRepo.supprimer(depenseId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // === METHODES SEMENCES ===
    public Double getTotalSemences() {
        List<Compte> comptes = compteRepo.findAll();
        return comptes.stream()
                .mapToDouble(compte -> semenceRepo.getTotalSemencesByCompte(compte.getCompteId()))
                .sum();
    }
    
    
   
    
    public List<Semence> getAllSemences() {
        return semenceRepo.findAll();
    }
    
    public List<Semence> getSemencesByProgramme(Long programmeId) {
        return semenceRepo.findByProgramme(programmeId);
    }
    
    public Semence createSemence(Semence semence) {
        return semenceRepo.enregistrer(semence);
    }
    
    
 // === METHODES PROGRAMMES COMPLETES ===
    public List<Programme> getProgrammesMoisEnCours() {
        return programmeRepo.listeDesProgrammesMoisEnCours();
    }
    
    public List<Programme> getProgrammesParMois(LocalDate date) {
        return programmeRepo.listeDesProgrammesParMois(date);
    }
    
    public List<Programme> getProgrammesParMois(int mois, int annee) {
        return programmeRepo.listeDesProgrammesParMois(mois, annee);
    }
    
    public List<Programme> getProgrammesParMoisEtUtilisateur(LocalDate date, Long utilisateurId) {
        return programmeRepo.listeDesProgrammesParMoisEtUtilisateur(date, utilisateurId);
    }
    
    public List<Programme> getProgrammesRecents(int limit) {
        return programmeRepo.findProgrammesRecents(limit);
    }
    
    public int getNombreProgrammesMoisEnCours() {
        return programmeRepo.countProgrammesParMois(LocalDate.now());
    }
    
    public Map<String, Integer> getStatistiquesProgrammesMois(LocalDate date) {
        int[] stats = programmeRepo.getStatistiquesMois(date);
        Map<String, Integer> statistiques = new HashMap<>();
        statistiques.put("total", stats[0]);
        statistiques.put("avec_semences", stats[1]);
        statistiques.put("complets", stats[2]);
        return statistiques;
    }
    
    public Programme creerProgramme(Programme programme) {
        return programmeRepo.enregistrer(programme);
    }
    
    public boolean supprimerProgramme(Long programmeId) {
        try {
            programmeRepo.supprimer(programmeId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean transfert(Long compteCreditId,Long compteDebiterId,BigDecimal montant,Long utilisateurId) {
    	return compteRepo.transfert(compteCreditId, compteDebiterId, montant,utilisateurId);
    }
    
    public List<Depense> getDepenseMonthCurrent(){
    	return getAllDepenses().stream()
    			.filter(dep->dep.getDateDepense().getMonthValue() == LocalDate.now().getMonthValue())
    			.toList();
    }
    
 
    // ==================== NOUVELLES MÉTHODES POUR L'IMPRESSION ====================
    
    /**
     * Récupère le solde d'un compte spécifique
     */
    public Double getSoldeCompte(String typeCompte) {
        try {
            Compte compte = compteRepo.findByType(typeCompte).get();
            return compte != null && compte.getSolde() != null ? 
                   compte.getSolde().doubleValue() : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Calcule le total des semences sur une période
     */
    public Double getTotalSemencesPeriode(LocalDate debut, LocalDate fin) {
        try {
            return transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.CREDIT)
                    .mapToDouble(t -> t.getMontant().doubleValue())
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Calcule le total des dépenses sur une période
     */
    public Double getTotalDepensesPeriode(LocalDate debut, LocalDate fin) {
        try {
            return transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.DEBIT)
                    .mapToDouble(t -> t.getMontant().doubleValue())
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Récupère le total des dépenses pour un compte spécifique sur une période
     */
    public Double getTotalDepensesByCompte(String typeCompte, LocalDate debut, LocalDate fin) {
        try {
            Compte compte = compteRepo.findByType(typeCompte).get();
            if (compte == null) return 0.0;
            
            return transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.DEBIT)
                    .filter(t -> t.getCompteId() != null && t.getCompteId().equals(compte.getCompteId()))
                    .mapToDouble(t -> t.getMontant().doubleValue())
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Récupère lE TOTAL des semences sur une période
     */
    public double getValueSemencesTotalByPeriode(LocalDate debut, LocalDate fin) {
        try {
            return transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.CREDIT)
                    .map(Transaction::getMontant)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Récupère lE TOTAL des dépenses sur une période
     */
    public double getTotalDepensesByPeriode(LocalDate debut, LocalDate fin) {
        try {
            return transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.DEBIT)
                    .map(Transaction::getMontant)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    
    public BigDecimal getCumulCreditCompteUntilDateTime(LocalDateTime dateTime, TypeCompte type) throws Exception {
        return transactionDAO.findCumulByCompteAndTypeAndDateTime(type, TypeTransaction.CREDIT, dateTime);
    }
    
    public BigDecimal getCumulDebitCompteUntilDateTime(LocalDateTime dateTime, TypeCompte type) throws Exception {
        return transactionDAO.findCumulByCompteAndTypeAndDateTime(type, TypeTransaction.DEBIT, dateTime);
    }

    /**
     * Calcule le solde net (semences - dépenses) sur une période
     */
    public Double getSoldeNetPeriode(LocalDate debut, LocalDate fin) {
        try {
            double totalSemences = getTotalSemencesPeriode(debut, fin);
            double totalDepenses = getTotalDepensesPeriode(debut, fin);
            return totalSemences - totalDepenses;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Calcule le taux d'épargne sur une période
     */
    public Double getTauxEpargnePeriode(LocalDate debut, LocalDate fin) {
        try {
            double totalSemences = getTotalSemencesPeriode(debut, fin);
            if (totalSemences == 0) return 0.0;
            
            double soldeNet = getSoldeNetPeriode(debut, fin);
            return (soldeNet / totalSemences) * 100;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Récupère les transactions Annuelles
     */
    public List<Transaction> getTransactionsParMois(int annee) {
        try {
            LocalDate debutAnnee = LocalDate.of(annee, 1, 1);
            LocalDate finAnnee = LocalDate.of(annee, 12, 31);
            
            return transactionDAO.findByPeriode(debutAnnee.atStartOfDay(), finAnnee.atStartOfDay());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Recupere les transaction Du mois en cours
     */
    
    public List<Transaction> getTransactionParMoisEnCours() {
        try {
            LocalDate maintenant = LocalDate.now();
            
            // Premier jour du mois à 00:00:00
            LocalDate debutMois = LocalDate.of(maintenant.getYear(), maintenant.getMonth(), 1);
            
            // Dernier jour du mois à 23:59:59
            LocalDate finMois = maintenant; // Jour actuel
            
            return transactionDAO.findByPeriode(
                debutMois.atStartOfDay(),           // 2025-01-01 00:00:00
                finMois.atTime(23, 59, 59)          // 2025-01-23 23:59:59
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<Transaction> getAllTransactions(){
    	return transactionDAO.findAll();
    }
    
 
    
    /**
     * Vérifie si une période a des transactions
     */
    public boolean periodeHasTransactions(LocalDate debut, LocalDate fin) {
        try {
            return !transactionDAO.findByPeriode(debut.atStartOfDay(), fin.atStartOfDay()).isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Compte> getComptesMouvementes() {
        try {
            // Option A – via CompteDAO (recommandé)
            CompteDAO compteDAO = new CompteDAO();
            return compteDAO.findComptesMouvementes();

        } catch (Exception e) {
            System.err.println("❌ Erreur getComptesMouvementes: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
 
}