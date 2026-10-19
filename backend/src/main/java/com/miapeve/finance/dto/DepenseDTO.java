package com.miapeve.finance.dto;

import java.time.LocalDate;

public record DepenseDTO(
        Long id,
        String libelle,
        double montant,
        LocalDate dateDepense,
        Long compteId,
        String nomCompte
) {
}
