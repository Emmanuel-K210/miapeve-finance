package com.miapeve.finance.dto;

public record CompteDTO(
        Long id,
        String nomCompte,
        String typeCompte,
        double solde
) {
}
