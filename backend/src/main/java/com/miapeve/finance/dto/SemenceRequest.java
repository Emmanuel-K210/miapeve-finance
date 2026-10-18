package com.miapeve.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SemenceRequest(
        @NotBlank String typeSemence,
        @DecimalMin(value = "0.01", message = "doit être supérieur à 0") double montant,
        @NotNull LocalDate dateSemence,
        String description,
        @NotNull Long programmeId,
        @NotNull Long compteId
) {
}
