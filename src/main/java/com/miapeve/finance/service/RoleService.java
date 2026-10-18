package com.miapeve.finance.service;

import com.miapeve.finance.entites.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role enregistrer(Role role);
    Optional<Role> findById(Long id);
    Optional<Role> findByLibelle(String libelle);
    List<Role> findAll();
    void supprimer(Long id);
    boolean existeParLibelle(String libelle);
}