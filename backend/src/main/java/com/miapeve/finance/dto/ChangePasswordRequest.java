package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String motDePasseActuel,
        @NotBlank @Size(min = 4, message = "doit contenir au moins 4 caractères") String nouveauMotDePasse
) {
}
