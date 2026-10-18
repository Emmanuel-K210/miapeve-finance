package com.miapeve.finance.repositories.impl;

import java.util.List;
import java.util.Optional;

import com.miapeve.finance.dao.UtilisateurDAO;
import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.repositories.UtilisateurRepository;

public class UtilisateurRepositoryImpl implements UtilisateurRepository{

	private final UtilisateurDAO utilisateurDAO;
	
	public UtilisateurRepositoryImpl() {
		this.utilisateurDAO = new UtilisateurDAO();
	}
	
	@Override
	public Utilisateur enregistrer(Utilisateur utilisateur) {
		return utilisateurDAO.save(utilisateur);
	}
	
	@Override
	public Optional<Utilisateur> findById(Long id){
		return utilisateurDAO.findById(id);
	}
	
	@Override
	public Optional<Utilisateur> findByUsername(String username) {
		return utilisateurDAO.findByUsername(username);
	}
	
	@Override
	public List<Utilisateur> findAll(){
		return utilisateurDAO.findAll();
	}
	
}
