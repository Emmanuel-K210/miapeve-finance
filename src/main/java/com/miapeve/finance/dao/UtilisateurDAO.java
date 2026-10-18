package com.miapeve.finance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Role;
import com.miapeve.finance.entites.Utilisateur;

public class UtilisateurDAO extends BaseDAO {
    
    public Optional<Utilisateur> findById(Long id) {
        String sql = """
            SELECT u.*, r.role_id, r.libelle, r.description 
            FROM utilisateurs u 
            INNER JOIN roles r ON u.role_id = r.role_id 
            WHERE u.utilisateur_id = ? AND u.actif = true
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findById: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public Optional<Utilisateur> findByUsername(String username) {
        String sql = """
            SELECT u.*, r.role_id, r.libelle, r.description 
            FROM utilisateurs u 
            INNER JOIN roles r ON u.role_id = r.role_id 
            WHERE u.nom_utilisateur = ? AND u.actif = true
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findByUsername: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = """
            SELECT u.*, r.role_id, r.libelle, r.description 
            FROM utilisateurs u 
            INNER JOIN roles r ON u.role_id = r.role_id 
            WHERE u.actif = true
            ORDER BY u.nom, u.prenom
            """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return utilisateurs;
    }
    
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE nom_utilisateur = ? AND actif = true";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur usernameExists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getId() == null) {
            return insert(utilisateur);
        } else {
            return update(utilisateur);
        }
    }
    
    private Utilisateur insert(Utilisateur utilisateur) {
        String sql = """
            INSERT INTO utilisateurs (nom, prenom, nom_utilisateur, mot_de_passe, role_id, date_creation, actif) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getNomUtilisateur());
            stmt.setString(4, utilisateur.getMotDePasse());
            stmt.setLong(5, utilisateur.getRole().getId());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(7, true);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    utilisateur.setId(generatedKeys.getLong(1));
                    System.out.println("✅ Utilisateur créé avec ID: " + utilisateur.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur insert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return utilisateur;
    }
    
    private Utilisateur update(Utilisateur utilisateur) {
        String sql = """
            UPDATE utilisateurs 
            SET nom = ?, prenom = ?, nom_utilisateur = ?, mot_de_passe = ?, role_id = ?, date_modification = ? 
            WHERE utilisateur_id = ?
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getNomUtilisateur());
            stmt.setString(4, utilisateur.getMotDePasse());
            stmt.setLong(5, utilisateur.getRole().getId());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(7, utilisateur.getId());
            
            stmt.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour: " + utilisateur.getId());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur update utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return utilisateur;
    }
    
    public void delete(Long utilisateurId) {
        String sql = "UPDATE utilisateurs SET actif = false, date_modification = ? WHERE utilisateur_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, utilisateurId);
            
            stmt.executeUpdate();
            System.out.println("✅ Utilisateur désactivé: " + utilisateurId);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("utilisateur_id"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setNomUtilisateur(rs.getString("nom_utilisateur"));
        utilisateur.setMotDePasse(rs.getString("mot_de_passe"));
        
        // Gestion des dates
        Timestamp creationTimestamp = rs.getTimestamp("date_creation");
        if (creationTimestamp != null) {
            // Si vous avez un champ dateCreation dans Utilisateur
            // utilisateur.setDateCreation(creationTimestamp.toLocalDateTime());
        }
        
        Timestamp modifTimestamp = rs.getTimestamp("date_modification");
        if (modifTimestamp != null) {
            // utilisateur.setDateModification(modifTimestamp.toLocalDateTime());
        }
        
        // Rôle
        Role role = new Role();
        role.setId(rs.getLong("role_id"));
        role.setLibelle(rs.getString("libelle"));
        role.setDescription(rs.getString("description"));
        
        utilisateur.setRole(role);
        return utilisateur;
    }
}