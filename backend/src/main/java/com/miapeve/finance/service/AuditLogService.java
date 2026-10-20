package com.miapeve.finance.service;

import com.miapeve.finance.dto.AuditLogDTO;
import com.miapeve.finance.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> findRecent() {
        return auditLogRepository.findTop300ByOrderByDateCreationDesc().stream()
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getUtilisateurId(),
                        log.getNomUtilisateur(),
                        log.getModule(),
                        log.getMethode(),
                        log.getChemin(),
                        log.getStatut(),
                        log.getDateCreation()
                ))
                .toList();
    }
}
