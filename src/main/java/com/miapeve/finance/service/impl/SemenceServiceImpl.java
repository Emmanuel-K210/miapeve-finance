package com.miapeve.finance.service.impl;

import java.util.List;
import java.util.Optional;
import com.miapeve.finance.dao.SemenceDAO;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.repositories.SemenceRepository;
import com.miapeve.finance.service.SemenceService;

public class SemenceServiceImpl implements SemenceService {

    private final SemenceDAO semenceDAO;
    
    public SemenceServiceImpl() {
        this.semenceDAO = new SemenceDAO();
    }
    
    @Override
    public Semence enregistrer(Semence semence) {
        return semenceDAO.save(semence);
    }
    
    @Override
    public Optional<Semence> findById(Long id) {
        return semenceDAO.findById(id);
    }
    
    @Override
    public List<Semence> findAll() {
        return semenceDAO.findAll();
    }
    
    @Override
    public List<Semence> findByProgramme(Long programmeId) {
        return semenceDAO.findByProgramme(programmeId);
    }
    
    @Override
    public List<Semence> findByCompte(Long compteId) {
        return semenceDAO.findByCompte(compteId);
    }
    
    @Override
    public void supprimer(Long id) {
        semenceDAO.delete(id);
    }
    
    @Override
    public Double getTotalSemencesByCompte(Long compteId) {
        return semenceDAO.getTotalSemencesByCompte(compteId);
    }
}