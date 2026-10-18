package com.miapeve.finance.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.entites.Depense;
import com.miapeve.finance.enums.TypeTransaction;

public class DepenseDAO extends BaseDAO {
    
    public Optional<Depense> findById(Long id) {
        String sql = """
            SELECT d.*, c.compte_id, c.nom_compte, c.type_compte, c.solde
            FROM depenses d
            INNER JOIN comptes c ON d.compte_id = c.compte_id
            WHERE d.depense_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToDepense(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findById Depense: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Depense> getDepenseByMonth(LocalDate debut, LocalDate fin) {
        List<Depense> depenses = new ArrayList<>();
        
        String sql = """
            SELECT d.*, c.compte_id, c.nom_compte, c.type_compte, c.solde
            FROM depenses d
            INNER JOIN comptes c ON d.compte_id = c.compte_id
            WHERE d.date_depense BETWEEN ? AND ?
            ORDER BY d.date_depense DESC, d.depense_id DESC
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, debut.toString());
            stmt.setString(2, fin.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    depenses.add(mapResultSetToDepense(rs));
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur getDepenseByMonth: " + e.getMessage());
            e.printStackTrace();
        }
        return depenses;
    }
    
    
    
    public List<Depense> findAll() {
        List<Depense> depenses = new ArrayList<>();
        String sql = """
            SELECT d.*, c.compte_id, c.nom_compte, c.type_compte, c.solde
            FROM depenses d
            INNER JOIN comptes c ON d.compte_id = c.compte_id
            ORDER BY d.date_depense DESC, d.depense_id DESC
            """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                depenses.add(mapResultSetToDepense(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findAll Depense: " + e.getMessage());
            e.printStackTrace();
        }
        return depenses;
    }
    
    public List<Depense> findByCompte(Long compteId) {
        List<Depense> depenses = new ArrayList<>();
        String sql = """
            SELECT d.*, c.compte_id, c.nom_compte, c.type_compte, c.solde
            FROM depenses d
            INNER JOIN comptes c ON d.compte_id = c.compte_id
            WHERE d.compte_id = ?
            ORDER BY d.date_depense DESC
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                depenses.add(mapResultSetToDepense(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findByCompte: " + e.getMessage());
            e.printStackTrace();
        }
        return depenses;
    }
    
    public BigDecimal getTotalDepensesByCompte(Long compteId) {
        String sql = "SELECT SUM(montant) as total FROM depenses WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, compteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getTotalDepensesByCompte: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    public Depense save(Depense depense) {
        if (depense.getDepenseId() == null) {
            return insert(depense);
        } else {
            return update(depense);
        }
    }
    
    private Depense insert(Depense depense) {
        String sql = """
            INSERT INTO depenses 
            (montant, libelle, date_depense, compte_id, cree_par, date_creation) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setBigDecimal(1, depense.getMontant());
            stmt.setString(2, depense.getLibelle());
            stmt.setString(3, depense.getDateDepense().toString());
            stmt.setLong(4, depense.getCompte().getCompteId());
            stmt.setLong(5, depense.getCreePar());
            stmt.setString(6, LocalDateTime.now().toString());
            
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                depense.setDepenseId(generatedKeys.getLong(1));
                
                // Mettre à jour le solde du compte et créer une transaction
                CompteDAO compteDAO = new CompteDAO();
                compteDAO.debiter(depense.getCompte().getCompteId(), depense.getMontant(), depense.getCreePar());
                
                // Créer une transaction automatique
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                	TypeTransaction.DEBIT, depense.getMontant(), depense.getCompte().getCompteId(),
                    "Dépense: " + depense.getLibelle(), depense.getCreePar()
                );
            }
            
            System.out.println("✅ Dépense enregistrée: " + depense.getLibelle() + " - " + depense.getMontant() + " F CFA");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert depense: " + e.getMessage());
            e.printStackTrace();
        }
        return depense;
    }
    
    private Depense update(Depense depense) {
        // Récupérer l'ancienne dépense pour ajuster le solde
        Optional<Depense> ancienneDepense = findById(depense.getDepenseId());
        
        String sql = """
            UPDATE depenses 
            SET montant = ?, libelle = ?, date_depense = ?, compte_id = ?,
                modifie_par = ?, date_modification = ?
            WHERE depense_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, depense.getMontant());
            stmt.setString(2, depense.getLibelle());
            stmt.setString(3, depense.getDateDepense().toString());
            stmt.setLong(4, depense.getCompte().getCompteId());
            stmt.setLong(5, depense.getModifiePar());
            stmt.setString(6, LocalDateTime.now().toString());
            stmt.setLong(7, depense.getDepenseId());
            
            stmt.executeUpdate();
            
            // Ajuster le solde du compte si le montant a changé
            if (ancienneDepense.isPresent()) {
                BigDecimal ancienMontant = ancienneDepense.get().getMontant();
                BigDecimal nouveauMontant = depense.getMontant();
                
                if (!ancienMontant.equals(nouveauMontant)) {
                    CompteDAO compteDAO = new CompteDAO();
                    // Annuler l'ancien débit et appliquer le nouveau
                    compteDAO.crediter(depense.getCompte().getCompteId(), ancienMontant, depense.getModifiePar());
                    compteDAO.debiter(depense.getCompte().getCompteId(), nouveauMontant, depense.getModifiePar());
                }
            }
            
            System.out.println("✅ Dépense mise à jour: " + depense.getLibelle());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur update depense: " + e.getMessage());
            e.printStackTrace();
        }
        return depense;
    }
    
    
    public void delete(Long id, Long utilisateurId) {

        Optional<Depense> depense = findById(id);
        
        String sql = "DELETE FROM depenses WHERE depense_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
            // Recréditer le compte (annuler le débit initial)
            if (depense.isPresent()) {
                CompteDAO compteDAO = new CompteDAO();
                compteDAO.crediter(depense.get().getCompte().getCompteId(), 
                    depense.get().getMontant(), utilisateurId);
                
                // Créer une transaction pour la suppression
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                		TypeTransaction.CREDIT, depense.get().getMontant(), 
                    depense.get().getCompte().getCompteId(),
                    "Suppression dépense: " + depense.get().getLibelle(), 
                    utilisateurId
                );
            }
            
            System.out.println("✅ Dépense supprimée: " + id + " par utilisateur: " + utilisateurId);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete dépense: " + e.getMessage());
            e.printStackTrace();
        }
    }

 // Surcharge pour compatibilité
    public void delete(Long id) {
        delete(id, 1L); // 1 = admin par défaut
    }
    
    private Depense mapResultSetToDepense(ResultSet rs) throws SQLException {
        Depense depense = new Depense();
        depense.setDepenseId(rs.getLong("depense_id"));
        depense.setMontant(rs.getBigDecimal("montant"));
        depense.setLibelle(rs.getString("libelle"));
        
        // Date de dépense
        String dateDepense = rs.getString("date_depense");
        if (dateDepense != null) {
            depense.setDateDepense(LocalDate.parse(dateDepense));
        }
        
        // Mapping Compte
        Compte compte = new Compte();
        compte.setCompteId(rs.getLong("compte_id"));
        compte.setNomCompte(rs.getString("nom_compte"));
        compte.setTypeCompte(rs.getString("type_compte"));
        compte.setSolde(rs.getBigDecimal("solde"));
        depense.setCompte(compte);
        
        // Audit
        depense.setCreePar(rs.getLong("cree_par"));
        
        String dateCreation = rs.getString("date_creation");
        if (dateCreation != null) {
            depense.setDateCreation(LocalDateTime.parse(dateCreation));
        }
        
        depense.setModifiePar(rs.getLong("modifie_par"));
        if (rs.wasNull()) {
            depense.setModifiePar(null);
        }
        
        String dateModification = rs.getString("date_modification");
        if (dateModification != null) {
            depense.setDateModification(LocalDateTime.parse(dateModification));
        }
        
        return depense;
    }
    
    public List<Depense> getDepenseByMonth(LocalDate date){
    	
    	return List.of();
    }
}