package com.miapeve.finance.controller;

import com.miapeve.finance.dto.AuditLogDTO;
import com.miapeve.finance.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/entries")
    public List<AuditLogDTO> findRecent() {
        return auditLogService.findRecent();
    }
}
