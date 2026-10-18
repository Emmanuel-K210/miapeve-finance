package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UtilisateurRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank String nomUtilisateur,
        String motDePasse,
        @NotNull Long roleId,
        boolean actif
) {
}
