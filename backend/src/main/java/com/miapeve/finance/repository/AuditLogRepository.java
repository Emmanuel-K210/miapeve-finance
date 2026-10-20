package com.miapeve.finance.repository;

import com.miapeve.finance.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop300ByOrderByDateCreationDesc();
}
