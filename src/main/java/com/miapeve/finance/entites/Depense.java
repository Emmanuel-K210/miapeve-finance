package com.miapeve.finance.entites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Depense {
    private Long depenseId;
    private BigDecimal montant; // ✅ Changé de Double à BigDecimal
    private String libelle;
    private LocalDate dateDepense; // ✅ Changé de Timestamp à Date
    private Compte compte;
    private Long creePar; // ✅ Audit
    private LocalDateTime dateCreation; // ✅ Audit
    private Long modifiePar; // ✅ Audit
    private LocalDateTime dateModification; // ✅ Audit
    
    // Constructeurs
    public Depense() {
        this.dateCreation = LocalDateTime.now();
        this.dateDepense = LocalDate.now();
    }
    
    public Depense(BigDecimal montant, String libelle, Compte compte, Long creePar) {
        this.montant = montant;
        this.libelle = libelle;
        this.compte = compte;
        this.creePar = creePar;
        this.dateCreation = LocalDateTime.now();
        this.dateDepense = LocalDate.now();
    }
    
    // Getters & Setters
    public Long getDepenseId() { return depenseId; }
    public void setDepenseId(Long depenseId) { this.depenseId = depenseId; }
    
    public BigDecimal getMontant() { return montant; } // ✅ BigDecimal
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    
    public LocalDate getDateDepense() { return dateDepense; } // ✅ LocalDate
    public void setDateDepense(LocalDate dateDepense) { this.dateDepense = dateDepense; }
    
    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }
    
    public Long getCreePar() { return creePar; } // ✅ Audit
    public void setCreePar(Long creePar) { this.creePar = creePar; }
    
    public LocalDateTime getDateCreation() { return dateCreation; } // ✅ Audit
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public Long getModifiePar() { return modifiePar; } // ✅ Audit
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }
    
    public LocalDateTime getDateModification() { return dateModification; } // ✅ Audit
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Depense depense = (Depense) o;
        return Objects.equals(depenseId, depense.depenseId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(depenseId);
    }
    
    @Override
    public String toString() {
        return String.format("Depense{id=%d, libelle='%s', montant=%,.0f F CFA}", 
                           depenseId, libelle, montant.doubleValue());
    }
}