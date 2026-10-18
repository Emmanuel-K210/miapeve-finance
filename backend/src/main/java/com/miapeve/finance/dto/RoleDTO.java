package com.miapeve.finance.dto;

import java.util.Set;

public record RoleDTO(
        Long id,
        String libelle,
        String description,
        Set<String> permissions,
        long nombreUtilisateurs
) {
}
