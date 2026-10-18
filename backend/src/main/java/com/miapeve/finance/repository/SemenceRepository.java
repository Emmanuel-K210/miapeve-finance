package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Semence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemenceRepository extends JpaRepository<Semence, Long> {
    List<Semence> findByCompteId(Long compteId);
    List<Semence> findByProgrammeId(Long programmeId);
}
