package com.miapeve.finance.dto;

import java.time.LocalDateTime;

public record TransactionDTO(
        Long id,
        String typeTransaction,
        double montant,
        Long compteId,
        String nomCompte,
        String description,
        String reference,
        LocalDateTime dateCreation
) {
}
