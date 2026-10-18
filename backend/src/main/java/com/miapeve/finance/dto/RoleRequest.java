package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record RoleRequest(
        @NotBlank String libelle,
        String description,
        Set<String> permissions
) {
}
