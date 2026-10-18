package com.miapeve.finance.dto;

import java.util.Set;

public record UtilisateurDTO(
        Long id,
        String nom,
        String prenom,
        String nomUtilisateur,
        Long roleId,
        String role,
        boolean actif,
        Set<String> permissions
) {
}
