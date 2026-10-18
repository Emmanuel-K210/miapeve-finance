package com.miapeve.finance.service;

import com.miapeve.finance.dto.UtilisateurDTO;
import com.miapeve.finance.entity.Utilisateur;

final class UtilisateurMapper {

    private UtilisateurMapper() {
    }

    static UtilisateurDTO toDTO(Utilisateur utilisateur) {
        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getNomUtilisateur(),
                utilisateur.getRole() != null ? utilisateur.getRole().getId() : null,
                utilisateur.getRole() != null ? utilisateur.getRole().getLibelle() : null,
                utilisateur.isActif(),
                utilisateur.getRole() != null ? utilisateur.getRole().getPermissions() : java.util.Set.of()
        );
    }
}
