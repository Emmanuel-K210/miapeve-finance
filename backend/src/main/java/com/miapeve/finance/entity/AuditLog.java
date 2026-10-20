package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long id;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "nom_utilisateur")
    private String nomUtilisateur;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "methode", nullable = false, length = 10)
    private String methode;

    @Column(name = "chemin", nullable = false)
    private String chemin;

    @Column(name = "statut", nullable = false)
    private int statut;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public int getStatut() { return statut; }
    public void setStatut(int statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
}
