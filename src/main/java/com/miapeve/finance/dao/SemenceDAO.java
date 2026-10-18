package com.miapeve.finance.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Programme;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.enums.TypeSemence;
import com.miapeve.finance.enums.TypeTransaction;
import com.miapeve.finance.utils.DateConverter;

public class SemenceDAO extends BaseDAO {
    
    public Optional<Semence> findById(Long id) {
        String sql = """
            SELECT s.*, 
                   p.programme_id, p.type_programme as programme_libelle, p.date_programme, p.heure_debut, p.heure_fin,
                   c.compte_id, c.type_compte, c.solde
            FROM semences s
            INNER JOIN programmes p ON s.programme_id = p.programme_id
            INNER JOIN comptes c ON s.compte_id = c.compte_id
            WHERE s.semence_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToSemence(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    
    public double calculerTotalSemencesMoisEnCours() {
        String sql = "SELECT COALESCE(SUM(s.montant), 0) as total " +
                     "FROM semences s " +
                     "JOIN programmes p ON s.programme_id = p.programme_id " +
                     "WHERE strftime('%Y-%m', p.date_programme) = strftime('%Y-%m', 'now')";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul total semences: " + e.getMessage());
            return 0.0;
        }
    }


    public int compterSemencesMoisEnCours() {
        String sql = "SELECT COUNT(s.semence_id) as count " +
                     "FROM semences s " +
                     "JOIN programmes p ON s.programme_id = p.programme_id " +
                     "WHERE strftime('%Y-%m', p.date_programme) = strftime('%Y-%m', 'now')";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage semences: " + e.getMessage());
            return 0;
        }
    }
    
    public List<Semence> findAll() {
        List<Semence> semences = new ArrayList<>();
        String sql = """
            SELECT s.*, 
                   p.programme_id, p.type_programme , p.date_programme, p.heure_debut, p.heure_fin,
                   c.compte_id, c.type_compte, c.solde
            FROM semences s
            INNER JOIN programmes p ON s.programme_id = p.programme_id
            INNER JOIN comptes c ON s.compte_id = c.compte_id
            ORDER BY s.semence_id DESC
            """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                semences.add(mapResultSetToSemence(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return semences;
    }
    
    public List<Semence> findByProgramme(Long programmeId) {
        List<Semence> semences = new ArrayList<>();
        String sql = """
            SELECT s.*, 
                   p.programme_id, p.type_programme, p.date_programme, p.heure_debut, p.heure_fin,
                   c.compte_id, c.type_compte, c.solde
            FROM semences s
            INNER JOIN programmes p ON s.programme_id = p.programme_id
            INNER JOIN comptes c ON s.compte_id = c.compte_id
            WHERE s.programme_id = ?
            ORDER BY s.type_semence
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, programmeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                semences.add(mapResultSetToSemence(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return semences;
    }
    
    public List<Semence> findByCompte(Long compteId) {
        List<Semence> semences = new ArrayList<>();
        String sql = """
            SELECT s.*, 
                   p.programme_id, p.type_programme, p.date_programme, p.heure_debut, p.heure_fin,
                   c.compte_id, c.type_compte, c.solde
            FROM semences s
            INNER JOIN programmes p ON s.programme_id = p.programme_id
            INNER JOIN comptes c ON s.compte_id = c.compte_id
            WHERE s.compte_id = ?
            ORDER BY s.semence_id DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                semences.add(mapResultSetToSemence(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return semences;
    }
    
    public Double getTotalSemencesByCompte(Long compteId) {
        String sql = "SELECT SUM(montant) as total FROM semences WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public Semence save(Semence semence) {
        if (semence.getSemenceId() == null) {
            return insert(semence);
        } else {
            return update(semence);
        }
    }
    
 // Dans la méthode insert de SemenceDAO
    private Semence insert(Semence semence) {
        String sql = """
            INSERT INTO semences 
            (type_semence, montant, date_semence, programme_id, compte_id, 
             cree_par, date_creation) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, semence.getTypeSemence().name());
            stmt.setBigDecimal(2, semence.getMontant());
            stmt.setString(3, semence.getDateSemence().toString()); // ✅ LocalDate
            stmt.setLong(4, semence.getProgramme().getId());
            stmt.setLong(5, semence.getCompte().getCompteId());
            stmt.setLong(6, semence.getCreePar()); // ✅ Audit
            stmt.setString(7, LocalDateTime.now().toString()); // ✅ Audit
            
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                semence.setSemenceId(generatedKeys.getLong(1));
                
                // Mettre à jour le solde du compte et créer une transaction
                CompteDAO compteDAO = new CompteDAO();
                compteDAO.crediter(semence.getCompte().getCompteId(), semence.getMontant(), semence.getCreePar());
                
                // Créer une transaction automatique
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                		TypeTransaction.CREDIT, semence.getMontant(), semence.getCompte().getCompteId(),
                    "Semence: " + semence.getTypeSemence(), semence.getCreePar()
                );
            }
            
