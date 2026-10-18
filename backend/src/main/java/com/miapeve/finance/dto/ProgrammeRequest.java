package com.miapeve.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProgrammeRequest(
        @NotBlank String typeProgramme,
        @NotNull LocalDate dateProgramme,
        @NotNull LocalTime heureDebut,
        @NotNull LocalTime heureFin
) {
}
