package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String nomUtilisateur,
        @NotBlank String motDePasse
) {
}
