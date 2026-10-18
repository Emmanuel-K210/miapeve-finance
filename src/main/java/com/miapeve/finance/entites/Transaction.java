package com.miapeve.finance.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.miapeve.finance.enums.TypeTransaction;

public class Transaction {
    private Long transactionId;
    private TypeTransaction typeTransaction; 
    private BigDecimal montant;
    private Long compteId;
    private String description;
    private String reference;
    private Long creePar;
    private LocalDateTime dateCreation;
    private Long modifiePar;
    private LocalDateTime dateModification;
    
    // Constructeur avec TypeTransaction
    public Transaction() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public Transaction(TypeTransaction typeTransaction, BigDecimal montant, Long compteId, 
                       String description, Long creePar) {
        this.typeTransaction = typeTransaction;
        this.montant = montant;
        this.compteId = compteId;
        this.description = description;
        this.creePar = creePar;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    
    public TypeTransaction getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(TypeTransaction typeTransaction) { 
        this.typeTransaction = typeTransaction; 
    }
    
    // Méthode utilitaire pour compatibilité avec String (si nécessaire)
    public void setTypeTransaction(String typeTransaction) { 
        try {
            this.typeTransaction = TypeTransaction.valueOf(typeTransaction.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback
            if (typeTransaction.equalsIgnoreCase("CREDIT")) {
                this.typeTransaction = TypeTransaction.CREDIT;
            } else if (typeTransaction.equalsIgnoreCase("DEBIT")) {
                this.typeTransaction = TypeTransaction.DEBIT;
            } else {
                throw new IllegalArgumentException("Type de transaction non reconnu: " + typeTransaction);
            }
        }
    }
    
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    
    public Long getCompteId() { return compteId; }
    public void setCompteId(Long compteId) { this.compteId = compteId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public Long getCreePar() { return creePar; }
    public void setCreePar(Long creePar) { this.creePar = creePar; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public Long getModifiePar() { return modifiePar; }
    public void setModifiePar(Long modifiePar) { this.modifiePar = modifiePar; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { 
        this.dateModification = dateModification; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type=%s, montant=%,.0f F CFA, compte=%d}", 
                           transactionId, typeTransaction, montant.doubleValue(), compteId);
    }
}