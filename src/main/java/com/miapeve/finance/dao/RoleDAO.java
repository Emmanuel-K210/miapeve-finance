package com.miapeve.finance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.config.BaseDAO;
import com.miapeve.finance.entites.Role;

public class RoleDAO extends BaseDAO {
    
    public Optional<Role> findById(Long id) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToRole(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY libelle";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
    
    public Role save(Role role) {
        if (role.getId() == null) {
            return insert(role);
        } else {
            return update(role);
        }
    }
    
    private Role insert(Role role) {
        String sql = "INSERT INTO roles (libelle, description) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, role.getLibelle());
            stmt.setString(2, role.getDescription());
            stmt.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                role.setId(generatedKeys.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }
    
    private Role update(Role role) {
        String sql = "UPDATE roles SET libelle = ?, description = ? WHERE role_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.getLibelle());
            stmt.setString(2, role.getDescription());
            stmt.setLong(3, role.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM roles WHERE role_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("role_id"));
        role.setLibelle(rs.getString("libelle"));
        role.setDescription(rs.getString("description"));
        return role;
    }
}