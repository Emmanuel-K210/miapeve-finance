package com.miapeve.finance.repositories.impl;

import java.util.List;
import java.util.Optional;
import com.miapeve.finance.dao.RoleDAO;
import com.miapeve.finance.entites.Role;
import com.miapeve.finance.repositories.RoleRepository;

public class RoleRepositoryImpl implements RoleRepository {

    private final RoleDAO roleDAO;
    
    public RoleRepositoryImpl() {
        this.roleDAO = new RoleDAO();
    }
    
    @Override
    public Role enregistrer(Role role) {
        return roleDAO.save(role);
    }
    
    @Override
    public Optional<Role> findById(Long id) {
        return roleDAO.findById(id);
    }
    
    @Override
    public Optional<Role> findByLibelle(String libelle) {
        // Implémentation à ajouter dans RoleDAO si nécessaire
        return roleDAO.findAll().stream()
                .filter(role -> role.getLibelle().equalsIgnoreCase(libelle))
                .findFirst();
    }
    
    @Override
    public List<Role> findAll() {
        return roleDAO.findAll();
    }
    
    @Override
    public void supprimer(Long id) {
        roleDAO.delete(id);
    }
    
    @Override
    public boolean existeParLibelle(String libelle) {
        return findByLibelle(libelle).isPresent();
    }
}