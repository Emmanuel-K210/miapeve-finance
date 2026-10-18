package com.miapeve.finance.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Compte {
    private Long compteId;
    private String nomCompte;
    private String typeCompte;
    private BigDecimal solde;
    private Long creePar; 
    private LocalDateTime dateCreation;
    private Long modifiePar; 
    private LocalDateTime dateModification; 
    
    public Compte() {
        this.solde = BigDecimal.ZERO;
        this.dateCreation = LocalDateTime.now();
    }
    
    public Compte(String nomCompte, String typeCompte, BigDecimal solde, String description) {
        this.nomCompte = nomCompte;
        this.typeCompte = typeCompte;
        this.solde = solde != null ? solde : BigDecimal.ZERO;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getCompteId() { return compteId; }
    public void setCompteId(Long compteId) { this.compteId = compteId; }
    
    public String getNomCompte() { return nomCompte; } 
    public void setNomCompte(String nomCompte) { this.nomCompte = nomCompte; }
    
    public String getTypeCompte() { return typeCompte; }
    public void setTypeCompte(String typeCompte) { this.typeCompte = typeCompte; }
    
    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }
    
    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public Long getModifiePar() { return modifiePar; } 
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    // Méthodes utilitaires DDD
    public void crediter(BigDecimal montant) {
        this.solde = this.solde.add(montant);
    }
    
    public void debiter(BigDecimal montant) {
        this.solde = this.solde.subtract(montant);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compte compte = (Compte) o;
        return Objects.equals(compteId, compte.compteId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(compteId);
    }
    
    @Override
    public String toString() {
        return String.format("nom=%s , solde=%.0f F CFA",
                           nomCompte, solde.doubleValue());
    }
}