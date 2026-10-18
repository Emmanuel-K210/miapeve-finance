package com.miapeve.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DepenseRequest(
        @NotBlank String libelle,
        @DecimalMin(value = "0.01", message = "doit être supérieur à 0") double montant,
        @NotNull LocalDate dateDepense,
        @NotNull Long compteId
) {
}
