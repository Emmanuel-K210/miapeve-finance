package com.miapeve.finance.repositories.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.miapeve.finance.dao.CompteDAO;
import com.miapeve.finance.entites.Compte;
import com.miapeve.finance.repositories.CompteRepository;

public class CompteRepositoryImpl implements CompteRepository {

    private final CompteDAO compteDAO;
    
    public CompteRepositoryImpl() {
        this.compteDAO = new CompteDAO();
    }
    
    @Override
    public Compte enregistrer(Compte compte) {
        return compteDAO.save(compte);
    }
    
    @Override
    public Optional<Compte> findById(Long id) {
        return compteDAO.findById(id);
    }
    
    @Override
    public Optional<Compte> findByType(String typeCompte) {
        return compteDAO.findByType(typeCompte);
    }
    
    @Override
    public List<Compte> findAll() {
        return compteDAO.findAll();
    }
    
    @Override
    public void supprimer(Long id) {
        compteDAO.delete(id);
    }
    
    @Override
    public boolean crediter(Long compteId, Double montant,Long userId) {
        return compteDAO.crediter(compteId, new BigDecimal(montant),userId);
    }
    
    @Override
    public boolean debiter(Long compteId, Double montant,Long userId) {
        return compteDAO.debiter(compteId,  new BigDecimal(montant),userId);
    }
    
    @Override
    public boolean updateSolde(Long compteId, Double nouveauSolde) {
        return compteDAO.updateSolde(compteId, new BigDecimal(nouveauSolde));
    }
    
    @Override
    public Double getSoldeTotal() {
        return compteDAO.findAll().stream()
                .mapToDouble(compte->compte.getSolde().doubleValue())
                .sum();
    }

	@Override
	public void delete(Long id) {
		compteDAO.delete(id);
	}

	@Override
	public boolean transfert(Long compteCrediterId, Long compteDebiterId, BigDecimal montant,Long utilisateurId) {
		
		return compteDAO.transfert(compteCrediterId, compteDebiterId, montant, utilisateurId);
	}
}