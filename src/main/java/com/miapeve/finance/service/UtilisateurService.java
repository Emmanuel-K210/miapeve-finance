package com.miapeve.finance.service;

import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Utilisateur;

public interface UtilisateurService {
    
    Utilisateur enregistrer(Utilisateur utilisateur);
    
    Utilisateur enregistrer(Utilisateur utilisateur, String motDePasse);
    
    Utilisateur create(Utilisateur utilisateur, String motDePasse);
    
    Utilisateur update(Utilisateur utilisateur);
    
    Optional<Utilisateur> findById(Long id);
    
    Optional<Utilisateur> findByUsername(String username);
    
    List<Utilisateur> findAll();
    
    boolean verifierMotDePasse(String nomUtilisateur, String motDePasse);
    
    boolean changerMotDePasse(Long id, String nouveauMotDePasse);
    
    boolean reinitialiserMotDePasse(Long id, String nouveauMotDePasse);
    
    boolean delete(Long id);
}