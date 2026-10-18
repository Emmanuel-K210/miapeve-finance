package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(name = "montant_prevu", nullable = false)
    private double montantPrevu;

    @Column(name = "mois", nullable = false)
    private int mois;

    @Column(name = "annee", nullable = false)
    private int annee;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public double getMontantPrevu() { return montantPrevu; }
    public void setMontantPrevu(double montantPrevu) { this.montantPrevu = montantPrevu; }

    public int getMois() { return mois; }
    public void setMois(int mois) { this.mois = mois; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }
}
