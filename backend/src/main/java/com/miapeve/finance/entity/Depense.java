package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "depenses")
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depense_id")
    private Long id;

    @Column(name = "montant", nullable = false)
    private double montant;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "date_depense", nullable = false)
    private LocalDate dateDepense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalDate getDateDepense() { return dateDepense; }
    public void setDateDepense(LocalDate dateDepense) { this.dateDepense = dateDepense; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }
}
