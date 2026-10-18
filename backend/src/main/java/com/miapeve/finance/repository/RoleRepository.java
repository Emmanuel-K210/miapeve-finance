package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByLibelle(String libelle);
}
