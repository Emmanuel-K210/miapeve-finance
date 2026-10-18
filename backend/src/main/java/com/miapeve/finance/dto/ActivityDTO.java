package com.miapeve.finance.dto;

import java.time.LocalDateTime;

public record ActivityDTO(
        String type,
        String titre,
        String sousTitre,
        double montant,
        boolean positif,
        LocalDateTime date
) {
}
