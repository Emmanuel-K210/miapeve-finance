package com.miapeve.finance.dto;

public record BudgetDTO(
        Long id,
        String libelle,
        Long compteId,
        String nomCompte,
        double montantPrevu,
        double montantConsomme,
        int mois,
        int annee
) {
}
