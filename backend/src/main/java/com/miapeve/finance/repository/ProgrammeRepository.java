package com.miapeve.finance.repository;

import com.miapeve.finance.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findAllByOrderByDateProgrammeDesc();
}
