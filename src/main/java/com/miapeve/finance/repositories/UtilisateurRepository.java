package com.miapeve.finance.repositories;

import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Utilisateur;

public interface UtilisateurRepository {

	Utilisateur enregistrer(Utilisateur utilisateur);

	Optional<Utilisateur> findById(Long id);

	Optional<Utilisateur> findByUsername(String username);

	List<Utilisateur> findAll();

}
