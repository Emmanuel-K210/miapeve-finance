package com.miapeve.finance.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.repositories.UtilisateurRepository;
import com.miapeve.finance.repositories.impl.UtilisateurRepositoryImpl;
import com.miapeve.finance.service.UtilisateurService;
import com.miapeve.finance.utils.PasswordHasher;

public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    
    public UtilisateurServiceImpl() {
        this.utilisateurRepository = new UtilisateurRepositoryImpl();
    }
    
    @Override
    public Utilisateur enregistrer(Utilisateur utilisateur) {
        try {
            System.out.println("💾 Sauvegarde utilisateur: " + utilisateur.getNomUtilisateur());
            return utilisateurRepository.enregistrer(utilisateur);
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde utilisateur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur", e);
        }
    }
    
    @Override
    public Utilisateur enregistrer(Utilisateur utilisateur, String motDePasse) {
        try {
            System.out.println("💾 Sauvegarde utilisateur avec mot de passe: " + utilisateur.getNomUtilisateur());
            
            // Hacher le mot de passe
            String motDePasseHash = PasswordHasher.hashPassword(motDePasse);
            utilisateur.setDateCreation(LocalDateTime.now());
            utilisateur.setMotDePasse(motDePasseHash);
            
            return utilisateurRepository.enregistrer(utilisateur);
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde utilisateur avec mot de passe: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur", e);
        }
    }
    
    @Override
    public Utilisateur create(Utilisateur utilisateur, String motDePasse) {
        return enregistrer(utilisateur, motDePasse);
    }
    
    @Override
    public Utilisateur update(Utilisateur utilisateur) {
        try {
            System.out.println("✏️ Mise à jour utilisateur ID: " + utilisateur.getId());
            
            // Récupérer l'utilisateur existant pour préserver le mot de passe
            Optional<Utilisateur> existingUserOpt = utilisateurRepository.findById(utilisateur.getId());
            if (existingUserOpt.isPresent()) {
                Utilisateur existingUser = existingUserOpt.get();
                utilisateur.setDateModification(LocalDateTime.now());
                // Conserver le mot de passe existant
                utilisateur.setMotDePasse(existingUser.getMotDePasse());
                
                return utilisateurRepository.enregistrer(utilisateur);
            } else {
                throw new RuntimeException("Utilisateur non trouvé avec ID: " + utilisateur.getId());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour utilisateur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }
    
    @Override
    public Optional<Utilisateur> findById(Long id) {
        try {
            return utilisateurRepository.findById(id);
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche utilisateur par ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<Utilisateur> findByUsername(String username) {
        try {
            return utilisateurRepository.findByUsername(username);
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche utilisateur par username: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<Utilisateur> findAll() {
        try {
            return utilisateurRepository.findAll();
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche tous les utilisateurs: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
        }
    }

    @Override
    public boolean verifierMotDePasse(String nomUtilisateur, String motDePasse) {
        try {
            System.out.println("🔐 Vérification mot de passe pour: " + nomUtilisateur);
            
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByUsername(nomUtilisateur);
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                boolean passwordMatch = PasswordHasher.verifyPassword(motDePasse, utilisateur.getMotDePasse());
                
                System.out.println("✅ Vérification mot de passe: " + (passwordMatch ? "SUCCÈS" : "ÉCHEC"));
                return passwordMatch;
            }
            
            System.out.println("❌ Utilisateur non trouvé: " + nomUtilisateur);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification mot de passe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changerMotDePasse(Long id, String nouveauMotDePasse) {
        try {
            System.out.println("🔑 Changement mot de passe utilisateur ID: " + id);
            
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                
                // Hacher le nouveau mot de passe
                String nouveauMotDePasseHash = PasswordHasher.hashPassword(nouveauMotDePasse);
                utilisateur.setMotDePasse(nouveauMotDePasseHash);
                
                // Sauvegarder
                utilisateurRepository.enregistrer(utilisateur);
                
                System.out.println("✅ Mot de passe changé avec succès pour: " + utilisateur.getNomUtilisateur());
                return true;
            }
            
            System.out.println("❌ Utilisateur non trouvé ID: " + id);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur changement mot de passe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        try {
            System.out.println("🔄 Réinitialisation mot de passe utilisateur ID: " + id);
            return changerMotDePasse(id, nouveauMotDePasse);
        } catch (Exception e) {
            System.err.println("❌ Erreur réinitialisation mot de passe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            System.out.println("🗑️ Suppression utilisateur ID: " + id);
            
            // Vérifier si l'utilisateur existe
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                
                // Désactiver l'utilisateur au lieu de le supprimer (soft delete)
                utilisateur.setActif(false);
                utilisateurRepository.enregistrer(utilisateur);
                
                System.out.println("✅ Utilisateur désactivé: " + utilisateur.getNomUtilisateur());
                return true;
            }
            
            System.out.println("❌ Utilisateur non trouvé ID: " + id);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    // Méthode utilitaire pour activer/désactiver un utilisateur
    public boolean toggleActivation(Long id, boolean actif) {
        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                utilisateur.setActif(actif);
                utilisateurRepository.enregistrer(utilisateur);
                
                System.out.println("✅ Utilisateur " + (actif ? "activé" : "désactivé") + ": " + utilisateur.getNomUtilisateur());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur changement statut utilisateur: " + e.getMessage());
            return false;
        }
    }
}