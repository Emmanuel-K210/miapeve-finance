package com.miapeve.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BudgetRequest(
        @NotBlank String libelle,
        @NotNull Long compteId,
        @DecimalMin(value = "1", message = "doit être supérieur à 0") double montantPrevu,
        @Min(1) @Max(12) int mois,
        @Min(2000) int annee
) {
}
