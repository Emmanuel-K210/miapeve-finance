package com.miapeve.finance.config;

import com.miapeve.finance.entity.Compte;
import com.miapeve.finance.entity.Role;
import com.miapeve.finance.entity.Utilisateur;
import com.miapeve.finance.permission.PermissionCatalog;
import com.miapeve.finance.repository.CompteRepository;
import com.miapeve.finance.repository.RoleRepository;
import com.miapeve.finance.repository.UtilisateurRepository;
import com.miapeve.finance.security.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Reproduit l'amorçage de données de l'ancienne application desktop
 * (rôles par défaut, compte admin, caisse principale) pour qu'une base
 * toute neuve soit immédiatement utilisable, avec des permissions
 * raisonnables par défaut — l'administrateur peut ensuite tout ajuster
 * depuis la page Rôles.
 */
@Component
public class DataSeeder implements CommandLineRunner {

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
        Role adminRole = seedRole("ADMIN", "Administrateur système avec tous les droits", PermissionCatalog.allKeys());
        // Garantie anti-verrouillage : même si le rôle ADMIN existait déjà
        // (mise à jour depuis une version antérieure), il garde toujours
        // l'ensemble complet des permissions.
        if (!adminRole.getPermissions().containsAll(PermissionCatalog.allKeys())) {
            adminRole.setPermissions(PermissionCatalog.allKeys());
            adminRole = roleRepository.save(adminRole);
        }

        seedRole("PASTEUR", "Pasteur - Accès complet comme l'admin", PermissionCatalog.allKeys());
        seedRole("COMPTABLE", "Comptable - Gestion financière uniquement", financePermissions());
        seedRole("MEMBRE", "Membre standard - Consultation uniquement", consultationPermissions());

        Utilisateur admin = utilisateurRepository.findByNomUtilisateur("admin").orElseGet(() -> {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom("Administrateur");
            utilisateur.setPrenom("System");
            utilisateur.setNomUtilisateur("admin");
            utilisateur.setMotDePasse(PasswordHasher.hashPassword("admin123"));
            utilisateur.setActif(true);
            return utilisateur;
        });
        admin.setRole(adminRole);
        admin = utilisateurRepository.save(admin);

        if (compteRepository.count() == 0) {
            Compte caisse = new Compte();
            caisse.setNomCompte("Caisse Principale");
            caisse.setTypeCompte("CAISSE");
            caisse.setSolde(0.0);
            caisse.setCreePar(admin.getId());
            compteRepository.save(caisse);
        }
    }

    private Role seedRole(String libelle, String description, Set<String> permissions) {
        return roleRepository.findByLibelle(libelle).orElseGet(() -> {
            Role role = new Role();
            role.setLibelle(libelle);
            role.setDescription(description);
            role.setPermissions(permissions);
            return roleRepository.save(role);
        });
    }

    private Set<String> financePermissions() {
        Set<String> perms = new HashSet<>();
        for (String module : new String[]{"COMPTES", "TRANSACTIONS", "SEMENCES", "DEPENSES", "BUDGETS"}) {
            perms.add(PermissionCatalog.view(module));
            perms.add(PermissionCatalog.manage(module));
        }
        perms.add(PermissionCatalog.view("PROGRAMMES"));
        return perms;
    }

    private Set<String> consultationPermissions() {
        Set<String> perms = new HashSet<>();
        for (String module : new String[]{"COMPTES", "TRANSACTIONS", "SEMENCES", "DEPENSES", "PROGRAMMES", "BUDGETS"}) {
            perms.add(PermissionCatalog.view(module));
        }
        return perms;
    }
}
