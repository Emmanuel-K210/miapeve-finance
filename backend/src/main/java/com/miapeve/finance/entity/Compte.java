package com.miapeve.finance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comptes")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compte_id")
    private Long id;

    @Column(name = "nom_compte", nullable = false)
    private String nomCompte;

    @Column(name = "type_compte", nullable = false, length = 50)
    private String typeCompte;

    @Column(name = "solde", nullable = false)
    private double solde;

    @Column(name = "cree_par", nullable = false)
    private Long creePar;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "modifie_par")
    private Long modifiePar;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomCompte() { return nomCompte; }
    public void setNomCompte(String nomCompte) { this.nomCompte = nomCompte; }

    public String getTypeCompte() { return typeCompte; }
    public void setTypeCompte(String typeCompte) { this.typeCompte = typeCompte; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public Long getModifiePar() { return modifiePar; }
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}
