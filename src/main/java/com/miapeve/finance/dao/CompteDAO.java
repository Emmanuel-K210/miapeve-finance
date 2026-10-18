package com.miapeve.finance.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.enums.TypeTransaction;

public class CompteDAO extends BaseDAO {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public Optional<Compte> findById(Long id) {
        String sql = "SELECT * FROM comptes WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCompte(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findById: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Compte> findAll() {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM comptes ORDER BY type_compte, nom_compte";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                comptes.add(mapResultSetToCompte(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return comptes;
    }
    
    public boolean transfert(Long compteCrediterId,Long compteDebiterId,BigDecimal montant,Long utilisateurId) {
   
    	return crediter(compteCrediterId, montant, utilisateurId) && debiter(compteDebiterId, montant, utilisateurId);
    }
    
    public Optional<Compte> findByType(String typeCompte) {
        String sql = "SELECT * FROM comptes WHERE type_compte = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, typeCompte);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCompte(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findByType: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public Compte save(Compte compte) { 
        if (compte.getCompteId() == null) {
            return insert(compte);
        } else {
            return update(compte);
        }
    }
    
    private Compte insert(Compte compte) {
        String sql = """
            INSERT INTO comptes 
            (nom_compte, type_compte, solde, cree_par, date_creation) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, compte.getNomCompte());
            stmt.setString(2, compte.getTypeCompte());
            stmt.setBigDecimal(3, compte.getSolde()); 
            stmt.setLong(4, compte.getCreePar());
            stmt.setString(5, LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                compte.setCompteId(generatedKeys.getLong(1));
            }
            
            System.out.println("✅ Compte créé: " + compte.getNomCompte());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert compte: " + e.getMessage());
            e.printStackTrace();
        }
        return compte;
    }
    
    private Compte update(Compte compte) {
        String sql = """
            UPDATE comptes 
            SET nom_compte = ?, type_compte = ?, solde = ?, 
                modifie_par = ?, date_modification = ?
            WHERE compte_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, compte.getNomCompte());
            stmt.setString(2, compte.getTypeCompte());
            stmt.setBigDecimal(3, compte.getSolde()); 
            stmt.setLong(4, compte.getModifiePar());
            stmt.setString(5, LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            stmt.setLong(6, compte.getCompteId());
            
            stmt.executeUpdate();
            
            System.out.println("✅ Compte mis à jour: " + compte.getNomCompte());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur update compte: " + e.getMessage());
            e.printStackTrace();
        }
        return compte;
    }
    
    public boolean updateSolde(Long compteId, BigDecimal nouveauSolde) {
        String sql = "UPDATE comptes SET solde = ? WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nouveauSolde); 
            stmt.setLong(2, compteId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur updateSolde: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean crediter(Long compteId, BigDecimal montant, Long utilisateurId) {
        String sql = "UPDATE comptes SET solde = solde + ? WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, montant);
            stmt.setLong(2, compteId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Créer automatiquement une transaction
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                    TypeTransaction.CREDIT, montant, compteId, 
                    "Crédit manuel du compte", utilisateurId
                );
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur crediter: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean debiter(Long compteId, BigDecimal montant, Long utilisateurId) {
        String sql = "UPDATE comptes SET solde = solde - ? WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, montant); 
            stmt.setLong(2, compteId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Créer automatiquement une transaction
                TransactionDAO transactionDAO = new TransactionDAO();
                transactionDAO.creerTransactionAutomatique(
                    TypeTransaction.DEBIT, montant, compteId, 
                    "Débit manuel du compte", utilisateurId
                );
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur debiter: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM comptes WHERE compte_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
            System.out.println("✅ Compte supprimé: " + id);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Compte mapResultSetToCompte(ResultSet rs) throws SQLException {
        Compte compte = new Compte();
        compte.setCompteId(rs.getLong("compte_id"));
        compte.setNomCompte(rs.getString("nom_compte"));
        compte.setTypeCompte(rs.getString("type_compte"));
        
        BigDecimal solde = rs.getBigDecimal("solde");
        if (rs.wasNull()) {
            solde = BigDecimal.ZERO;
        }
        compte.setSolde(solde);
        
        compte.setCreePar(rs.getLong("cree_par"));
        
        compte.setModifiePar(rs.getLong("modifie_par"));
        if (rs.wasNull()) {
            compte.setModifiePar(null);
        }
        
        // Gestion sécurisée des dates
        try {
            String dateCreationStr = rs.getString("date_creation");
            if (dateCreationStr != null && !dateCreationStr.trim().isEmpty()) {
                compte.setDateCreation(LocalDateTime.parse(dateCreationStr, TIMESTAMP_FORMATTER));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing date_creation: " + e.getMessage());
            compte.setDateCreation(LocalDateTime.now());
        }
        
        try {
            String dateModificationStr = rs.getString("date_modification");
            if (dateModificationStr != null && !dateModificationStr.trim().isEmpty()) {
                compte.setDateModification(LocalDateTime.parse(dateModificationStr, TIMESTAMP_FORMATTER));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing date_modification: " + e.getMessage());
            // Laisser null si erreur
        }
        return compte;
    }
    
    public List<Compte> findComptesMouvementes() throws SQLException {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM comptes WHERE solde > 0 ORDER BY solde DESC";
     
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
     
            while (rs.next()) {
                Compte c = mapResultSetToCompte(rs);
                comptes.add(c);
            }
        }
        return comptes;
    }

}