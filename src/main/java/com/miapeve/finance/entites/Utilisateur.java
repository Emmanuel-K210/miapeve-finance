package com.miapeve.finance.entites;

import java.time.LocalDateTime;

public class Utilisateur {
    private Long id;
    private String nom;
    private String prenom;
    private String nomUtilisateur;
    private String motDePasse;
    private Role role;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
    private Long creePar; // ✅ Audit
    private Long modifiePar; // ✅ Audit

    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }
    
    public Utilisateur(Long id, String nom, String prenom, String nomUtilisateur, String motDePasse, Role role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.nomUtilisateur = nomUtilisateur;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }

    private Utilisateur(UtilisateurBuilder builder) {
        this.id = builder.id;
        this.nom = builder.nom;
        this.prenom = builder.prenom;
        this.nomUtilisateur = builder.nomUtilisateur;
        this.motDePasse = builder.motDePasse;
        this.role = builder.role;
        this.dateCreation = builder.dateCreation != null ? builder.dateCreation : LocalDateTime.now();
        this.actif = builder.actif != null ? builder.actif : true;
        this.creePar = builder.creePar;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Long getCreePar() { return creePar; } // ✅ Audit
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public Long getModifiePar() { return modifiePar; } // ✅ Audit
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }

    // Builder Pattern
    public static UtilisateurBuilder builder() {
        return new UtilisateurBuilder();
    }

    public static class UtilisateurBuilder {
        private Long id;
        private String nom;
        private String prenom;
        private String nomUtilisateur;
        private String motDePasse;
        private Role role;
        private LocalDateTime dateCreation;
        private Boolean actif;
        private Long creePar;

        public UtilisateurBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UtilisateurBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public UtilisateurBuilder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public UtilisateurBuilder nomUtilisateur(String nomUtilisateur) {
            this.nomUtilisateur = nomUtilisateur;
            return this;
        }

        public UtilisateurBuilder motDePasse(String motDePasse) {
            this.motDePasse = motDePasse;
            return this;
        }

        public UtilisateurBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UtilisateurBuilder dateCreation(LocalDateTime dateCreation) {
            this.dateCreation = dateCreation;
            return this;
        }

        public UtilisateurBuilder actif(Boolean actif) {
            this.actif = actif;
            return this;
        }

        public UtilisateurBuilder creePar(Long creePar) {
            this.creePar = creePar;
            return this;
        }

        public Utilisateur build() {
            return new Utilisateur(this);
        }
    }
}