            System.out.println("✅ Semence enregistrée: " + semence.getTypeSemence() + " - " + semence.getMontant() + " F CFA");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert semence: " + e.getMessage());
            e.printStackTrace();
        }
        return semence;
    }
    
    private Semence update(Semence semence) {
        // Récupérer l'ancien montant pour ajuster le solde
        Optional<Semence> ancienneSemence = findById(semence.getSemenceId());
        
        String sql = """
            UPDATE semences 
            SET type_semence = ?, montant = ?, programme_id = ?, compte_id = ?,
                modifie_par = ?, date_modification = ?
            WHERE semence_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, semence.getTypeSemence().name());
            stmt.setBigDecimal(2, semence.getMontant()); // ✅ BigDecimal
            stmt.setLong(3, semence.getProgramme().getId());
            stmt.setLong(4, semence.getCompte().getCompteId());
            stmt.setLong(5, semence.getModifiePar()); // ✅ Utilisateur qui modifie
            stmt.setString(6, java.time.LocalDateTime.now().toString());
            stmt.setLong(7, semence.getSemenceId());
            
            stmt.executeUpdate();
            
            // Ajuster le solde du compte si le montant a changé
            if (ancienneSemence.isPresent()) {
                BigDecimal ancienMontant = ancienneSemence.get().getMontant();
                BigDecimal nouveauMontant = semence.getMontant();
                BigDecimal difference = nouveauMontant.subtract(ancienMontant);
                
                if (difference.compareTo(BigDecimal.ZERO) != 0) {
                    CompteDAO compteDAO = new CompteDAO();
                    Long utilisateurId = semence.getModifiePar(); // ✅ Utilisateur connecté
                    
                    if (difference.compareTo(BigDecimal.ZERO) > 0) {
                        // Montant augmenté -> créditer la différence
                        compteDAO.crediter(semence.getCompte().getCompteId(), difference, utilisateurId);
                    } else {
                        // Montant diminué -> débiter la différence (valeur absolue)
                        compteDAO.debiter(semence.getCompte().getCompteId(), difference.abs(), utilisateurId);
                    }
                    
                    // Créer une transaction pour l'ajustement
                    TransactionDAO transactionDAO = new TransactionDAO();
                    TypeTransaction type = difference.compareTo(BigDecimal.ZERO) > 0 ?   TypeTransaction.CREDIT :   TypeTransaction.DEBIT;
                  
                    String description = "Ajustement semence: " + semence.getTypeSemence();
                    transactionDAO.creerTransactionAutomatique(type, difference.abs(), 
                        semence.getCompte().getCompteId(), description, utilisateurId);
                }
            }
            
            System.out.println("✅ Semence mise à jour: " + semence.getTypeSemence());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur update semence: " + e.getMessage());
            e.printStackTrace();
        }
        return semence;
    }
    
    public List<Semence> findByPeriode(LocalDateTime deb,LocalDateTime fin){
    	List<Semence> semences = new ArrayList<Semence>();
    	String sql = """
    			SELECT s.*, 
                   p.programme_id, p.type_programme, p.date_programme, p.heure_debut, p.heure_fin,
                   c.compte_id, c.type_compte, c.solde
            FROM semences s
            INNER JOIN programmes p ON s.programme_id = p.programme_id
            INNER JOIN comptes c ON s.compte_id = c.compte_id
            WHERE s.date_creation BETWEEN ? AND ?
            ORDER BY s.semence_id DESC
    			""";
    	try(
    			Connection conn = getConnection();
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql);
    			){
    			while(rs.next()) semences.add(mapResultSetToSemence(rs));
    	}catch (SQLException e) {
			e.printStackTrace();
		}
    	return semences;
    }
    
    public void delete(Long id, Long utilisateurId) {
        // ✅ Ajout du paramètre utilisateurId
        // Récupérer la semence avant suppression pour ajuster le solde
        Optional<Semence> semence = findById(id);
        
        String sql = "DELETE FROM semences WHERE semence_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
            // Débiter le compte (annuler le crédit initial)
            if (semence.isPresent()) {
                CompteDAO compteDAO = new CompteDAO();
                compteDAO.debiter(semence.get().getCompte().getCompteId(), 
                    semence.get().getMontant(), utilisateurId);
                
                // Créer une transaction pour la suppression
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                   TypeTransaction.DEBIT, semence.get().getMontant(), 
                    semence.get().getCompte().getCompteId(),
                    "Suppression semence: " + semence.get().getTypeSemence(), 
                    utilisateurId
                );
            }
            
            System.out.println("✅ Semence supprimée: " + id + " par utilisateur: " + utilisateurId);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete semence: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void delete(Long id) {
        // Utiliser une valeur par défaut (à remplacer par l'utilisateur connecté)
        delete(id, 1L); // 1 = admin par défaut
    }
    
    
    private Semence mapResultSetToSemence(ResultSet rs) throws SQLException {
        Semence semence = new Semence();
        semence.setSemenceId(rs.getLong("semence_id"));
        semence.setTypeSemence(TypeSemence.valueOf(rs.getString("type_semence")));
        semence.setMontant(new BigDecimal(rs.getDouble("montant")));
        
        // Mapping Programme
        Programme programme = new Programme();
        programme.setId(rs.getLong("programme_id"));
        programme.setTypeProgramme(rs.getString("type_programme"));
        programme.setDateProgramme(DateConverter.stringToLocalDate(rs.getString("date_programme")));
        programme.setHeureDebut(DateConverter.stringToLocalTime(rs.getString("heure_debut")));
        programme.setHeureFin(DateConverter.stringToLocalTime(rs.getString("heure_fin")));
        semence.setProgramme(programme);
        
        // Mapping Compte
        Compte compte = new Compte();
        compte.setCompteId(rs.getLong("compte_id"));
        compte.setTypeCompte(rs.getString("type_compte"));
        compte.setSolde(new BigDecimal(rs.getDouble("solde")));
        semence.setCompte(compte);
        
        return semence;
    }
}