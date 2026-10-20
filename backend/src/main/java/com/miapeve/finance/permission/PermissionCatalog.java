package com.miapeve.finance.permission;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Catalogue centralisé des modules de l'application et de leurs permissions.
 * Chaque module expose deux niveaux : VIEW (consultation) et MANAGE (créer,
 * modifier, supprimer). Un rôle est simplement un ensemble de ces clés —
 * l'administrateur peut créer n'importe quel rôle et cocher les permissions
 * voulues ; le reste de l'application (menu, boutons, API) s'adapte en
 * fonction de cet ensemble, rien n'est codé en dur par rôle.
 */
public final class PermissionCatalog {

    public record Module(String key, String libelle) {
    }

    public static final List<Module> MODULES = List.of(
            new Module("COMPTES", "Comptes"),
            new Module("TRANSACTIONS", "Transactions"),
            new Module("SEMENCES", "Semences"),
            new Module("DEPENSES", "Dépenses"),
            new Module("PROGRAMMES", "Programmes"),
            new Module("BUDGETS", "Budgets"),
            new Module("UTILISATEURS", "Utilisateurs"),
            new Module("ROLES", "Rôles et permissions"),
            new Module("PARAMETRES", "Paramètres"),
            new Module("AUDIT", "Journal d'activité")
    );

    private PermissionCatalog() {
    }

    public static String view(String module) {
        return module + "_VIEW";
    }

    public static String manage(String module) {
        return module + "_MANAGE";
    }

    public static Set<String> allKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Module module : MODULES) {
            keys.add(view(module.key()));
            keys.add(manage(module.key()));
        }
        return keys;
    }

    public static boolean isKnownModule(String module) {
        return MODULES.stream().anyMatch(m -> m.key().equals(module));
    }
}
