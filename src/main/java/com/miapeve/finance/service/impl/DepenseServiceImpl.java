package com.miapeve.finance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.miapeve.finance.dao.DepenseDAO;
import com.miapeve.finance.entites.Depense;
import com.miapeve.finance.repositories.DepenseRepository;

public class DepenseServiceImpl implements DepenseRepository {

    private final DepenseDAO depenseDAO;
    
    public DepenseServiceImpl() {
        this.depenseDAO = new DepenseDAO();
    }
    
    @Override
    public Depense enregistrer(Depense depense) {
        return depenseDAO.save(depense);
    }
    
    @Override
    public Optional<Depense> findById(Long id) {
        return depenseDAO.findById(id);
    }
    
    @Override
    public List<Depense> findAll() {
        return depenseDAO.findAll();
    }
    
    @Override
    public List<Depense> findByCompte(Long compteId) {
        return depenseDAO.findByCompte(compteId);
    }
    

    
    @Override
    public void supprimer(Long id) {
        depenseDAO.delete(id);
    }
    
    @Override
    public Double getTotalDepensesByCompte(Long compteId) {
        return depenseDAO.getTotalDepensesByCompte(compteId).doubleValue();
    }
    
    @Override
    public Double getTotalDepenses() {
        return depenseDAO.findAll().stream()
                .mapToDouble(depense->depense.getMontant().doubleValue())
                .sum();
    }

	@Override
	public List<Depense> getDepenseByMonth(LocalDate deb, LocalDate fin) {
		
		return depenseDAO.getDepenseByMonth(deb, fin);
	}
}