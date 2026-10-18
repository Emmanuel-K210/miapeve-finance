package com.miapeve.finance.dto;

import java.time.LocalDate;

public record SemenceDTO(
        Long id,
        String typeSemence,
        double montant,
        LocalDate dateSemence,
        String description,
        Long programmeId,
        String programmeLibelle,
        Long compteId,
        String nomCompte
) {
}
