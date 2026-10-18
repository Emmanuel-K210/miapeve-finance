package com.miapeve.finance.entites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.miapeve.finance.enums.TypeSemence;

public class Semence {
    private Long semenceId;
    private TypeSemence typeSemence;
    private BigDecimal montant;
    private LocalDate dateSemence;
    private Programme programme;
    private Compte compte;
    private Long creePar; // ✅ Audit
    private LocalDateTime dateCreation; // ✅ Audit
    private Long modifiePar; // ✅ Audit
    private LocalDateTime dateModification; // ✅ Audit
    
    // Constructeurs
    public Semence() {
        this.dateCreation = LocalDateTime.now();
        this.dateSemence = LocalDate.now();
    }
    
    public Semence(TypeSemence typeSemence, BigDecimal montant, Programme programme, Compte compte, Long creePar) {
        this.typeSemence = typeSemence;
        this.montant = montant;
        this.programme = programme;
        this.compte = compte;
        this.creePar = creePar;
        this.dateCreation = LocalDateTime.now();
        this.dateSemence = LocalDate.now();
    }
    
    // Getters & Setters
    public Long getSemenceId() { return semenceId; }
    public void setSemenceId(Long semenceId) { this.semenceId = semenceId; }
    
    public TypeSemence getTypeSemence() { return typeSemence; }
    public void setTypeSemence(TypeSemence typeSemence) { this.typeSemence = typeSemence; }
    
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    
    public LocalDate getDateSemence() { return dateSemence; } // ✅ LocalDate
    public void setDateSemence(LocalDate dateSemence) { this.dateSemence = dateSemence; }

    
    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }
    
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
        Semence semence = (Semence) o;
        return Objects.equals(semenceId, semence.semenceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(semenceId);
    }
    
    @Override
    public String toString() {
        return String.format("Semence{id=%d, type=%s, montant=%,.0f F CFA}", 
                           semenceId, typeSemence, montant.doubleValue());
    }
}