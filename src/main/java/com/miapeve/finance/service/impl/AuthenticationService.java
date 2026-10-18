package com.miapeve.finance.service.impl;

import java.util.Optional;

import com.miapeve.finance.entites.Utilisateur;
import com.miapeve.finance.repositories.UtilisateurRepository;
import com.miapeve.finance.repositories.impl.UtilisateurRepositoryImpl;
import com.miapeve.finance.utils.PasswordHasher;

public class AuthenticationService {
    
    private UtilisateurRepository utilisateurRepository;
    private Utilisateur utilisateurCourant;
    
    public AuthenticationService() {
        this.utilisateurRepository = new UtilisateurRepositoryImpl();
    }
    
    public boolean authentifier(String nomUtilisateur, String motDePasse) {
        System.out.println("=== AUTHENTIFICATION ===");
        System.out.println("👤 Utilisateur: " + nomUtilisateur);
        
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByUsername(nomUtilisateur);
        
        if (!utilisateurOpt.isPresent()) {
            System.out.println("❌ Utilisateur non trouvé");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        String storedHash = utilisateur.getMotDePasse();
        
        System.out.println("🔍 Hash en base: " + storedHash);
        System.out.println("🔍 Longueur: " + storedHash.length());
        
        // Utiliser PasswordHasher pour vérifier
        boolean authentifie = PasswordHasher.verifyPassword(motDePasse, storedHash);
        
        if (authentifie) {
            this.utilisateurCourant = utilisateur;
            System.out.println("✅ Authentification réussie");
            return true;
        } else {
            // Essayer aussi la méthode simple (au cas où certains mots de passe n'ont pas de salt)
            boolean authentifieSimple = PasswordHasher.verifySimple(motDePasse, storedHash);
            if (authentifieSimple) {
                this.utilisateurCourant = utilisateur;
                System.out.println("✅ Authentification réussie (méthode simple)");
                return true;
            }
            
            System.out.println("❌ Échec d'authentification");
            return false;
        }
    }
    
    public void deconnecter() {
        this.utilisateurCourant = null;
    }
    
    public Utilisateur getUtilisateurCourant() {
        return utilisateurCourant;
    }
    
    public boolean estConnecte() {
        return utilisateurCourant != null;
    }
    
    public boolean estAdmin() {
        return estConnecte() && "ADMIN".equals(utilisateurCourant.getRole().getLibelle());
    }
    
    public boolean estPasteur() {
        return estConnecte() && "PASTEUR".equals(utilisateurCourant.getRole().getLibelle());
    }
    
    public boolean estComptable() {
        return estConnecte() && "COMPTABLE".equals(utilisateurCourant.getRole().getLibelle());
    }
    
    public boolean aAccesAdministration() {
        return estAdmin() || estPasteur();
    }
    
    private String hashMotDePasse(String motDePasse) {
        // Utiliser PasswordHasher pour générer un nouveau hash
        return PasswordHasher.hashPassword(motDePasse);
    }
    
    // Plus besoin de cette méthode car l'initialisation se fait dans DatabaseManager
    public void initialiserUtilisateursParDefaut() {
        System.out.println("ℹ️  Les utilisateurs par défaut sont gérés par DatabaseManager");
    }
}