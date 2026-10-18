package com.miapeve.finance.dto;

public record PermissionModuleDTO(
        String module,
        String libelle,
        String viewKey,
        String manageKey
) {
}
