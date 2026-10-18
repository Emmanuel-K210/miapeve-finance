package com.miapeve.finance.repositories.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.miapeve.finance.dao.SemenceDAO;
import com.miapeve.finance.entites.Semence;
import com.miapeve.finance.repositories.SemenceRepository;

public class SemenceRepositoryImpl implements SemenceRepository {

    private final SemenceDAO semenceDAO;
    
    public SemenceRepositoryImpl() {
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

	@Override
	public double calculerTotalSemencesMoisEnCours() {
		
		return semenceDAO.calculerTotalSemencesMoisEnCours();
	}

	@Override
	public int compterSemencesMoisEnCours() {
		return semenceDAO.compterSemencesMoisEnCours();
	}

	@Override
	public List<Semence> findByPeriode(LocalDate deb, LocalDate fin) {
		
		return semenceDAO.findByPeriode(deb.atStartOfDay(), fin.atStartOfDay());
	}
}