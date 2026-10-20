package com.miapeve.finance.dto;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        Long utilisateurId,
        String nomUtilisateur,
        String module,
        String methode,
        String chemin,
        int statut,
        LocalDateTime dateCreation
) {
}
