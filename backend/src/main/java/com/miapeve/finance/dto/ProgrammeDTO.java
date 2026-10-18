package com.miapeve.finance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProgrammeDTO(
        Long id,
        String typeProgramme,
        LocalDate dateProgramme,
        LocalTime heureDebut,
        LocalTime heureFin
) {
}
