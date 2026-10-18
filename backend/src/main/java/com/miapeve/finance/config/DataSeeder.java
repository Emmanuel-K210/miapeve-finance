package com.miapeve.finance.config;

import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.entity.Role;
import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.RoleRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import com.miapeve.finance.security.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Reproduit l'amorçage de données de l'ancienne application desktop
 * (rôles par défaut, compte admin, caisse principale) pour qu'une base
 * toute neuve soit immédiatement utilisable.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String[][] ROLES = {
            {"ADMIN", "Administrateur système avec tous les droits"},
            {"PASTEUR", "Pasteur - Accès complet comme l'admin"},
            {"COMPTABLE", "Comptable - Gestion financière uniquement"},
            {"MEMBRE", "Membre standard - Consultation uniquement"}
    };

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CompteRepository compteRepository;

    public DataSeeder(RoleRepository roleRepository, UtilisateurRepository utilisateurRepository, CompteRepository compteRepository) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.compteRepository = compteRepository;
    }

    @Override
    public void run(String... args) {
        for (String[] roleDef : ROLES) {
            roleRepository.findByLibelle(roleDef[0]).orElseGet(() -> {
                Role role = new Role();
                role.setLibelle(roleDef[0]);
                role.setDescription(roleDef[1]);
                return roleRepository.save(role);
            });
        }

        Utilisateur admin = utilisateurRepository.findByNomUtilisateur("admin").orElseGet(() -> {
            Role adminRole = roleRepository.findByLibelle("ADMIN").orElseThrow();
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom("Administrateur");
            utilisateur.setPrenom("System");
            utilisateur.setNomUtilisateur("admin");
            utilisateur.setMotDePasse(PasswordHasher.hashPassword("admin123"));
            utilisateur.setRole(adminRole);
            utilisateur.setActif(true);
            return utilisateurRepository.save(utilisateur);
        });

        if (compteRepository.count() == 0) {
            Compte caisse = new Compte();
            caisse.setNomCompte("Caisse Principale");
            caisse.setTypeCompte("CAISSE");
            caisse.setSolde(0.0);
            caisse.setCreePar(admin.getId());
            compteRepository.save(caisse);
        }
    }
}
