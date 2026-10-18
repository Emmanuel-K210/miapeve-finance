package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;

public record CompteRequest(
        @NotBlank String nomCompte,
        @NotBlank String typeCompte,
        double solde
) {
}
