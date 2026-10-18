package com.miapeve.finance.service;

import com.miapeve.finance.dto.PermissionModuleDTO;
import com.miapeve.finance.dto.RoleDTO;
import com.miapeve.finance.dto.RoleRequest;
import com.miapeve.finance.entity.Role;
import com.miapeve.finance.permission.PermissionCatalog;
import com.miapeve.finance.repository.RoleRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RoleService(RoleRepository roleRepository, UtilisateurRepository utilisateurRepository) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<PermissionModuleDTO> getCatalog() {
        return PermissionCatalog.MODULES.stream()
                .map(m -> new PermissionModuleDTO(m.key(), m.libelle(), PermissionCatalog.view(m.key()), PermissionCatalog.manage(m.key())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public RoleDTO create(RoleRequest request) {
        if (roleRepository.findByLibelle(request.libelle().trim().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Un rôle avec ce nom existe déjà");
        }

        Role role = new Role();
        role.setLibelle(request.libelle().trim().toUpperCase());
        role.setDescription(request.description());
        role.setPermissions(sanitizePermissions(request.permissions()));

        return toDTO(roleRepository.save(role));
    }

    @Transactional
    public RoleDTO update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rôle introuvable: " + id));

        role.setLibelle(request.libelle().trim().toUpperCase());
        role.setDescription(request.description());
        role.setPermissions(sanitizePermissions(request.permissions()));

        return toDTO(roleRepository.save(role));
    }

    @Transactional
    public void delete(Long id) {
        if (!utilisateurRepository.findAll().stream().filter(u -> u.getRole().getId().equals(id)).toList().isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer : des utilisateurs ont encore ce rôle");
        }
        roleRepository.deleteById(id);
    }

    private Set<String> sanitizePermissions(Set<String> requested) {
        if (requested == null) {
            return new HashSet<>();
        }
        Set<String> all = PermissionCatalog.allKeys();
        Set<String> cleaned = new HashSet<>();
        for (String key : requested) {
            if (all.contains(key)) {
                cleaned.add(key);
            }
        }
        return cleaned;
    }

    private RoleDTO toDTO(Role role) {
        long nombreUtilisateurs = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole().getId().equals(role.getId()))
                .count();
        return new RoleDTO(role.getId(), role.getLibelle(), role.getDescription(), role.getPermissions(), nombreUtilisateurs);
    }
}
